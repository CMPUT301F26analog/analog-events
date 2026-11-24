package com.example.birchbark.model

// See #048 in LLM.md

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

/**
 * US 01.10.01, 02.10.01, 02.10.02, 02.12.01, 03.08.01, 03.12.01: saves and loads
 * [AppNotification]s in the top-level Firestore "notifications" collection, and
 * each user's opt-out choice in "notificationPrefs/{fid}".
 *
 * A user with no notificationPrefs document has notifications on, so new users
 * need nothing created for them.
 *
 * Firestore calls are asynchronous, so every function reports back through
 * onResult/onFailure instead of returning a value.
 */
class NotificationRepository {

    private val db = FirebaseFirestore.getInstance()

    // US 01.10.01: opting out

    /**
     * Loads whether a user receives notifications.
     *
     * @param fid the user's FID.
     * @param onResult true if notifications are on (also when they never chose).
     */
    fun isEnabled(
        fid: String,
        onResult: (Boolean) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("notificationPrefs").document(fid).get()
            .addOnSuccessListener { document -> onResult(document.getBoolean("enabled") ?: true) }
            .addOnFailureListener { error -> onFailure(error) }
    }

    /**
     * Saves whether a user receives notifications. Only the user can change
     * their own choice (Firestore rules).
     *
     * @param fid the user's FID.
     * @param enabled false to opt out.
     */
    fun setEnabled(
        fid: String,
        enabled: Boolean,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("notificationPrefs").document(fid).set(mapOf("enabled" to enabled))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { error -> onFailure(error) }
    }

    /**
     * Finds which of these users opted out, so senders can skip them.
     *
     * @param fids the FIDs to check. Duplicates are fine.
     * @param onResult the FIDs that turned notifications off.
     */
    fun getOptedOut(
        fids: List<String>,
        onResult: (Set<String>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val unique = fids.distinct()
        if (unique.isEmpty()) {
            onResult(emptySet())
            return
        }
        val reads = unique.map { fid -> db.collection("notificationPrefs").document(fid).get() }
        Tasks.whenAllSuccess<DocumentSnapshot>(reads)
            .addOnSuccessListener { documents ->
                onResult(documents.filter { it.getBoolean("enabled") == false }.map { it.id }.toSet())
            }
            .addOnFailureListener { error -> onFailure(error) }
    }

    // Sending (US 02.10.01, 02.12.01, 01.05.01, 01.07.02)

    /**
     * Saves notifications, skipping anyone who opted out. A notification with
     * a blank [AppNotification.id] gets a new Firestore ID; one with an ID
     * overwrites that document, so resending it can't create a duplicate.
     *
     * @param notifications the notifications to send, one per recipient.
     * @param onResult how many were actually sent after skipping opted-out users.
     */
    fun send(
        notifications: List<AppNotification>,
        onResult: (Int) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (notifications.isEmpty()) {
            onResult(0)
            return
        }
        getOptedOut(
            notifications.map { it.recipientId },
            onResult = { optedOut ->
                val toSend = notifications.filter { it.recipientId !in optedOut }
                if (toSend.isEmpty()) {
                    onResult(0)
                } else {
                    val collection = db.collection("notifications")
                    //a batch holds at most 500 writes
                    val commits = toSend.chunked(500).map { chunk ->
                        val batch = db.batch()
                        for (notification in chunk) {
                            val ref = if (notification.id.isBlank()) collection.document()
                            else collection.document(notification.id)
                            batch.set(ref, notification.copy(id = ref.id))
                        }
                        batch.commit()
                    }
                    Tasks.whenAll(commits)
                        .addOnSuccessListener { onResult(toSend.size) }
                        .addOnFailureListener { error -> onFailure(error) }
                }
            },
            onFailure = onFailure
        )
    }

    // Reading

    /**
     * US 01.03.01, 01.03.02: an entrant's inbox, newest first.
     *
     * @param recipientId the entrant's FID.
     */
    fun getInbox(
        recipientId: String,
        onResult: (List<AppNotification>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        loadWhere("recipientId", recipientId, onResult, onFailure)
    }

    /**
     * US 02.10.02, 03.12.01: everything a user sent, newest first.
     *
     * @param senderId the sender's FID, usually an owner.
     */
    fun getSentBy(
        senderId: String,
        onResult: (List<AppNotification>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        loadWhere("senderId", senderId, onResult, onFailure)
    }

    /**
     * US 03.08.01: every notification in the app, newest first. Admins only
     * (Firestore rules refuse anyone else).
     */
    fun getAll(
        onResult: (List<AppNotification>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("notifications").get()
            .addOnSuccessListener { result ->
                onResult(result.toObjects(AppNotification::class.java).sortedByDescending { it.createdAt })
            }
            .addOnFailureListener { error -> onFailure(error) }
    }

    /**
     * Marks a notification as read. Only its recipient can do this.
     *
     * @param id the notification's document ID ([AppNotification.id]).
     */
    fun markRead(
        id: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("notifications").document(id).update("read", true)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { error -> onFailure(error) }
    }

    //sorted in Kotlin rather than with orderBy, so Firestore doesn't need a composite index
    private fun loadWhere(
        field: String,
        value: String,
        onResult: (List<AppNotification>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("notifications").whereEqualTo(field, value).get()
            .addOnSuccessListener { result ->
                onResult(result.toObjects(AppNotification::class.java).sortedByDescending { it.createdAt })
            }
            .addOnFailureListener { error -> onFailure(error) }
    }
}
