package com.example.birchbark.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [WaitlistMessage]. Covers US 02.10.01 (notify waitlist entrants).
 */
class WaitlistMessageTest {

    private val lottery = Lottery(id = "L1", ownerId = "owner1")
    private val waitlist = listOf("e1", "e2", "e3")

    @Test
    fun goodMessage_canBeSent() {
        assertNull(WaitlistMessage.check("Gates open at 9am", waitlist))
    }

    @Test
    fun blankMessage_cantBeSent() {
        assertEquals("Write a message first", WaitlistMessage.check("   ", waitlist))
    }

    @Test
    fun tooLongMessage_cantBeSent() {
        val long = "a".repeat(WaitlistMessage.MAX_LENGTH + 1)
        assertTrue(WaitlistMessage.check(long, waitlist) != null)
    }

    @Test
    fun emptyWaitlist_cantSend() {
        assertEquals("Nobody is on the waitlist yet", WaitlistMessage.check("Hi", emptyList()))
    }

    @Test
    fun build_oneNotificationPerEntrant() {
        val built = WaitlistMessage.build(lottery, waitlist, "  Gates open at 9am ", 1000L)
        assertEquals(listOf("e1", "e2", "e3"), built.map { it.recipientId })
        val first = built.first()
        assertEquals("owner1", first.senderId)
        assertEquals(NotificationType.WAITLIST_MESSAGE, first.type)
        assertEquals("Gates open at 9am", first.body)
        assertEquals("L1", first.lotteryId)
        assertEquals(1000L, first.createdAt)
    }

    @Test
    fun build_skipsDuplicateEntrants() {
        val built = WaitlistMessage.build(lottery, listOf("e1", "e1", "e2"), "Hi", 0L)
        assertEquals(listOf("e1", "e2"), built.map { it.recipientId })
    }

    @Test
    fun sentLabel_saysWhenSomeoneOptedOut() {
        assertEquals("Sent to all 3 entrants", WaitlistMessage.sentLabel(3, 3))
        assertEquals("Sent to 2 of 3 entrants, the rest turned notifications off", WaitlistMessage.sentLabel(2, 3))
    }
}