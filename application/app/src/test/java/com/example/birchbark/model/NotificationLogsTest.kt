package com.example.birchbark.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [NotificationLogs]. Covers US 03.08.01 (review notification logs).
 */
class NotificationLogsTest {

    @Test
    fun newestFirst_sortsByDate() {
        val old = AppNotification(id = "a", createdAt = 100L)
        val newest = AppNotification(id = "b", createdAt = 300L)
        val middle = AppNotification(id = "c", createdAt = 200L)
        val sorted = NotificationLogs.newestFirst(listOf(old, newest, middle))
        assertEquals(listOf("b", "c", "a"), sorted.map { it.id })
    }

    @Test
    fun nameFor_knownUser_givesName() {
        val names = mapOf("fid1" to "Yash Rawat")
        assertEquals("Yash Rawat", NotificationLogs.nameFor("fid1", names))
    }

    @Test
    fun nameFor_unknownUser_saysUnknown() {
        assertEquals("Unknown user", NotificationLogs.nameFor("gone", mapOf()))
    }

    @Test
    fun nameFor_blankName_saysUnknown() {
        assertEquals("Unknown user", NotificationLogs.nameFor("fid1", mapOf("fid1" to "")))
    }

    @Test
    fun typeLabel_givesReadableLabel() {
        assertEquals("Lottery won", NotificationLogs.typeLabel(NotificationType.LOTTERY_WON))
        assertEquals("Waitlist message", NotificationLogs.typeLabel(NotificationType.WAITLIST_MESSAGE))
    }
}