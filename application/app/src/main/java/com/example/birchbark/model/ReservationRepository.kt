package com.example.birchbark.model

// See #046 in LLM.md
// See #054 in LLM.md (confirm with dates, decline, bookings)

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.example.birchbark.util.Time
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestoreException

/**
 * Saves and loads [Reservation] objects in the top-level Firestore
 * "reservations" collection, and runs the winner draw (US 02.05.01).
 *
 * Firestore calls are asynchronous, so every function reports back
 * through onResult/onFailure instead of returning a value.
 */
class ReservationRepository {

    private val db = FirebaseFirestore.getInstance()

    enum class DrawResult { DRAWN, ALREADY_DRAWN, NOT_DUE }

    /**
     * US 02.05.01: draws winners once registration has closed, saves one
     * PENDING reservation per winner, and marks the lottery as drawn.
     *
     * Safe to call every time a screen opens: it does nothing before
     * registration ends, and the transaction makes sure only one device
     * ever runs the draw.
     *
     * US 01.03.01, 01.03.02: everyone on the waitlist gets a win or lose
     * notification ([DrawNotifications]), except entrants who opted out
     * (US 01.10.01). The notifications are written in the same transaction as
     * the reservations and the drawn flag, so they're saved together or not at
     * all: if the app closes mid-draw nothing is saved, and the next device to
     * open the campsite runs the whole draw again. See #055 in LLM.md.
     *
     * A Firestore transaction holds at most 500 writes, so this supports
     * waitlists of up to about 490 entrants.
     *
     * @param lottery the lottery to draw. Must have its [Lottery.id].
     * @param now current time in epoch ms.
     */
    fun runDrawIfDue(
        lottery: Lottery,
        now: Long,
        onResult: (DrawResult) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (lottery.drawn) {
            onResult(DrawResult.ALREADY_DRAWN)
            return
        }
        if (lottery.id.isBlank() || now < lottery.registrationEnd) {
            onResult(DrawResult.NOT_DUE)
            return
        }

        val lotteryRef = db.collection("lotteries").document(lottery.id)

        // Registration is closed, so the waitlist can no longer change.
        // That makes it safe to read outside the transaction
        // (Android transactions can't read a whole collection).
        lotteryRef.collection("waitlist").get(Source.SERVER)
            .addOnSuccessListener { snapshot ->
                val entrantIds = snapshot.documents.map { it.id }
                //opt-outs are read before the transaction too, it can't run queries
                NotificationRepository().getOptedOut(
                    entrantIds,
                    onResult = { optedOut ->
                        saveDraw(lottery, lotteryRef, entrantIds, optedOut, now, onResult, onFailure)
                    },
                    onFailure = onFailure
                )
            }
            .addOnFailureListener { error -> onFailure(error) }
    }

    /**
     * Picks the winners and saves the draw in one transaction: the
     * reservations, the win/lose notifications and the drawn flag.
     */
    private fun saveDraw(
        lottery: Lottery,
        lotteryRef: DocumentReference,
        entrantIds: List<String>,
        optedOut: Set<String>,
        now: Long,
        onResult: (DrawResult) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val winners = LotteryDraw.drawWinners(entrantIds, lottery.numWinners)
        val reservations = LotteryDraw.buildReservations(lottery, winners, now)
        val notifications = DrawNotifications.build(lottery, entrantIds, winners, optedOut, now)

        db.runTransaction { transaction ->
            // Re-check inside the transaction: another device may
            // have drawn since this lottery was loaded.
            val fresh = transaction.get(lotteryRef)
            if (fresh.getBoolean("drawn") == true) {
                return@runTransaction DrawResult.ALREADY_DRAWN
            }

            for (reservation in reservations) {
                val ref = db.collection("reservations").document(reservation.id)
                transaction.set(ref, reservation)
            }
            for (notification in notifications) {
                val ref = db.collection("notifications").document(notification.id)
                transaction.set(ref, notification)
            }
            transaction.update(lotteryRef, "drawn", true)

            DrawResult.DRAWN
        }
            .addOnSuccessListener { result -> onResult(result) }
            .addOnFailureListener { error -> onFailure(error) }
    }

    /**
     * US 01.06.01, 01.06.02: loads every reservation for an entrant,
     * newest first. The screen splits them into active and past.
     *
     * @param entrantId the entrant's FID.
     */
    fun getReservationsForEntrant(
        entrantId: String,
        onResult: (List<Reservation>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("reservations").whereEqualTo("entrantId", entrantId).get()
            .addOnSuccessListener { result ->
                onResult(result.toObjects(Reservation::class.java).sortedByDescending { it.createdAt })
            }
            .addOnFailureListener { error -> onFailure(error) }
    }

    /**
     * US 02.07.01, 02.07.02, 02.07.03: loads every reservation for an
     * owner's campsites, newest first. For US 02.12.01, confirmed stays are matched by
     * selected campsite so invited owners can also find bookings created by older clients.
     *
     * @param ownerId the owner's FID.
     */
    fun getReservationsForOwner(
        ownerId: String,
        onResult: (List<Reservation>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val bookings = db.collection("reservations")
        val sitesTask = db.collection("campsites").whereEqualTo("ownerId", ownerId).get()
        val organizedTask = bookings.whereEqualTo("ownerId", ownerId).get()
        Tasks.whenAll(sitesTask, organizedTask).continueWithTask { initial ->
            initial.result
            val sites = sitesTask.result.documents.map { it.id }.toSet()
            val organized = organizedTask.result.documents.mapNotNull { doc ->
                doc.toObject(Reservation::class.java)?.copy(id = doc.id)
            }.filter { it.startDate == null || it.campsiteId in sites }
            // The existing rules allow organizer queries. Selected-site queries additionally
            // find invited owners' bookings once the corresponding permission is available.
            val reads = sites.map { site ->
                bookings.whereEqualTo("campsiteId", site).get().continueWith { task ->
                    val failure = task.exception
                    if (failure is FirebaseFirestoreException &&
                        failure.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        emptyList()
                    } else {
                        task.result.documents.mapNotNull { doc ->
                            doc.toObject(Reservation::class.java)?.copy(id = doc.id)
                        }.filter { it.status != ReservationStatus.PENDING }
                    }
                }
            }
            Tasks.whenAll(reads).continueWith { completed ->
                completed.result
                (organized + reads.flatMap { it.result }).distinctBy { it.id }.sortedByDescending { it.createdAt }
            }
        }.addOnSuccessListener { onResult(it) }.addOnFailureListener { onFailure(it) }
    }

    /**
     * US 01.04.01, 02.11.01, 02.07.01: the entrant accepts their reservation
     * for the days they picked, after agreeing to the campsite rules.
     *
     * In one transaction it marks the reservation CONFIRMED with the campsite
     * and dates, copies in the entrant's contact details (so the owner can see
     * who booked without reading their profile), and saves one
     * [CampsiteBooking] per night ([BookingNights]) holding the reservations on
     * that night, so other winners can see which days are full.
     *
     * Capacity: a campground holds up to [Campsite.maxOccupancy] parties a
     * night (Riley's PR feedback), so the transaction reads the campsite and
     * every night first, and stops if any night already has that many
     * reservations. Firestore retries the transaction if a night changes while
     * it runs, so two entrants can't both take the last spot: one gets
     * [DatesTakenException] and nothing of theirs is saved. See #079 and #091
     * in LLM.md.
     *
     * @param reservation the PENDING reservation being accepted.
     * @param campsiteId the campsite being booked.
     * @param startDate first day of the stay (epoch ms, start of that day).
     * @param endDate last day of the stay (epoch ms, end of that day).
     * @param name the entrant's full name.
     * @param email the entrant's email.
     * @param phone the entrant's phone number.
     */
    fun confirm(
        reservation: Reservation,
        campsiteId: String,
        startDate: Long,
        endDate: Long,
        name: String,
        email: String,
        phone: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val reservationRef = db.collection("reservations").document(reservation.id)
        //one booking per night (BookingNights), so two stays can never share a night
        val nights = BookingNights.bookingsFor(
            campsiteId,
            Time.toLocalDate(startDate),
            Time.lastDayOf(endDate)
        )
        val nightRefs = nights.map { db.collection("campsiteBookings").document(it.id) }

        val campsiteRef = db.collection("campsites").document(campsiteId)

        db.runTransaction { transaction ->
            //all reads before any writes: stop if any night filled up since the screen loaded
            val capacity = maxOf(1, transaction.get(campsiteRef).getLong("maxOccupancy")?.toInt() ?: 1)
            val nightDocs = nightRefs.map { transaction.get(it) }
            if (nightDocs.any { reservationIdsOf(it).size >= capacity }) {
                throw DatesTakenException()
            }

            transaction.update(
                reservationRef,
                mapOf(
                    "status" to ReservationStatus.CONFIRMED.name,
                    "campsiteId" to campsiteId,
                    "startDate" to startDate,
                    "endDate" to endDate,
                    "entrantName" to name,
                    "entrantEmail" to email,
                    "entrantPhone" to phone
                )
            )
            for ((night, doc) in nights.zip(nightDocs)) {
                if (doc.exists()) {
                    //someone else is already on this night, take the next spot
                    transaction.update(doc.reference, "reservationIds", FieldValue.arrayUnion(reservation.id))
                } else {
                    transaction.set(
                        doc.reference,
                        mapOf(
                            "campsiteId" to night.campsiteId,
                            "startDate" to night.startDate,
                            "endDate" to night.endDate,
                            "reservationIds" to listOf(reservation.id)
                        )
                    )
                }
            }
        }
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { error -> onFailure(error) }
    }

    /**
     * Thrown inside [confirm]'s transaction when one of the picked nights was
     * booked by someone else after the screen loaded. Nothing is saved.
     */
    class DatesTakenException : Exception("Some of those days just filled up, pick other days")

    /** The reservations on one night document, or none if it doesn't exist. */
    private fun reservationIdsOf(night: DocumentSnapshot): List<String> {
        @Suppress("UNCHECKED_CAST")
        return night.get("reservationIds") as? List<String> ?: emptyList()
    }

    /**
     * US 01.04.03: the entrant turns down a reservation they won.
     *
     * @param reservationId the reservation's document ID ([Reservation.id]).
     */
    fun decline(
        reservationId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        updateStatus(reservationId, ReservationStatus.DECLINED, onSuccess, onFailure)
    }

    /**
     * US 02.08.01: the owner rejects a reservation. Marks it REJECTED and, if it
     * was confirmed, frees its nights in "campsiteBookings" so others can book
     * them. See #080 in LLM.md.
     *
     * @param reservation the active reservation being rejected.
     */
    fun reject(
        reservation: Reservation,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        endStay(reservation, ReservationStatus.REJECTED, onSuccess, onFailure)
    }

    /**
     * US 01.07.01: the entrant cancels their own reservation. Marks it
     * CANCELLED and frees its nights the same way [reject] does, so those
     * dates open up again for other winners.
     *
     * @param reservation the active reservation being cancelled.
     */
    fun cancel(
        reservation: Reservation,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        endStay(reservation, ReservationStatus.CANCELLED, onSuccess, onFailure)
    }

    /**
     *     // See #TBD12 in LLM.md
     * Gives a reservation its new [status] and, if it was confirmed, removes it
     * from each of its nights in "campsiteBookings". Both happen in one batch,
     * so a stay that ended never keeps its nights.
     *
     * @param reservation the reservation that's ending.
     * @param status REJECTED (owner) or CANCELLED (entrant).
     */
    private fun endStay(
        reservation: Reservation,
        status: ReservationStatus,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val batch = db.batch()
        batch.update(
            db.collection("reservations").document(reservation.id),
            "status", status.name
        )

        val campsiteId = reservation.campsiteId
        val start = reservation.startDate
        val end = reservation.endDate
        //only confirmed reservations have nights booked
        if (reservation.status == ReservationStatus.CONFIRMED &&
            campsiteId != null && start != null && end != null
        ) {
            val nights = BookingNights.bookingsFor(campsiteId, Time.toLocalDate(start), Time.lastDayOf(end))
            //free this reservation's spot on each night, others on the same night stay
            for (night in nights) {
                batch.update(
                    db.collection("campsiteBookings").document(night.id),
                    "reservationIds", FieldValue.arrayRemove(reservation.id)
                )
            }
        }

        batch.commit()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { error -> onFailure(error) }
    }

    /**
     * US 01.04.01: the days already booked at a campsite, for working out
     * which days a winner can still pick ([AvailableDates]).
     *
     * @param campsiteId the campsite's document ID.
     */
    fun getBookingsForCampsite(
        campsiteId: String,
        onResult: (List<CampsiteBooking>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("campsiteBookings").whereEqualTo("campsiteId", campsiteId).get()
            .addOnSuccessListener { result ->
                //a night everyone has left (rejected) is empty, not booked
                onResult(result.documents.filter { reservationIdsOf(it).isNotEmpty() }.map { document ->
                    CampsiteBooking(
                        id = document.id,
                        campsiteId = document.getString("campsiteId") ?: "",
                        startDate = document.getLong("startDate") ?: 0L,
                        endDate = document.getLong("endDate") ?: 0L,
                        reservationIds = reservationIdsOf(document)
                    )
                })
            }
            .addOnFailureListener { error -> onFailure(error) }
    }

    /**
     * Changes a reservation's status: confirm (US 02.11.01), decline
     * (US 01.04.03), cancel (US 01.07.01) or reject (US 02.08.01).
     *
     * @param reservationId the reservation's document ID ([Reservation.id]).
     */
    fun updateStatus(
        reservationId: String,
        status: ReservationStatus,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("reservations").document(reservationId)
            .update("status", status.name)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { error -> onFailure(error) }
    }
}