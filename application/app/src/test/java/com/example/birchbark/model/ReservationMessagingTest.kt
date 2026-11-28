package com.example.birchbark.model

import org.junit.Assert.*
import org.junit.Test

/**
 * US 01.14.01 and US 02.12.01
 * JUnit 4 tests for booking eligibility and inbox receipts.
 * Exercises the Kotlin models directly. The rejection-notice test also checks the sender used for
 * an invited owner’s booking.
 */
class ReservationMessagingTest {
    private val booking = Reservation(campsiteId = "site-b", status = ReservationStatus.CONFIRMED,
        startDate = 100L, endDate = 200L)

    // A confirmed upcoming stay can send through the last instant before checkout and must have a selected campsite.
    @Test fun confirmedUpcomingStayCanSendUntilExclusiveCheckout() {
        assertTrue(booking.acceptsMessages(50))
        assertTrue(booking.acceptsMessages(199))
        assertFalse(booking.acceptsMessages(200))
        assertFalse(booking.copy(campsiteId = null).acceptsMessages(100))
    }

    // Only CONFIRMED passes the eligibility check across all reservation statuses.
    @Test fun onlyConfirmedBookingsCanSend() {
        for (status in ReservationStatus.values()) {
            assertEquals(status == ReservationStatus.CONFIRMED, booking.copy(status = status).acceptsMessages(150))
        }
    }

    // An incoming message is unread until its ID matches the receipt. The sender sees their own message as read.
    @Test fun receiptBelongsToTheLatestIncomingMessage() {
        val summary = ConversationSummary("stay", "Site B", "Guest", "Hello", 1, "new", "owner-b", "old")
        assertTrue(summary.unread("guest"))
        assertFalse(summary.unread("owner-b"))
        assertFalse(summary.copy(seenId = "new").unread("guest"))
    }
    // A rejection from the selected site's owner uses that owner's FID in the existing notification.
    @Test fun invitedOwnersRejectionIdentifiesTheActingOwner() {
        val reserved = booking.copy(ownerId = "organizer", entrantId = "guest")
        val notice = RejectionNotice.build(reserved, "Jasper", 150, senderId = "selected-owner")
        assertEquals("selected-owner", notice.senderId)
        assertEquals("guest", notice.recipientId)
        assertEquals(NotificationType.RESERVATION_CANCELLED, notice.type)
    }
}