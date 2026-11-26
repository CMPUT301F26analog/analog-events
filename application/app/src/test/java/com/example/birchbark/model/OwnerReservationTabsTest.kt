package com.example.birchbark.model

import org.junit.Assert.assertEquals
import org.junit.Test

// See #049 in LLM.md
/**
 * Unit tests for sorting an owner's reservations into Active, Past and Cancelled.
 * Covers US 02.07.01, 02.07.02, 02.07.03.
 */
class OwnerReservationTabsTest {

    private val now = 1_000L

    private fun reservation(id: String, status: ReservationStatus, endDate: Long? = null) =
        Reservation(id = id, status = status, startDate = endDate?.let { it - 500L }, endDate = endDate)

    // US 02.07.01: active reservations

    @Test
    fun pendingWithoutDates_isActive() {
        assertEquals(OwnerReservationTab.ACTIVE, OwnerReservationTabs.tabOf(reservation("r", ReservationStatus.PENDING), now))
    }

    @Test
    fun confirmedEndingLater_isActive() {
        assertEquals(OwnerReservationTab.ACTIVE, OwnerReservationTabs.tabOf(reservation("r", ReservationStatus.CONFIRMED, 2_000L), now))
    }

    // US 02.07.02: past reservations

    @Test
    fun confirmedAlreadyEnded_isPast() {
        assertEquals(OwnerReservationTab.PAST, OwnerReservationTabs.tabOf(reservation("r", ReservationStatus.CONFIRMED, 500L), now))
    }

    @Test
    fun confirmedEndingExactlyNow_isPast() {
        //endDate is exclusive: the stay is over at that moment
        assertEquals(OwnerReservationTab.PAST, OwnerReservationTabs.tabOf(reservation("r", ReservationStatus.CONFIRMED, now), now))
    }

    // US 02.07.03: cancelled reservations

    @Test
    fun cancelledDeclinedAndRejected_areCancelled() {
        val statuses = listOf(ReservationStatus.CANCELLED, ReservationStatus.DECLINED, ReservationStatus.REJECTED)
        for (status in statuses) {
            assertEquals(OwnerReservationTab.CANCELLED, OwnerReservationTabs.tabOf(reservation("r", status, 2_000L), now))
        }
    }

    @Test
    fun forTab_keepsOnlyThatTab_inOrder() {
        val all = listOf(
            reservation("a", ReservationStatus.CONFIRMED, 2_000L),
            reservation("b", ReservationStatus.CANCELLED),
            reservation("c", ReservationStatus.PENDING),
            reservation("d", ReservationStatus.CONFIRMED, 500L),
        )
        assertEquals(listOf("a", "c"), OwnerReservationTabs.forTab(all, OwnerReservationTab.ACTIVE, now).map { it.id })
        assertEquals(listOf("d"), OwnerReservationTabs.forTab(all, OwnerReservationTab.PAST, now).map { it.id })
        assertEquals(listOf("b"), OwnerReservationTabs.forTab(all, OwnerReservationTab.CANCELLED, now).map { it.id })
    }
}
