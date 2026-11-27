package com.example.birchbark.model

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

/**
 * US 02.04.08: saves and loads [LotteryInvite]s in the Firestore
 * "lotteryInvites" collection, and adds the campsite to the lottery when an
 * invite is accepted.
 *
 * Outstanding issues. An owner can't withdraw their campsite after accepting.
 */
class LotteryInviteRepository {

    private val db = FirebaseFirestore.getInstance()

    /** Gets told if saving or answering an invite worked. */
    interface DoneCallback {
        /** It worked. */
        fun onDone()

        /**
         * It didn't work.
         * @param message what to show the owner.
         */
        fun onError(message: String)
    }

    /** Gets the invites that were loaded. */
    interface ListCallback {
        /**
         * @param invites the pending invites, newest first.
         */
        fun onLoaded(invites: List<LotteryInvite>)

        /**
         * @param error what Firestore reported.
         */
        fun onError(error: Exception)
    }

    /**
     * Sends an invite for another owner's campsite.
     *
     * @param campsite the campsite being invited.
     * @param lottery the lottery it's being invited to.
     * @param callback told if it saved.
     */
    fun sendInvite(campsite: Campsite, lottery: Lottery, callback: DoneCallback) {
        //one invite per campsite per lottery, so the same campsite can't be invited twice
        val ref = db.collection("lotteryInvites").document("${lottery.id}_${campsite.id}")
        val invite = LotteryInvite(
            id = ref.id,
            lotteryId = lottery.id,
            fromOwnerId = lottery.ownerId,
            toOwnerId = campsite.ownerId,
            campsiteId = campsite.id,
            campsiteName = campsite.name,
            registrationStart = lottery.registrationStart,
            registrationEnd = lottery.registrationEnd,
            createdAt = System.currentTimeMillis()
        )
        ref.set(invite)
            .addOnSuccessListener { callback.onDone() }
            .addOnFailureListener { callback.onError("Couldn't send the invite, check your connection and try again") }
    }

    /**
     * Loads the invites another owner sent to this owner that haven't been answered yet.
     *
     * @param ownerId the owner whose campsites were invited.
     * @param callback gets the invites, or an error.
     */
    fun getPendingInvites(ownerId: String, callback: ListCallback) {
        db.collection("lotteryInvites")
            .whereEqualTo("toOwnerId", ownerId)
            .whereEqualTo("status", InviteStatus.PENDING.name)
            .get()
            .addOnSuccessListener { result ->
                val invites = result.toObjects(LotteryInvite::class.java)
                callback.onLoaded(invites.sortedByDescending { it.createdAt })
            }
            .addOnFailureListener { error -> callback.onError(error) }
    }

    /**
     * Accepts an invite. Checks the campsite isn't already in another lottery
     * first, then adds it to the lottery and marks the invite accepted, both
     * at once so it can't half save.
     *
     * @param invite the invite to accept.
     * @param callback told if it worked.
     */
    fun acceptInvite(invite: LotteryInvite, callback: DoneCallback) {
        LotteryRepository().getLotteriesForCampsite(invite.campsiteId, object : LotteryRepository.ListCallback {
            override fun onLoaded(lotteries: List<Lottery>) {
                //same rule as the lottery form, one campsite can't be in two lotteries at once
                if (CurrentLottery.pick(lotteries) != null) {
                    callback.onError("This campsite is already in a lottery")
                    return
                }
                val batch = db.batch()
                batch.update(
                    db.collection("lotteries").document(invite.lotteryId),
                    "campsiteIds", FieldValue.arrayUnion(invite.campsiteId)
                )
                batch.update(
                    db.collection("lotteryInvites").document(invite.id),
                    "status", InviteStatus.ACCEPTED.name
                )
                batch.commit()
                    .addOnSuccessListener { callback.onDone() }
                    .addOnFailureListener { error ->
                        Log.e("LotteryInviteRepository", "Accept failed", error)
                        callback.onError("Couldn't accept the invitation")
                    }
            }

            override fun onError(error: Exception) {
                callback.onError("Couldn't accept, check your connection and try again")
            }
        })
    }

    /**
     * Declines an invite. The campsite doesn't join the lottery.
     *
     * @param invite the invite to decline.
     * @param callback told if it worked.
     */
    fun declineInvite(invite: LotteryInvite, callback: DoneCallback) {
        db.collection("lotteryInvites").document(invite.id)
            .update("status", InviteStatus.DECLINED.name)
            .addOnSuccessListener { callback.onDone() }
            .addOnFailureListener { callback.onError("Couldn't decline, check your connection and try again") }
    }
}