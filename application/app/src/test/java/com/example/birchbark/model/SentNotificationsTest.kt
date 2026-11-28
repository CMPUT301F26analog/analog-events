package com.example.birchbark.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [SentNotifications]. Covers US 02.10.02 (view sent notifications).
 */
class SentNotificationsTest {

    private fun sentTo(recipient: String, body: String, time: Long, lotteryId: String? = "L1") = AppNotification(
        id = recipient + time, recipientId = recipient, senderId = "owner1",
        type = NotificationType.WAITLIST_MESSAGE, title = "Update", body = body,
        lotteryId = lotteryId, createdAt = time
    )

    @Test
    fun group_sameMessageToManyPeople_isOneEntry() {
        val sent = listOf(sentTo("a", "Bring bug spray", 100L), sentTo("b", "Bring bug spray", 100L), sentTo("c", "Bring bug spray", 100L))
        val grouped = SentNotifications.group(sent)
        assertEquals(1, grouped.size)
        assertEquals(3, grouped[0].recipientCount)
    }

    @Test
    fun group_differentMessages_areSeparate() {
        val sent = listOf(sentTo("a", "Bring bug spray", 100L), sentTo("a", "Trail is closed", 200L))
        assertEquals(2, SentNotifications.group(sent).size)
    }

    @Test
    fun group_newestFirst() {
        val sent = listOf(sentTo("a", "Old one", 100L), sentTo("a", "New one", 300L), sentTo("a", "Middle one", 200L))
        assertEquals(listOf("New one", "Middle one", "Old one"), SentNotifications.group(sent).map { it.body })
    }

    @Test
    fun lotteryLabel_usesCampsiteNames() {
        val lottery = Lottery(id = "L1", campsiteIds = listOf("c1", "c2"))
        val names = mapOf("c1" to "Whistlers Campground", "c2" to "Wapiti Campground")
        assertEquals("Lottery for Whistlers Campground, Wapiti Campground", SentNotifications.lotteryLabel(lottery, names))
    }

    @Test
    fun lotteryLabel_missingLottery_saysRemoved() {
        assertEquals("A lottery that was removed", SentNotifications.lotteryLabel(null, mapOf()))
    }

    @Test
    fun recipientsText_singularAndPlural() {
        assertEquals("Sent to 1 person", SentNotifications.recipientsText(1))
        assertEquals("Sent to 4 people", SentNotifications.recipientsText(4))
    }
}