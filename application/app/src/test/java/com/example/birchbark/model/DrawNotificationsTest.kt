package com.example.birchbark.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// See #048 in LLM.md
/**
 * Unit tests for the win and lose notifications built by the lottery draw.
 * Covers US 01.03.01, 01.03.02, 01.10.01.
 */
class DrawNotificationsTest {

    private val lottery = Lottery(id = "L1", ownerId = "O1", campsiteIds = listOf("C1"))
    private val entrants = listOf("E1", "E2", "E3")

    // US 01.03.01, 01.03.02: everyone on the waitlist hears the result

    @Test
    fun buildMakesOneNotificationPerEntrant() {
        val notifications = DrawNotifications.build(lottery, entrants, listOf("E2"), emptySet(), 1000L)
        assertEquals(entrants, notifications.map { it.recipientId })
    }

    @Test
    fun winnerGetsWonNotification() {
        val notifications = DrawNotifications.build(lottery, entrants, listOf("E2"), emptySet(), 1000L)
        assertEquals(NotificationType.LOTTERY_WON, notifications.single { it.recipientId == "E2" }.type)
    }

    @Test
    fun othersGetLostNotification() {
        val notifications = DrawNotifications.build(lottery, entrants, listOf("E2"), emptySet(), 1000L)
        assertTrue(notifications.filter { it.recipientId != "E2" }.all { it.type == NotificationType.LOTTERY_LOST })
    }

    @Test
    fun buildCopiesLotteryFields_andStartsUnread() {
        val notification = DrawNotifications.build(lottery, listOf("E1"), emptyList(), emptySet(), 1000L).single()
        assertEquals("L1_E1_draw", notification.id)
        assertEquals("O1", notification.senderId)
        assertEquals("L1", notification.lotteryId)
        assertEquals(1000L, notification.createdAt)
        assertFalse(notification.read)
    }

    @Test
    fun buildMakesNothing_whenWaitlistEmpty() {
        assertTrue(DrawNotifications.build(lottery, emptyList(), emptyList(), emptySet(), 1000L).isEmpty())
    }

    // US 01.10.01: opted-out entrants get nothing

    @Test
    fun buildSkipsOptedOutEntrants() {
        val notifications = DrawNotifications.build(lottery, entrants, listOf("E2"), setOf("E1", "E2"), 1000L)
        assertEquals(listOf("E3"), notifications.map { it.recipientId })
    }
}
