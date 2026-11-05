package com.example.birchbark.model

import com.google.firebase.firestore.FirebaseFirestore

/**
 * Saves and loads [Campsite] objects in the Firestore "campsites" collection.
 * The only class that talks to Firestore about campsites.
 *
 * Firestore calls are asynchronous, so no function here returns a result
 * directly. Each one reports back later through a [SaveCallback],
 * [LoadCallback], [ListCallback] or [DeleteCallback].
 *
 * Outstanding issues. Nothing checks that the person updating a campsite is
 * actually its owner yet, the screens just only show an owner their own campsites.
 * Deleting a campsite doesn't remove it from any lottery that lists it yet.
 */
class CampsiteRepository {

    private val db = FirebaseFirestore.getInstance()

    /** Receives the result of [createCampsite] or [updateCampsite]. Exactly one function is called. */
    interface SaveCallback {
        /**
         * The campsite was saved.
         * @param campsiteId the Firestore document ID of the saved campsite.
         */
        fun onSuccess(campsiteId: String)

        /**
         * The campsite broke a rule, so nothing was saved.
         * @param errors messages to show the owner, one per problem.
         */
        fun onInvalid(errors: List<String>)

        /**
         * Firestore failed, e.g. no network or no permission. Nothing was saved.
         * @param error the exception Firestore reported.
         */
        fun onError(error: Exception)
    }

    /** Receives the result of [getCampsite]. Exactly one function is called. */
    interface LoadCallback {
        /**
         * The campsite was found.
         * @param campsite the loaded campsite.
         */
        fun onLoaded(campsite: Campsite)

        /** No campsite exists with that ID. */
        fun onNotFound()

        /**
         * Firestore failed, e.g. no network or no permission.
         * @param error the exception Firestore reported.
         */
        fun onError(error: Exception)
    }

    /** Receives the result of [getCampsitesByOwner] or [getAllCampsites]. Exactly one function is called. */
    interface ListCallback {
        /**
         * The campsites were loaded.
         * @param campsites the campsites, sorted by name. Empty if there aren't any.
         */
        fun onLoaded(campsites: List<Campsite>)

        /**
         * Firestore failed, e.g. no network or no permission.
         * @param error the exception Firestore reported.
         */
        fun onError(error: Exception)
    }

    /** Receives the result of [deleteCampsite]. Exactly one function is called. */
    interface DeleteCallback {
        /** The campsite was deleted. */
        fun onDeleted()

        /**
         * Firestore failed, e.g. no network or no permission. Nothing was deleted.
         * @param error the exception Firestore reported.
         */
        fun onError(error: Exception)
    }

    /**
     * US 02.01.04, US 01.01.05: loads one campsite by its document ID.
     *
     * @param id the campsite's Firestore document ID ([Campsite.id]).
     * @param callback receives the campsite, a not-found result, or an error.
     */
    fun getCampsite(id: String, callback: LoadCallback) {
        db.collection("campsites").document(id).get()
            .addOnSuccessListener { document ->
                val campsite = document.toObject(Campsite::class.java)
                if (campsite == null) {
                    callback.onNotFound()
                } else {
                    callback.onLoaded(campsite)
                }
            }
            .addOnFailureListener { error -> callback.onError(error) }
    }

    /**
     * US 02.03.01: loads every campsite one owner registered. Owner only sees
     * the campsites they registered.
     *
     * @param ownerId the owner's FID ([Campsite.ownerId]).
     * @param callback receives the list (sorted by name), or an error.
     */
    fun getCampsitesByOwner(ownerId: String, callback: ListCallback) {
        db.collection("campsites").whereEqualTo("ownerId", ownerId).get()
            .addOnSuccessListener { result ->
                val campsites = result.toObjects(Campsite::class.java)
                callback.onLoaded(campsites.sortedBy { it.name.lowercase() })
            }
            .addOnFailureListener { error -> callback.onError(error) }
    }

    /**
     * US 01.01.03, US 03.04.01: loads every campsite in the app, for the
     * entrant browse list and the admin browse screen. To search, filter the
     * result with [Campsite.matchesSearch].
     *
     * @param callback receives the list (sorted by name), or an error.
     */
    fun getAllCampsites(callback: ListCallback) {
        db.collection("campsites").get()
            .addOnSuccessListener { result ->
                val campsites = result.toObjects(Campsite::class.java)
                callback.onLoaded(campsites.sortedBy { it.name.lowercase() })
            }
            .addOnFailureListener { error -> callback.onError(error) }
    }

    /**
     * US 02.01.01: saves a new campsite. Runs [CampsiteValidator] first and
     * refuses to save an invalid campsite. Firestore generates the ID, which is
     * also stored inside the saved campsite's [Campsite.id].
     *
     * @param campsite the new campsite. Its [Campsite.id] is ignored and replaced.
     * @param callback onSuccess with the new ID, onInvalid with the rule
     *   messages, or onError if Firestore fails.
     */
    fun createCampsite(campsite: Campsite, callback: SaveCallback) {

        //checking rules before touching DB
        val errors = CampsiteValidator.validate(campsite)
        if (errors.isNotEmpty()) {
            callback.onInvalid(errors)
            return
        }

        val ref = db.collection("campsites").document()
        val toSave = campsite.copy(id = ref.id)

        ref.set(toSave)
            .addOnSuccessListener {
                callback.onSuccess(ref.id)
            }
            .addOnFailureListener { error ->
                callback.onError(error)
            }
    }

    /**
     * US 02.01.03: saves changes to an existing campsite, overwriting the stored
     * version. Refuses if the campsite has no ID yet or breaks a rule
     * ([CampsiteValidator]).
     *
     * @param campsite the edited campsite. Must have the [Campsite.id] it was saved with.
     * @param callback onSuccess with the campsite's ID, onInvalid with the reason
     *   it was refused, or onError if Firestore fails.
     */
    fun updateCampsite(campsite: Campsite, callback: SaveCallback) {
        //no ID means there's no saved campsite to update
        if (campsite.id.isBlank()) {
            callback.onInvalid(listOf("Campsite has not been saved yet"))
            return
        }

        //checking rules before touching DB
        val errors = CampsiteValidator.validate(campsite)
        if (errors.isNotEmpty()) {
            callback.onInvalid(errors)
            return
        }

        //overwrite the existing document
        db.collection("campsites").document(campsite.id).set(campsite)
            .addOnSuccessListener {
                callback.onSuccess(campsite.id)
            }
            .addOnFailureListener { error ->
                callback.onError(error)
            }
    }

    /**
     * US 03.01.01: deletes a campsite from Firebase so entrants can't see it
     * anymore. The screen should ask the admin to confirm before calling this.
     *
     * @param id the campsite's Firestore document ID ([Campsite.id]).
     * @param callback onDeleted once it's gone, or onError if Firestore fails.
     */
    fun deleteCampsite(id: String, callback: DeleteCallback) {
        db.collection("campsites").document(id).delete()
            .addOnSuccessListener {
                callback.onDeleted()
            }
            .addOnFailureListener { error ->
                callback.onError(error)
            }
    }
}