package com.example.birchbark.model

import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

// See [TBD 4] in LLM.md

/**
 * Saves and removes an entrant's profile image. The image is a small square
 * JPEG ([com.example.birchbark.util.ProfileImageCodec]) stored as a [Blob] in
 * the "profileImage" field of their document in the "users" collection, so it's
 * deleted along with the profile (US 01.08.01) and an admin can remove it by
 * calling [removeImage] (US 03.03.01).
 *
 * The only class that talks to Firestore about profile images. If images move to
 * Firebase Storage later, only this class and the type of
 * [UserProfile.profileImage] change.
 *
 * It only changes the one field, never the rest of the profile, so it can't
 * overwrite a name or email edit.
 *
 * Firestore calls are asynchronous, so no function here returns a result
 * directly. Each one reports back later through a [SaveCallback] or [RemoveCallback].
 *
 */
class ProfileImageRepository {

    //created on first use, so checking an invalid image never needs Firebase (and can be unit tested)
    private val db by lazy { FirebaseFirestore.getInstance() }

    /** Receives the result of [setImage]. Exactly one function is called. */
    interface SaveCallback {
        /**
         * The image was saved.
         * @param image the saved JPEG bytes, to show on screen.
         */
        fun onSaved(image: ByteArray)

        /**
         * The image broke a rule, so nothing was saved.
         * @param errors messages to show the entrant, one per problem.
         */
        fun onInvalid(errors: List<String>)

        /**
         * Firestore failed, e.g. no network, no permission, or the profile doesn't exist.
         * Nothing was saved.
         * @param error the exception Firestore reported.
         */
        fun onError(error: Exception)
    }

    /** Receives the result of [removeImage]. Exactly one function is called. */
    interface RemoveCallback {
        /** The image was removed (or there wasn't one). */
        fun onRemoved()

        /**
         * Firestore failed, e.g. no network, no permission, or the profile doesn't exist.
         * @param error the exception Firestore reported.
         */
        fun onError(error: Exception)
    }

    /**
     * US 01.08.02: adds an entrant's profile image, or replaces the one they have.
     * Runs [ProfileImageValidator] first and refuses to save an invalid image.
     *
     * @param fid the entrant's FID, i.e. their document ID in the "users" collection.
     * @param image the small JPEG from [com.example.birchbark.util.ProfileImageCodec.encode].
     * @param callback onSaved with the image, onInvalid with the messages, or onError if Firestore fails.
     */
    fun setImage(fid: String, image: ByteArray, callback: SaveCallback) {

        //checking rules before touching DB
        val errors = ProfileImageValidator.validate(fid, image)
        if (errors.isNotEmpty()) {
            callback.onInvalid(errors)
            return
        }

        //update() changes just this field, and fails if the profile doesn't exist
        db.collection(USERS).document(fid).update(FIELD, Blob.fromBytes(image))
            .addOnSuccessListener { callback.onSaved(image) }
            .addOnFailureListener { error -> callback.onError(error) }
    }

    /**
     * US 01.08.02: removes an entrant's profile image, so their initials show again.
     * Also what an admin uses to remove an image (US 03.03.01).
     *
     * @param fid the entrant's FID, i.e. their document ID in the "users" collection.
     * @param callback onRemoved once it's gone, or onError if Firestore fails.
     */
    fun removeImage(fid: String, callback: RemoveCallback) {
        if (fid.isBlank()) {
            callback.onError(IllegalArgumentException("Profile is required"))
            return
        }

        db.collection(USERS).document(fid).update(FIELD, FieldValue.delete())
            .addOnSuccessListener { callback.onRemoved() }
            .addOnFailureListener { error -> callback.onError(error) }
    }

    private companion object {
        const val USERS = "users"
        const val FIELD = "profileImage"
    }
}
