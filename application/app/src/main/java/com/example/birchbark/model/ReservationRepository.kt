package com.example.birchbark.model

// See #046 in LLM.md
// See #054 in LLM.md (confirm with dates, decline, bookings)

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source

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
                val winners = LotteryDraw.drawWinners(entrantIds, lottery.numWinners)
                val reservations = LotteryDraw.buildReservations(lottery, winners, now)

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
                    transaction.update(lotteryRef, "drawn", true)

                    DrawResult.DRAWN
                }
                    .addOnSuccessListener { result -> onResult(result) }
                    .addOnFailureListener { error -> onFailure(error) }
            }
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
     * owner's campsites, newest first.
     *
     * @param ownerId the owner's FID.
     */
    fun getReservationsForOwner(
        ownerId: String,
        onResult: (List<Reservation>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("reservations").whereEqualTo("ownerId", ownerId).get()
            .addOnSuccessListener { result ->
                onResult(result.toObjects(Reservation::class.java).sortedByDescending { it.createdAt })
            }
            .addOnFailureListener { error -> onFailure(error) }
    }

    /**
     * US 01.04.01, 02.11.01, 02.07.01: the entrant accepts their reservation
     * for the days they picked, after agreeing to the campsite rules.
     *
     * In one batch it marks the reservation CONFIRMED with the campsite and
     * dates, copies in the entrant's contact details (so the owner can see who
     * booked without reading their profile), and saves a [CampsiteBooking] so
     * other winners can see those days are taken. Check the days first with
     * [AvailableDates.problemWith].
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
        val batch = db.batch()
        batch.update(
            db.collection("reservations").document(reservation.id),
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
        //same ID as the reservation, so each reservation has at most one booking
        batch.set(
            db.collection("campsiteBookings").document(reservation.id),
            mapOf(
                "campsiteId" to campsiteId,
                "startDate" to startDate,
                "endDate" to endDate
            )
        )
        batch.commit()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { error -> onFailure(error) }
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
                onResult(result.documents.map { document ->
                    CampsiteBooking(
                        id = document.id,
                        campsiteId = document.getString("campsiteId") ?: "",
                        startDate = document.getLong("startDate") ?: 0L,
                        endDate = document.getLong("endDate") ?: 0L
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