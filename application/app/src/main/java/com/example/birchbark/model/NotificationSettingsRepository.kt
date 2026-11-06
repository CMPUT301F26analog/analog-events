
package com.example.birchbark.model
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

/**
 * Handles saving and loading notification preferences
 * for an authenticated user.
 */
class NotificationSettingsRepository {
    private val db = FirebaseFirestore.getInstance()

    /**
     * Loads the user's notification preference.
     *
     * @param uid Firebase Authentication user ID.
     * @param onSuccess Returns the saved preference.
     * @param onFailure Returns a Firestore error.
     */
    fun loadPreference(
        uid: String,
        onSuccess: (Boolean) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                val enabled =
                    document.getBoolean("notificationsEnabled") ?: true

                onSuccess(enabled)
            }
            .addOnFailureListener { error ->
                onFailure(error)
            }
    }

    /**
     * Saves the user's notification preference.
     *
     * @param uid Firebase Authentication user ID.
     * @param enabled Whether notifications are enabled.
     * @param onSuccess Called after a successful save.
     * @param onFailure Called when saving fails.
     */
    fun savePreference(
        uid: String,
        enabled: Boolean,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("users")
            .document(uid)
            .set(
                mapOf("notificationsEnabled" to enabled),
                SetOptions.merge()
            )
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { error ->
                onFailure(error)
            }
    }
}
