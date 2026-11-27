package com.example.birchbark.model

/**
 * US 02.10.01: checks an owner's message to their lottery waitlist and builds
 * one notification per entrant. Pure logic with no Firestore, so it can be
 * unit tested. [NotificationRepository.send] skips anyone who opted out
 * (US 01.10.01) and saves the rest in Firestore.
 *
 * Outstanding issues. None.
 */
object WaitlistMessage {

    /** The longest message an owner can send. */
    const val MAX_LENGTH = 500

    /** The heading every waitlist message gets in the entrant's notifications. */
    const val TITLE = "Message about your campsite lottery"

    /**
     * Checks if the message can be sent.
     *
     * @param text what the owner typed.
     * @param entrantIds FIDs of everyone on the waitlist.
     * @return what's wrong, or null if it can be sent.
     */
    fun check(text: String, entrantIds: List<String>): String? {
        val message = text.trim()
        return when {
            entrantIds.isEmpty() -> "Nobody is on the waitlist yet"
            message.isEmpty() -> "Write a message first"
            message.length > MAX_LENGTH -> "Keep the message to $MAX_LENGTH characters or less"
            else -> null
        }
    }

    /**
     * Builds one notification for each entrant on the waitlist. They have no
     * ID yet, so Firestore gives each a new one and an owner can send more
     * than one message.
     *
     * @param lottery the lottery whose waitlist gets the message. Its owner is the sender.
     * @param entrantIds FIDs of everyone on the waitlist. Duplicates are skipped.
     * @param text the message, already checked by [check].
     * @param now when it was sent, in epoch ms.
     * @return the notifications to send, in the same order as [entrantIds].
     */
    fun build(lottery: Lottery, entrantIds: List<String>, text: String, now: Long): List<AppNotification> {
        val message = text.trim()
        return entrantIds.distinct().map { entrantId ->
            AppNotification(
                recipientId = entrantId,
                senderId = lottery.ownerId,
                type = NotificationType.WAITLIST_MESSAGE,
                title = TITLE,
                body = message,
                lotteryId = lottery.id,
                createdAt = now,
            )
        }
    }

    /**
     * What to tell the owner after sending, since people who opted out are skipped.
     *
     * @param sent how many notifications were actually saved.
     * @param total how many entrants are on the waitlist.
     * @return a short message for the owner.
     */
    fun sentLabel(sent: Int, total: Int): String {
        return when {
            sent == 0 -> "Nobody got it, everyone on the waitlist turned notifications off"
            sent < total -> "Sent to $sent of $total entrants, the rest turned notifications off"
            else -> "Sent to all $total entrants"
        }
    }
}