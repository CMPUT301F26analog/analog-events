package com.example.birchbark.model

import com.google.firebase.firestore.FirebaseFirestore

/**
 * Saves and loads Lottery objects in the Firestore "lotteries" collection.
 * The only class that talks to Firestore about lotteries.
 */
class LotteryRepository {

    private val db = FirebaseFirestore.getInstance()

    /** Results of a save (create or update). */
    interface SaveCallback {
        fun onSuccess(lotteryId: String)
        fun onInvalid(errors: List<String>)   // validation failed, nothing saved
        fun onError(error: Exception)          // network/permission problem
    }

    /** Results of loading one lottery. */
    interface LoadCallback {
        fun onLoaded(lottery: Lottery)
        fun onNotFound()
        fun onError(error: Exception)
    }

    /** Loads one lottery by its document ID. */
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

    /** US 02.04.01: saves a new lottery. Refuses to save if invalid. */
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

    /** US 02.04.06: saves changes to an existing lottery. Refuses after the draw. */
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
}