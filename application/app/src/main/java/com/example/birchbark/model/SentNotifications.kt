package com.example.birchbark.model

/**
 * US 02.10.02 View sent notifications. Turns the notifications an owner sent
 * into what their Sent Notifications screen shows. No Firestore in here so it
 * can be unit tested.
 *
 * Each notification is saved once per person who got it, so a message to a
 * waitlist of 10 is 10 notifications. The owner sent it once though, so
 * [group] puts those back together into one [SentMessage].
 *
 * Outstanding issues. None.
 */
object SentNotifications {

    /**
     * One message the owner sent, and how many people got it.
     *
     * @property type what kind of notification it was.
     * @property title the heading.
     * @property body the full message.
     * @property lotteryId the lottery it went to, or null if it wasn't about one.
     * @property createdAt when it was sent (epoch ms).
     * @property recipientCount how many people got it.
     */
    data class SentMessage(
        val type: NotificationType,
        val title: String,
        val body: String,
        val lotteryId: String?,
        val createdAt: Long,
        val recipientCount: Int
    )

    // See #TBD14 in LLM.md
    /**
     * Puts notifications that were sent together (same message, same lottery,
     * same time) into one [SentMessage], newest first.
     *
     * @param notifications everything the owner sent.
     * @return one entry per message, newest first.
     */
    fun group(notifications: List<AppNotification>): List<SentMessage> {
        return notifications
            .groupBy { listOf(it.type, it.title, it.body, it.lotteryId, it.createdAt) }
            .values
            .map { sameMessage ->
                val first = sameMessage.first()
                SentMessage(
                    type = first.type,
                    title = first.title,
                    body = first.body,
                    lotteryId = first.lotteryId,
                    createdAt = first.createdAt,
                    recipientCount = sameMessage.size
                )
            }
            .sortedByDescending { it.createdAt }
    }

    /**
     * Says which lottery a message went to, using the names of its campsites
     * since lotteries don't have names of their own.
     *
     * @param lottery the lottery, or null if it couldn't be found.
     * @param campsiteNames campsite names by ID.
     * @return like "Lottery for Whistlers Campground, Wapiti Campground".
     */
    fun lotteryLabel(lottery: Lottery?, campsiteNames: Map<String, String>): String {
        if (lottery == null) {
            return "A lottery that was removed"
        }
        val names = lottery.campsiteIds.mapNotNull { campsiteNames[it] }
        return if (names.isEmpty()) "A lottery" else "Lottery for " + names.joinToString(", ")
    }

    /**
     * Says how many people got a message.
     *
     * @param count how many people.
     * @return like "Sent to 1 person" or "Sent to 4 people".
     */
    fun recipientsText(count: Int): String {
        return if (count == 1) "Sent to 1 person" else "Sent to $count people"
    }
}