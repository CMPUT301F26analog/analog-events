package com.example.birchbark

import com.example.birchbark.ui.entrant.ReservationListItem
import com.example.birchbark.ui.entrant.canCancel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests for [canCancel]. Covers US 01.07.01, which reservations an
 * entrant can still cancel.
 */
class CancelReservationRuleTest {

    private val today = LocalDate.of(2026, 11, 6)

    private fun stay(start: LocalDate, end: LocalDate, cancelled: Boolean = false) =
        ReservationListItem(id = "r1", campsiteName = "Whistlers Campground", startDate = start, endDate = end, cancelled = cancelled)

    @Test
    fun upcomingStay_canBeCancelled() {
        assertTrue(canCancel(stay(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 5)), today))
    }

    @Test
    fun stayEndingToday_canBeCancelled() {
        assertTrue(canCancel(stay(LocalDate.of(2026, 11, 4), today), today))
    }

    @Test
    fun pastStay_cantBeCancelled() {
        assertFalse(canCancel(stay(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 4)), today))
    }

    @Test
    fun cancelledStay_cantBeCancelledAgain() {
        assertFalse(canCancel(stay(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 5), cancelled = true), today))
    }
}