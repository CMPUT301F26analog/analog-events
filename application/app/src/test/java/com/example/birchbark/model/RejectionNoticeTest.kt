package com.example.birchbark.model

import org.junit.Assert.assertEquals
import org.junit.Test

// See #058 in LLM.md
/** Unit tests for the notification sent when an owner rejects a reservation. US 02.08.01. */
class RejectionNoticeTest {

    private val reservation = Reservation(id = "L1_E1", lotteryId = "L1", entrantId = "E1", ownerId = "O1")

    @Test
    fun goesToEntrantFromOwner() {
        val notice = RejectionNotice.build(reservation, "Whistlers", 5L)
        assertEquals("E1", notice.recipientId)
        assertEquals("O1", notice.senderId)
        assertEquals("L1_E1_rejected", notice.id)
    }

    @Test
    fun namesCampsiteWhenKnown() {
        assertEquals("The owner rejected your reservation at Whistlers.", RejectionNotice.build(reservation, "Whistlers", 5L).body)
    }

    @Test
    fun noNameWhenUnknown() {
        assertEquals("The owner rejected your reservation.", RejectionNotice.build(reservation, null, 5L).body)
    }
}
