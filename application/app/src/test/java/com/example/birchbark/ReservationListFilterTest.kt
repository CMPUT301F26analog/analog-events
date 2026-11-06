package com.example.birchbark

import com.example.birchbark.ui.entrant.ReservationListItem
import com.example.birchbark.ui.entrant.activeReservations
import com.example.birchbark.ui.entrant.pastReservations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ReservationListFilterTest {

    private val today = LocalDate.of(2026, 11, 6)

    private fun reservation(
        id: String,
        start: String,
        end: String,
        cancelled: Boolean = false
    ) = ReservationListItem(
        id = id,
        campsiteName = "Test Campsite",
        startDate = LocalDate.parse(start),
        endDate = LocalDate.parse(end),
        cancelled = cancelled
    )

    @Test
    fun currentReservationIsActive() {
        val item = reservation("1", "2026-11-04", "2026-11-08")
        assertEquals(listOf(item), activeReservations(listOf(item), today))
    }

    @Test
    fun futureReservationIsActive() {
        val item = reservation("1", "2026-12-01", "2026-12-05")
        assertEquals(listOf(item), activeReservations(listOf(item), today))
    }

    @Test
    fun completedReservationIsPast() {
        val item = reservation("1", "2026-10-01", "2026-10-05")
        assertEquals(listOf(item), pastReservations(listOf(item), today))
    }

    @Test
    fun checkoutTodayIsStillActive() {
        val item = reservation("1", "2026-11-04", "2026-11-06")
        assertEquals(listOf(item), activeReservations(listOf(item), today))
    }

    @Test
    fun cancelledReservationIsExcluded() {
        val item = reservation(
            "1",
            "2026-11-04",
            "2026-11-08",
            cancelled = true
        )

        assertTrue(activeReservations(listOf(item), today).isEmpty())
        assertTrue(pastReservations(listOf(item), today).isEmpty())
    }

    @Test
    fun activeReservationsAreSortedByStartDate() {
        val later = reservation("2", "2026-12-01", "2026-12-05")
        val earlier = reservation("1", "2026-11-10", "2026-11-12")

        assertEquals(
            listOf("1", "2"),
            activeReservations(listOf(later, earlier), today).map { it.id }
        )
    }

    @Test
    fun pastReservationsShowMostRecentFirst() {
        val older = reservation("1", "2026-09-01", "2026-09-05")
        val newer = reservation("2", "2026-10-01", "2026-10-05")

        assertEquals(
            listOf("2", "1"),
            pastReservations(listOf(older, newer), today).map { it.id }
        )
    }
}
