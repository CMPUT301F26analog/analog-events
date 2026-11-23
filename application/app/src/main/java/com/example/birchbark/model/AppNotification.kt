package com.example.birchbark.model

// See #048 in LLM.md

/**
 * US 01.03.01, 01.03.02, 02.10.01, 02.10.02, 03.08.01: an in-app notification,
 * as stored in the top-level Firestore "notifications" collection. One document
 * per recipient. Plain data only, like [Lottery].
 *
 * Named AppNotification so it doesn't clash with Android's own
 * android.app.Notification class.
 *
 * @property id Firestore document ID.
 * @property recipientId FID of the user who receives it (their inbox).
 * @property senderId FID of the user who sent it. Used for an owner's sent
 *   notifications (US 02.10.02) and message logs (US 03.12.01).
 * @property type what the notification is about.
 * @property title short heading for the inbox list.
 * @property body the full message.
 * @property lotteryId the lottery it is about, or null if it isn't about one.
 * @property createdAt when it was sent (epoch ms).
 * @property read true once the recipient has opened it.
 */
data class AppNotification(
    val id: String = "",
    val recipientId: String = "",
    val senderId: String = "",
    val type: NotificationType = NotificationType.OWNER_MESSAGE,
    val title: String = "",
    val body: String = "",
    val lotteryId: String? = null,
    val createdAt: Long = 0L,
    val read: Boolean = false,
)
