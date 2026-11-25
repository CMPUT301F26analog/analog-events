package com.example.birchbark

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
/**
 * Stores a block record for a violating owner and removes their profile.
 *
 * The transaction checks that the profile exists and includes the `owner` role.
 * It reads the profile's Firebase Authentication UID (`authUid`) and checks
 * that any matching `authLinks/{authUid}` document points to the supplied FID.
 * It then writes `authUid`, `fid`, and a server timestamp to
 * `blockedOwners/{authUid}`, deletes `users/{fid}`, and deletes the matching
 * auth link if one exists.
 *
 * @param db Firestore instance used for the transaction.
 */
class ViolatingOwnerRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    /**
     * Blocks an owner and removes their profile in a Firestore transaction.
     *
     * @param fid Firebase Installation ID and document ID in the `users`
     * collection for the owner profile to remove.
     * @param onSuccess Called after the transaction completes successfully.
     * @param onError Called with the exception if validation or the transaction
     * fails.
     */
    fun blockOwnerAndRemoveProfile(
        fid: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val userRef = db.collection("users").document(fid)

        db.runTransaction { transaction ->
            val user = transaction.get(userRef)

            if (!user.exists()) {
                throw IllegalStateException("The profile no longer exists.")
            }

            val roles = (user.get("roles") as? List<*>)
                ?.filterIsInstance<String>()
                .orEmpty()

            if (roles.none { it.equals("owner", ignoreCase = true) }) {
                throw IllegalStateException("This profile is not an owner.")
            }

            val authUid = user.getString("authUid")
                ?.takeIf { it.isNotBlank() }
                ?: throw IllegalStateException(
                    "The owner profile does not have an authUid."
                )

            val authLinkRef = db.collection("authLinks").document(authUid)
            val authLink = transaction.get(authLinkRef)

            if (authLink.exists() && authLink.getString("fid") != fid) {
                throw IllegalStateException(
                    "The owner's auth link points to a different profile."
                )
            }

            val blockedOwnerRef =
                db.collection("blockedOwners").document(authUid)


            transaction.set(
                blockedOwnerRef,
                mapOf(
                    "authUid" to authUid,
                    "fid" to fid,
                    "blockedAt" to FieldValue.serverTimestamp()
                ))
            transaction.delete(userRef)

            if (authLink.exists()) {
                transaction.delete(authLinkRef) }

            true
        }
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { error -> onError(error) } } }