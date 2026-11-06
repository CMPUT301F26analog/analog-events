package com.example.birchbark
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.installations.FirebaseInstallations;

/**
 * This class is designed for authentication purposes. In this class,
 * we utilize FirebaseInstallations, by using the 128-bit integer that
 * is created from the Firestore database when an application is first installed
 * on an android device. We will utilize the FID as the main authenticator.
 *
 * The class first checks if a FID exists for the installation and if it is in the
 * database.
 *
 * [checkForAccount] retrieves the FID and looks for the matching document in the
 * firestore `users` collection, and then reports the findings through [callback].
 *
 */

class Authenticator {

    private val access = FirebaseInstallations.getInstance()
    private val db = FirebaseFirestore.getInstance()

    /**
     * Receives the result of [callback].
     */
    interface Callback {
        // Account exists in db
        fun onAccountFound(fid: String)
        // Account does not exist in db
        fun onAccountGone(fid: String)
        // Fault
        fun onError(error: Exception)
    }

    /**
     * The function that actually checks to see if the account exists through
     * FID. Utilizes Firebase Installations and Firestore API callbacks.
     * Code is adapted from [2] in References.md
     *
     * @param callback receives the outcome of the check
     */
    fun checkForAccount(callback: Callback) {
        access.id.addOnCompleteListener { task ->

            if (!task.isSuccessful) {
                callback.onError(
                    task.exception ?: Exception("Unable to retrieved FID")
                )
                return@addOnCompleteListener
            }

            val fid = task.result

            db.collection("users").document(fid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        callback.onAccountFound(fid)
                    } else {
                        callback.onAccountGone(fid)
                    }
                }
                .addOnFailureListener { error ->
                    callback.onError(error)
                }
        }
    }
}
