
package com.example.birchbark.model

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source

/**
 * Manages entrant membership in lottery waitlists.
 */
class WaitlistRepository {

    private val db = FirebaseFirestore.getInstance()

    enum class JoinResult {
        JOINED,
        ALREADY_JOINED,
        REGISTRATION_CLOSED,
        WAITLIST_FULL,
        LOTTERY_NOT_FOUND
    }

    /**
     * Adds an entrant to a lottery's waitlist.
     *
     * Uses a Firestore transaction to prevent duplicate
     * entries and enforce the waitlist capacity.
     */
    fun joinWaitlist(
        lotteryId: String,
        userId: String,
        onResult: (JoinResult) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (lotteryId.isBlank() || userId.isBlank()) {
            onFailure(IllegalArgumentException("Missing lottery or user ID"))
            return
        }

        val lotteryRef = db.collection("lotteries")
            .document(lotteryId)

        val entrantRef = lotteryRef.collection("waitlist")
            .document(userId)

        val statsRef = lotteryRef.collection("waitlistMeta")
            .document("stats")

        db.runTransaction { transaction ->
            val lotteryDoc = transaction.get(lotteryRef)
            val entrantDoc = transaction.get(entrantRef)
            val statsDoc = transaction.get(statsRef)

            val lottery = lotteryDoc.toObject(Lottery::class.java)
                ?: return@runTransaction JoinResult.LOTTERY_NOT_FOUND

            val count = statsDoc.getLong("count")?.toInt() ?: 0

            when {
                entrantDoc.exists() -> JoinResult.ALREADY_JOINED

                !RegistrationChecker.isOpen(
                    lottery,
                    System.currentTimeMillis()
                ) -> JoinResult.REGISTRATION_CLOSED

                WaitlistChecker.isFull(
                    lottery,
                    count
                ) -> JoinResult.WAITLIST_FULL

                else -> {
                    transaction.set(
                        entrantRef,
                        mapOf(
                            "userId" to userId,
                            "joinedAt" to FieldValue.serverTimestamp()
                        )
                    )

                    transaction.set(
                        statsRef,
                        mapOf("count" to count + 1),
                        SetOptions.merge()
                    )

                    JoinResult.JOINED
                }
            }
        }
            .addOnSuccessListener { result ->
                onResult(result)
            }
            .addOnFailureListener { error ->
                onFailure(error)
            }
    }

    enum class LeaveResult {
        LEFT,
        NOT_JOINED,
        LOTTERY_NOT_FOUND
    }

    /**
     * Checks whether an entrant is already registered for a lottery.
     *
     * @param lotteryId The lottery's Firestore document ID.
     * @param userId The authenticated entrant's Firebase UID.
     * @param onResult Receives true if the entrant is registered.
     * @param onFailure Receives any Firestore error.
     */
    fun isOnWaitlist(
        lotteryId: String,
        userId: String,
        onResult: (Boolean) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("lotteries")
            .document(lotteryId)
            .collection("waitlist")
            .document(userId)
            .get(Source.SERVER)
            .addOnSuccessListener { document ->
                onResult(document.exists())
            }
            .addOnFailureListener { error ->
                onFailure(error)
            }
    }

    /**
     * Removes an entrant from a lottery waitlist.
     *
     * Uses a Firestore transaction to remove their registration and
     * decrease the waitlist count together.
     *
     * @param lotteryId The lottery's Firestore document ID.
     * @param userId The authenticated entrant's Firebase UID.
     * @param onResult Receives the outcome of the removal.
     * @param onFailure Receives any Firestore error.
     */
    fun leaveWaitlist(
        lotteryId: String,
        userId: String,
        onResult: (LeaveResult) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (lotteryId.isBlank() || userId.isBlank()) {
            onFailure(IllegalArgumentException("Missing lottery or user ID"))
            return
        }

        val lotteryRef = db.collection("lotteries")
            .document(lotteryId)

        val entrantRef = lotteryRef.collection("waitlist")
            .document(userId)

        val statsRef = lotteryRef.collection("waitlistMeta")
            .document("stats")

        db.runTransaction { transaction ->
            val lotteryDoc = transaction.get(lotteryRef)
            val entrantDoc = transaction.get(entrantRef)
            val statsDoc = transaction.get(statsRef)

            when {
                !lotteryDoc.exists() ->
                    LeaveResult.LOTTERY_NOT_FOUND

                !entrantDoc.exists() ->
                    LeaveResult.NOT_JOINED

                else -> {
                    val count = statsDoc.getLong("count")
                        ?: throw IllegalStateException(
                            "Waitlist count is missing"
                        )

                    if (count <= 0L) {
                        throw IllegalStateException(
                            "Invalid waitlist count"
                        )
                    }

                    transaction.delete(entrantRef)

                    transaction.set(
                        statsRef,
                        mapOf("count" to count - 1),
                        SetOptions.merge()
                    )

                    LeaveResult.LEFT
                }
            }
        }
            .addOnSuccessListener { result ->
                onResult(result)
            }
            .addOnFailureListener { error ->
                onFailure(error)
            }
    }

    // See [TBD 1] for LLM.md
    /**
     * Asynchronously retrieves a list of lottery IDs for which the specified entrant has an
     * entry in the lottery's `waitlist` subcollection.
     *
     * Fetches all documents in the `lotteries` collection and checks each lottery's `waitlist`
     * subcollection for a document keyed by [userId]. If [userId] is blank or no lotteries exist,
     * [onResult] is invoked immediately with an empty list.
     *
     * @param userId The unique identifier of the entrant to check across lottery waitlists.
     * @param onResult Callback invoked with the list of matching lottery IDs once all checks complete.
     * @param onFailure Callback invoked with the [Exception] if fetching the `lotteries` collection fails.
     */
    fun getWaitlistLotteryIdsForEntrant(
        userId: String,
        onResult: (List<String>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (userId.isBlank()) {
            onResult(emptyList())
            return
        }
        db.collection("lotteries").get()
            .addOnSuccessListener { snapshot ->
                val docs = snapshot.documents
                if (docs.isEmpty()) {
                    onResult(emptyList())
                    return@addOnSuccessListener
                }
                val matched = java.util.Collections.synchronizedList(mutableListOf<String>())
                var remaining = docs.size
                for (lotteryDoc in docs) {
                    lotteryDoc.reference.collection("waitlist").document(userId).get()
                        .addOnSuccessListener { entry ->
                            if (entry.exists()) {
                                matched.add(lotteryDoc.id)
                            }
                        }
                        .addOnCompleteListener {
                            remaining--
                            if (remaining == 0) {
                                onResult(matched.toList())
                            }
                        }
                }
            }
            .addOnFailureListener { error -> onFailure(error) }
    }
}
