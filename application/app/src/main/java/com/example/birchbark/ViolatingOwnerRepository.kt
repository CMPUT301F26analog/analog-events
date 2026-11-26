package com.example.birchbark

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

/**
 * US 03.07.01 Blocks violating campsite owners and removes their profiles.
 * References.md 57 to 66
 * The owner's FID is used to find their profile in `users` and their campsites
 * through each campsite's `ownerId` field. Matching campsite documents are
 * deleted first. After those deletions succeed, the repository writes records
 * to `blockedOwners/{authUid}` and `blockedOwnerFids/{fid}`, then removes the
 * owner profile and matching auth link.
 *
 * @property db Firestore instance used to read and update the database.
 */
class ViolatingOwnerRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    /**
     * Deletes an owner's campsites, adds the owner to the block list, and
     * removes the owner's profile.
     *
     * Only profiles whose `roles` field contains `"owner"` can be blocked.
     * Campsites are matched by `ownerId == fid` and deleted before the profile
     * removal step begins.
     *
     * @param fid Firebase Installation ID and document ID of the owner profile
     * in the `users` collection.
     * @param onSuccess Called after the campsites, block records, and profile
     * operations succeed.
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
                            it.equals("owner", ignoreCase = true) }) {
                        throw IllegalStateException(
                            "This profile is not an owner.") }

                    val authUid = user.getString("authUid")
                        ?.takeIf { it.isNotBlank() }
                        ?: throw IllegalStateException(
                            "The owner profile does not have an authUid.")

                    db.collection("campsites")
                        .whereEqualTo("ownerId", fid)
                        .get()
                        .addOnSuccessListener { campsiteResult ->
                            val campsiteChunks =
                                campsiteResult.documents.chunked(500)

                            fun deleteNextCampsiteChunk(index: Int) {
                                if (index >= campsiteChunks.size) {
                                    blockAndRemoveProfile(
                                        fid,
                                        authUid,
                                        onSuccess,
                                        onError)
                                    return }
                                val batch = db.batch()
                                campsiteChunks[index].forEach { campsite ->
                                    batch.delete(campsite.reference) }
                                batch.commit()
                                    .addOnSuccessListener {
                                        deleteNextCampsiteChunk(index + 1) }
                                    .addOnFailureListener { error ->
                                        onError(error) } }

                            deleteNextCampsiteChunk(0) }
                        .addOnFailureListener { error ->
                            onError(error)
                        } } catch (error: Exception) {
                    onError(error) } }
            .addOnFailureListener { error ->
                onError(error) } }

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
        onError: (Exception) -> Unit) {
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
                    it.equals("owner", ignoreCase = true)
                }
            ) {
                throw IllegalStateException(
                    "This profile is not an owner.") }
            if (user.getString("authUid") != authUid) {
                throw IllegalStateException(
                    "The owner's authUid changed. Please try again.") }
            if (authLink.exists() && authLink.getString("fid") != fid) {
                throw IllegalStateException(
                    "The owner's auth link points to a different profile.") }
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
            .addOnFailureListener { error -> onError(error) } } }