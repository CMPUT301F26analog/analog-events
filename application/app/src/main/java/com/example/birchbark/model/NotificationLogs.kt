package com.example.birchbark.model

/**
 * US 03.08.01 Review notification logs. Turns saved notifications into what
 * the admin's log screen shows. No Firestore in here so it can be unit tested.
 *
 * Outstanding issues. None.
 */
object NotificationLogs {

    /**
     * Sorts notifications so the newest one is first.
     *
     * @param notifications the notifications to sort.
     * @return a new list, newest first.
     */
    fun newestFirst(notifications: List<AppNotification>): List<AppNotification> {
        return notifications.sortedByDescending { it.createdAt }
    }

    /**
     * Finds a user's name to show instead of their ID.
     *
     * @param fid the user's FID.
     * @param names every user's name, by FID.
     * @return their name, or "Unknown user" if they aren't in [names] (like a deleted profile).
     */
    fun nameFor(fid: String, names: Map<String, String>): String {
        val name = names[fid]
        return if (name.isNullOrBlank()) "Unknown user" else name
    }

    /**
     * A short label for what kind of notification it was.
     *
     * @param type the notification's type.
     * @return the label shown on the log, like "Lottery won".
     */
    fun typeLabel(type: NotificationType): String {
        return when (type) {
            NotificationType.LOTTERY_WON -> "Lottery won"
            NotificationType.LOTTERY_LOST -> "Lottery lost"
            NotificationType.WAITLIST_MESSAGE -> "Waitlist message"
            NotificationType.RESERVATION_CONFIRMED -> "Reservation confirmed"
            NotificationType.RESERVATION_CANCELLED -> "Reservation cancelled"
            NotificationType.OWNER_MESSAGE -> "Owner message"
            NotificationType.OWNER_REMOVED -> "Owner removed"
        }

    }
}