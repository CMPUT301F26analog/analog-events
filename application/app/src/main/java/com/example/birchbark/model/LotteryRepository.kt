package com.example.birchbark.model

// LLM assistance: see doc/LLM.md, Prompt #002 (Akshat)

import com.google.firebase.firestore.FirebaseFirestore

/**
 * Saves and loads [Lottery] objects in the Firestore "lotteries" collection.
 * The only class that talks to Firestore about lotteries.
 *
 * Firestore calls are asynchronous, so no function here returns a result
 * directly: each one reports back later through a [SaveCallback] or [LoadCallback].
 */
class LotteryRepository {

    private val db = FirebaseFirestore.getInstance()

    /** Receives the result of [createLottery] or [updateLottery]. Exactly one function is called. */
    interface SaveCallback {
        /**
         * The lottery was saved.
         * @param lotteryId the Firestore document ID of the saved lottery.
         */
        fun onSuccess(lotteryId: String)

        /**
         * The lottery broke a rule, so nothing was saved.
         * @param errors messages to show the owner, one per problem.
         */
        fun onInvalid(errors: List<String>)

        /**
         * Firestore failed, e.g. no network or no permission. Nothing was saved.
         * @param error the exception Firestore reported.
         */
        fun onError(error: Exception)
    }

    /** Receives the result of [getLottery]. Exactly one function is called. */
    interface LoadCallback {
        /**
         * The lottery was found.
         * @param lottery the loaded lottery.
         */
        fun onLoaded(lottery: Lottery)

        /** No lottery exists with that ID. */
        fun onNotFound()

        /**
         * Firestore failed, e.g. no network or no permission.
         * @param error the exception Firestore reported.
         */
        fun onError(error: Exception)
    }

    /** Receives the result of [getLotteriesForCampsite]. Exactly one function is called. */
    interface ListCallback {
        /**
         * The lotteries were loaded.
         * @param lotteries every lottery that includes the campsite. Empty if there aren't any.
         */
        fun onLoaded(lotteries: List<Lottery>)

        /**
         * Firestore failed, e.g. no network or no permission.
         * @param error the exception Firestore reported.
         */
        fun onError(error: Exception)
    }

    /**
     * Loads one lottery by its document ID.
     *
     * @param id the lottery's Firestore document ID ([Lottery.id]).
     * @param callback receives the lottery, a not-found result, or an error.
     */
    fun getLottery(id: String, callback: LoadCallback) {
        db.collection("lotteries").document(id).get()
            .addOnSuccessListener { document ->
                val lottery = document.toObject(Lottery::class.java)
                if (lottery == null) {
                    callback.onNotFound()
                } else {
                    callback.onLoaded(lottery)
                }
            }
            .addOnFailureListener { error -> callback.onError(error) }
    }

    /**
     * US 02.04.01: saves a new lottery. Runs [LotteryValidator] first and
     * refuses to save an invalid lottery. Firestore generates the ID, which is
     * also stored inside the saved lottery's [Lottery.id].
     *
     * @param lottery the new lottery. Its [Lottery.id] is ignored and replaced.
     * @param callback onSuccess with the new ID, onInvalid with the rule
     *   messages, or onError if Firestore fails.
     */
    fun createLottery(lottery: Lottery, callback: SaveCallback) {

        //Checking rules before touching DB
        val errors = LotteryValidator.validate(lottery)
        if (errors.isNotEmpty()) {
            callback.onInvalid(errors)
            return
        }

        val ref = db.collection("lotteries").document()
        val toSave = lottery.copy(id = ref.id)

        ref.set(toSave)
            .addOnSuccessListener {
                callback.onSuccess(ref.id)
            }
            .addOnFailureListener { error ->
                callback.onError(error)
            }

    }

    /**
     * US 02.04.06: saves changes to an existing lottery, overwriting the stored
     * version. Refuses if the lottery has been drawn ([EditPermission]), has no
     * ID yet, or breaks a rule ([LotteryValidator]).
     *
     * @param lottery the edited lottery. Must have the [Lottery.id] it was saved with.
     * @param callback onSuccess with the lottery's ID, onInvalid with the reason
     *   it was refused, or onError if Firestore fails.
     */
    fun updateLottery(lottery: Lottery, callback: SaveCallback) {
        //can't edit after draw
        if (!EditPermission.canEdit(lottery)) {
            callback.onInvalid(listOf("Cannot edit lottery after draw"))
            return
        }

        //no ID means there's no saved lottery to update
        if (lottery.id.isBlank()) {
            callback.onInvalid(listOf("Lottery has not been saved yet"))
            return
        }

        //checking rules before touching DB
        val errors = LotteryValidator.validate(lottery)
        if (errors.isNotEmpty()) {
            callback.onInvalid(errors)
            return
        }

        //overwrite the existing document
        db.collection("lotteries").document(lottery.id).set(lottery)
            .addOnSuccessListener {
                callback.onSuccess(lottery.id)
            }
            .addOnFailureListener { error ->
                callback.onError(error)
            }
    }

    /**
     * US 02.04.01: loads every lottery that includes a campsite, so the campsite
     * details screen can show whether a lottery is open.
     *
     * @param campsiteId the campsite's Firestore document ID, as stored in [Lottery.campsiteIds].
     * @param callback receives the lotteries, or an error.
     */
    fun getLotteriesForCampsite(campsiteId: String, callback: ListCallback) {
        db.collection("lotteries").whereArrayContains("campsiteIds", campsiteId).get()
            .addOnSuccessListener { result ->
                callback.onLoaded(result.toObjects(Lottery::class.java))
            }
            .addOnFailureListener { error -> callback.onError(error) }
    }

    /**
     * US 02.04.06: loads the campsite's current lottery (see [CurrentLottery]).
     *
     * @param campsiteId the campsite's Firestore document ID.
     * @param callback onLoaded with the current lottery, onNotFound if the
     *   campsite has none, or onError if Firestore fails.
     */
    fun getLotteryForCampsite(campsiteId: String, callback: LoadCallback) {
        getLotteriesForCampsite(campsiteId, object : ListCallback {
            override fun onLoaded(lotteries: List<Lottery>) {
                val current = CurrentLottery.pick(lotteries)
                if (current == null) {
                    callback.onNotFound()
                } else {
                    callback.onLoaded(current)
                }
            }

            override fun onError(error: Exception) {
                callback.onError(error)
            }
        })
    }
}