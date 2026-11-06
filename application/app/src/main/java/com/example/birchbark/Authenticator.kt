package com.example.birchbark

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.installations.FirebaseInstallations
import kotlin.jvm.java

/**
 * US 01.09.01 implemented.
 *
 * This class is designed for authentication purposes. In this class,
 * we utilize FirebaseInstallations, by using the 128-bit integer that
 * is created from the Firestore database when an application is first installed
 * on an android device. We will utilize the FID as the main authenticator.
 *
 * The class first checks if a FID exists for the installation and if it is in the
 * database.
 *
 * In version 0.2, it now utilizes the anonymous Firebase Authentication UID as
 * seen in References.md [ID].
 *
 * Note that going foward, the [FID] will not be the authentication credential,
 * but mainly act as a primary key for database collections.
 *
 * [checkForAccount] retrieves the FID and looks for the matching document in the
 * firestore `users` collection, and then reports the findings through [callback].
 *
 * Portions of the authenticator were generated with LLM, see LLM.md [014].
 *
 * References.md [13] and [14]
 *
 * @param auth The firebase anonymous ID for data operation purposes
 * @param db Passing the database parameter, inits handshake for firebase
 * @param installations Parameter callback instance to get FID and use it from firestore
 *
 */

class Authenticator(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val installations: FirebaseInstallations = FirebaseInstallations.getInstance()
) {
    // Callback interface for finding accounts based on FID in the firebase
    interface Callback {
        fun onAccountFound(profile: UserProfile)
        fun onRegistrationRequired(fid: String, authUid: String)
        fun onError(error: Exception)
    }

    /**
     * checkForAccount is as the name implies. It takes the parameters:
     * @param user passes in the current user based on auth ID
     *
     * If there was a valid user auth profile, it returns to load the FID
     * to gain the appropriate profile.
     *
     * If errors arise, it then logs out the previous session and reattempts
     * with the anonymous sign in.
     */
    fun checkForAccount(callback: Callback) {
        val user = auth.currentUser
        if (user != null && user.isAnonymous) {
            loadInstallation(user.uid, callback)
            return
        }
        // Retire the previous developer email/password session, if one exists.
        if (user != null) auth.signOut()
        auth.signInAnonymously()
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid.isNullOrBlank()) {
                    callback.onError(IllegalStateException("Anonymous sign-in returned no UID."))
                } else {
                    loadInstallation(uid, callback)
                }
            }
            .addOnFailureListener(callback::onError)
    }

    /**
     * The function utilizes listeners, talking to the database, snapshot, and callbacks.
     * Using the firebase built-in installations package, the function listens inside
     * the database collection users, matching for the FID and authUid. Once these are
     * found, it returns the user profile data. If not, exceptions are instantiated when
     * unable to access the database or the profile was bound to a different session.
     *
     * @param authUid authentication anonymous id
     * @param callback interface for returning result of handshake transactions
     *
     * Some portions were generated and assisted by LLM. See LLM.md [014]
     */
    private fun loadInstallation(authUid: String, callback: Callback) {
        installations.id
            .addOnSuccessListener { fid ->
                db.collection("users").document(fid).get()
                    .addOnSuccessListener { snapshot ->
                        // If successful, profile found and returned
                        if (!snapshot.exists()) {
                            callback.onRegistrationRequired(fid, authUid)
                            return@addOnSuccessListener
                        }
                        val profile = snapshot.toObject(UserProfile::class.java)
                        // Error reading profile or getting profile
                        if (profile == null) {
                            callback.onError(IllegalStateException("Unable to read user profile."))
                        } else if (profile.authUid != authUid) {
                            callback.onError(IllegalStateException(
                                "This installation's profile is bound to a different Firebase session. " +
                                        "Do not overwrite it; use a clean test installation or recover the original session."
                            ))
                        // Checking to see if registration is done
                        } else if (!profile.registrationComplete) {
                            callback.onRegistrationRequired(fid, authUid)
                        // Roles must be filled upon registration. Note admin role is done through firebase console.
                        } else if (profile.roles.isEmpty()) {
                            callback.onError(IllegalStateException("Profile has no assigned roles."))
                        } else {
                            callback.onAccountFound(profile)
                        }
                    }
                    .addOnFailureListener(callback::onError)
            }
            .addOnFailureListener(callback::onError)
    }
}