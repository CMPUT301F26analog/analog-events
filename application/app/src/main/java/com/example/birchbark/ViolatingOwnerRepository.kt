package com.example.birchbark

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.WriteBatch

/**
 * US 03.07.01 Blocks violating campsite owners and removes their profiles.
 * References.md 57 to 66
 * The owner's FID is used to find their profile in `users`, campsites, and
 * lotteries. Active reservations affected by the removal are cancelled, their
 * campsite booking records and lottery waitlist data are deleted, and entrants
 * with notifications enabled are notified. The repository then writes records
 * to `blockedOwners/{authUid}` and `blockedOwnerFids/{fid}`, and removes the
 * owner profile and matching auth link.
 *
 * @property db Firestore instance used to read and update the database.
 */
class ViolatingOwnerRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    /**
     * Cancels an owner's active reservations, removes their campsites and
     * lotteries, notifies affected entrants who have notifications enabled,
     * adds the owner to the block list, and removes the owner's profile.
     *
     * Only profiles whose `roles` field contains `"owner"` can be blocked.
     * Campsites and lotteries are matched by `ownerId == fid`.
     *
     * @param fid Firebase Installation ID and document ID of the owner profile
     * in the `users` collection.
     * @param onSuccess Called after the reservations, campsites, lotteries,
     * notifications, block records, and profile operations succeed.
     * @param onError Called with an exception if validation or a Firestore
     * operation fails.
     */
    fun blockOwnerAndRemoveProfile(
        fid: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val userRef = db.collection("users").document(fid)

        userRef.get()
            .addOnSuccessListener { user ->
                try {
                    if (!user.exists()) {
                        throw IllegalStateException(
                            "The profile no longer exists."
                        )
                    }

                    val roles = (user.get("roles") as? List<*>)
                        ?.filterIsInstance<String>()
                        .orEmpty()

                    if (roles.none {
                            it.equals("owner", ignoreCase = true)
                        }) {
                        throw IllegalStateException(
                            "This profile is not an owner."
                        )
                    }

                    val authUid = user.getString("authUid")
                        ?.takeIf { it.isNotBlank() }
                        ?: throw IllegalStateException(
                            "The owner profile does not have an authUid."
                        )

                    prepareOwnerRemoval(
                        fid,
                        authUid,
                        onSuccess,
                        onError
                    )
                } catch (error: Exception) {
                    onError(error)
                }
            }
            .addOnFailureListener { error ->
                onError(error)
            }
    }

    /**
     * Writes the owner's block records and removes the owner profile and auth
     * link.
     *
     * The transaction rechecks that the profile still exists, still belongs to
     * the same auth UID, and still has the owner role before writing or deleting
     * any documents.
     *
     * @param fid FID and document ID of the owner profile.
     * @param authUid Firebase Authentication UID of the owner.
     * @param onSuccess Called when the transaction succeeds.
     * @param onError Called with an exception if validation or the transaction
     * fails.
     */
    private fun blockAndRemoveProfile(
        fid: String,
        authUid: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val userRef = db.collection("users").document(fid)
        val authLinkRef = db.collection("authLinks").document(authUid)
        val blockedOwnerRef =
            db.collection("blockedOwners").document(authUid)
        val blockedFidRef =
            db.collection("blockedOwnerFids").document(fid)

        db.runTransaction { transaction ->
            val user = transaction.get(userRef)
            val authLink = transaction.get(authLinkRef)

            if (!user.exists()) {
                throw IllegalStateException(
                    "The profile no longer exists.") }
            val roles = (user.get("roles") as? List<*>)
                ?.filterIsInstance<String>()
                .orEmpty()

            if (roles.none {
                    it.equals("owner", ignoreCase = true) }) {
                throw IllegalStateException(
                    "This profile is not an owner.") }
            if (user.getString("authUid") != authUid) {
                throw IllegalStateException(
                    "The owner's authUid changed. Please try again."
                )
            }
            if (authLink.exists() && authLink.getString("fid") != fid) {
                throw IllegalStateException(
                    "The owner's auth link points to a different profile."
                ) }
            val blockData = mapOf(
                "authUid" to authUid,
                "fid" to fid,
                "blockedAt" to FieldValue.serverTimestamp())
            transaction.set(blockedOwnerRef, blockData)
            transaction.set(blockedFidRef, blockData)
            transaction.delete(userRef)

            if (authLink.exists()) {
                transaction.delete(authLinkRef) }

            true }
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { error -> onError(error) } }

    private fun prepareOwnerRemoval(
        fid: String,
        authUid: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        loadAdminFid(
            onResult = { adminFid ->
                loadQueries(
                    listOf(
                        db.collection("campsites").whereEqualTo("ownerId", fid).get(),
                        db.collection("lotteries").whereEqualTo("ownerId", fid).get()
                    ),
                    onResult = { ownerData ->
                        val campsites = ownerData[0].documents
                        val lotteries = ownerData[1].documents

                        val reservationQueries = mutableListOf(
                            db.collection("reservations").whereEqualTo("ownerId", fid).get()
                        )
                        campsites.forEach { campsite ->
                            reservationQueries += db.collection("reservations")
                                .whereEqualTo("campsiteId", campsite.id)
                                .get()
                        }

                        val waitlistQueries = lotteries.map { lottery ->
                            lottery.reference.collection("waitlist").get()
                        }

                        val bookingQueries = campsites.map { campsite ->
                            db.collection("campsiteBookings")
                                .whereEqualTo("campsiteId", campsite.id)
                                .get()
                        }

                        val allQueries = reservationQueries + waitlistQueries + bookingQueries

                        loadQueries(
                            allQueries,
                            onResult = { results ->
                                val reservationCount = reservationQueries.size
                                val waitlistStart = reservationCount
                                val bookingStart = waitlistStart + waitlistQueries.size

                                val reservations = results
                                    .take(reservationCount)
                                    .flatMap { it.documents }
                                    .distinctBy { it.id }

                                val waitlistEntries = results
                                    .drop(waitlistStart)
                                    .take(waitlistQueries.size)
                                    .flatMap { it.documents }

                                val campsiteBookings = results
                                    .drop(bookingStart)
                                    .flatMap { it.documents }

                                val activeReservations = reservations.filter { reservation ->
                                    reservation.getString("status") in
                                            listOf("PENDING", "CONFIRMED")
                                }

                                val entrantIds = activeReservations
                                    .mapNotNull { it.getString("entrantId") }
                                    .filter { it.isNotBlank() }
                                    .distinct()

                                loadOptedOutFids(
                                    entrantIds,
                                    onResult = { optedOut ->
                                        val writes = mutableListOf<(WriteBatch) -> Unit>()

                                        activeReservations.forEach { reservation ->
                                            writes += { batch ->
                                                batch.update(
                                                    reservation.reference,
                                                    "status",
                                                    "CANCELLED") } }

                                        campsiteBookings.forEach { booking ->
                                            writes += { batch ->
                                                batch.delete(booking.reference) } }

                                        waitlistEntries.forEach { entry ->
                                            writes += { batch ->
                                                batch.delete(entry.reference) } }

                                        lotteries.forEach { lottery ->
                                            writes += { batch ->
                                                batch.delete(
                                                    lottery.reference
                                                        .collection("waitlistMeta")
                                                        .document("stats")) }
                                            writes += { batch ->
                                                batch.delete(lottery.reference) } }

                                        campsites.forEach { campsite ->
                                            writes += { batch ->
                                                batch.delete(campsite.reference) } }
                                        entrantIds
                                            .filterNot { it in optedOut }
                                            .forEach { entrantId ->
                                                val notificationRef = db.collection("notifications")
                                                    .document("owner_removed_${fid}_$entrantId")

                                                val notification = mapOf(
                                                    "id" to notificationRef.id,
                                                    "recipientId" to entrantId,
                                                    "senderId" to adminFid,
                                                    "type" to "OWNER_REMOVED",
                                                    "title" to "Reservation cancelled",
                                                    "body" to "The campsite owner was removed, so your reservation is no longer available.",
                                                    "createdAt" to System.currentTimeMillis(),
                                                    "read" to false)

                                                writes += { batch ->
                                                    batch.set(notificationRef, notification) } }

                                        commitWrites(
                                            writes,
                                            onSuccess = {
                                                blockAndRemoveProfile(
                                                    fid,
                                                    authUid,
                                                    onSuccess,
                                                    onError) },
                                            onError = onError) },
                                    onError = onError) },
                            onError = onError) },
                    onError = onError) },
            onError = onError) }

    private fun loadAdminFid(
        onResult: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid.isNullOrBlank()) {
            onError(IllegalStateException("No signed-in admin was found."))
            return
        }

        db.collection("authLinks")
            .document(uid)
            .get()
            .addOnSuccessListener { authLink ->
                val adminFid = authLink.getString("fid")
                if (adminFid.isNullOrBlank()) {
                    onError(IllegalStateException("The admin's FID could not be found."))
                } else {
                    onResult(adminFid)
                } }
            .addOnFailureListener(onError) }

    private fun loadQueries(
        queries: List<Task<QuerySnapshot>>,
        onResult: (List<QuerySnapshot>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (queries.isEmpty()) {
            onResult(emptyList())
            return
        }

        Tasks.whenAllSuccess<QuerySnapshot>(queries)
            .addOnSuccessListener(onResult)
            .addOnFailureListener(onError)
    }

    private fun loadOptedOutFids(
        fids: List<String>,
        onResult: (Set<String>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (fids.isEmpty()) {
            onResult(emptySet())
            return
        }

        val preferenceReads = fids.map { fid ->
            db.collection("notificationPrefs").document(fid).get()
        }

        Tasks.whenAllSuccess<DocumentSnapshot>(preferenceReads)
            .addOnSuccessListener { documents ->
                onResult(
                    documents
                        .filter { it.getBoolean("enabled") == false }
                        .mapNotNull { it.id }
                        .toSet()
                )
            }
            .addOnFailureListener(onError)
    }

    private fun commitWrites(
        writes: List<(WriteBatch) -> Unit>,
        index: Int = 0,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (index >= writes.size) {
            onSuccess()
            return
        }

        val batch = db.batch()
        writes.drop(index).take(450).forEach { write ->
            write(batch)
        }

        batch.commit()
            .addOnSuccessListener {
                commitWrites(
                    writes = writes,
                    index = index + 450,
                    onSuccess = onSuccess,
                    onError = onError
                )
            }
            .addOnFailureListener(onError)
    } }