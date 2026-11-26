package com.example.birchbark.model

import com.google.firebase.firestore.FirebaseFirestore

/**
 * Saves, loads and deletes [CampsiteImage] objects in the Firestore "images"
 * collection. The only class that talks to Firestore about photos.
 * US 02.01.02 (optional image), the image gallery, and the admin image
 * screens (US 03.06.01 browse uploaded images, US 03.03.01 remove images).
 *
 * Works the same way as [CampsiteRepository]. Firestore calls are
 * asynchronous, so no function here returns a result directly. Each one
 * reports back later through a [SaveCallback], [ListCallback] or [DeleteCallback].
 *
 * Outstanding issues. Deleting a campsite doesn't delete its photos yet.
 */
class ImageRepository {

    private val db = FirebaseFirestore.getInstance()

    companion object {
        /**
         * Biggest photo we'll save, in characters of Base64 text. Firestore
         * documents can't go over 1 MB, so this leaves room for the other fields.
         */
        const val MAX_IMAGE_SIZE = 900_000
    }

    /** Receives the result of [addImage]. Exactly one function is called. */
    interface SaveCallback {
        /**
         * The photo was saved.
         * @param imageId the Firestore document ID of the saved photo.
         */
        fun onSuccess(imageId: String)

        /**
         * The photo was empty or too big, so nothing was saved.
         * @param message what to show the owner.
         */
        fun onInvalid(message: String)

        /**
         * Firestore failed, e.g. no network or no permission. Nothing was saved.
         * @param error the exception Firestore reported.
         */
        fun onError(error: Exception)
    }

    /** Receives the result of [getImagesForCampsite] or [getAllImages]. Exactly one function is called. */
    interface ListCallback {
        /**
         * The photos were loaded.
         * @param images the photos, oldest first. Empty if there aren't any.
         */
        fun onLoaded(images: List<CampsiteImage>)

        /**
         * Firestore failed, e.g. no network or no permission.
         * @param error the exception Firestore reported.
         */
        fun onError(error: Exception)
    }

    /** Receives the result of [deleteImage]. Exactly one function is called. */
    interface DeleteCallback {
        /** The photo was deleted. */
        fun onDeleted()

        /**
         * Firestore failed, e.g. no network or no permission. Nothing was deleted.
         * @param error the exception Firestore reported.
         */
        fun onError(error: Exception)
    }

    /**
     * US 02.01.02: saves a new photo for a campsite. Gives it a new document
     * ID and the current time before saving.
     *
     * @param image the photo to save. Needs campsiteId, ownerId and data filled in.
     * @param callback receives the new photo's ID, an invalid result, or an error.
     */
    fun addImage(image: CampsiteImage, callback: SaveCallback) {
        if (image.data.isBlank()) {
            callback.onInvalid("Couldn't read that photo, try a different one")
            return
        }
        if (image.data.length > MAX_IMAGE_SIZE) {
            callback.onInvalid("That photo is too big, try a different one")
            return
        }

        val ref = db.collection("images").document()
        val toSave = image.copy(id = ref.id, createdAt = System.currentTimeMillis())
        ref.set(toSave)
            .addOnSuccessListener { callback.onSuccess(ref.id) }
            .addOnFailureListener { error -> callback.onError(error) }
    }

    /**
     * US 02.01.02, US 01.01.05: loads every photo of one campsite, oldest
     * first, so the first photo the owner added is the main one.
     *
     * Sorted here instead of in the query so Firestore doesn't need an extra index.
     *
     * @param campsiteId the campsite's Firestore document ID ([Campsite.id]).
     * @param callback receives the photos, or an error.
     */
    fun getImagesForCampsite(campsiteId: String, callback: ListCallback) {
        db.collection("images").whereEqualTo("campsiteId", campsiteId).get()
            .addOnSuccessListener { result ->
                val images = result.toObjects(CampsiteImage::class.java)
                callback.onLoaded(images.sortedBy { it.createdAt })
            }
            .addOnFailureListener { error -> callback.onError(error) }
    }

    /**
     * US 03.06.01: loads every uploaded photo in the app, for the admin.
     *
     * @param callback receives all photos (oldest first), or an error.
     */
    fun getAllImages(callback: ListCallback) {
        db.collection("images").get()
            .addOnSuccessListener { result ->
                val images = result.toObjects(CampsiteImage::class.java)
                callback.onLoaded(images.sortedBy { it.createdAt })
            }
            .addOnFailureListener { error -> callback.onError(error) }
    }

    /**
     * US 03.03.01, US 02.01.03: deletes one photo. Used by the owner to remove
     * a photo from their gallery, and by the admin to remove any photo.
     *
     * @param imageId the photo's Firestore document ID ([CampsiteImage.id]).
     * @param callback told when it's deleted, or about an error.
     */
    fun deleteImage(imageId: String, callback: DeleteCallback) {
        db.collection("images").document(imageId).delete()
            .addOnSuccessListener { callback.onDeleted() }
            .addOnFailureListener { error -> callback.onError(error) }
    }
}