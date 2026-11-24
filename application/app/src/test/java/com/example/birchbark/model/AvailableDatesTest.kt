package com.example.birchbark.model

import com.example.birchbark.util.Time
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

// See #051 in LLM.md
/**
 * Unit tests for working out and checking the days a lottery winner can book.
 * Covers US 01.04.01.
 */
class AvailableDatesTest {

    private val nov20 = LocalDate.of(2026, 11, 20)

    //booking period Nov 20 to Nov 29, stored the same way the lottery form stores it
    private val lottery = Lottery(
        id = "L1",
        bookingStart = Time.startOfDay(nov20),
        bookingEnd = Time.endOfDay(nov20.plusDays(9))
    )

    private fun booking(firstDay: LocalDate, lastDay: LocalDate) =
        CampsiteBooking(id = "b", campsiteId = "C1", startDate = Time.startOfDay(firstDay), endDate = Time.endOfDay(lastDay))

    // US 01.04.01: seeing which days are free

    @Test
    fun wholePeriodFree_whenNoBookings() {
        assertEquals(listOf(nov20 to nov20.plusDays(9)), AvailableDates.freeRanges(lottery, emptyList()))
    }

    @Test
    fun bookingInMiddle_splitsFreeDays() {
        val ranges = AvailableDates.freeRanges(lottery, listOf(booking(nov20.plusDays(3), nov20.plusDays(5))))
        assertEquals(listOf(nov20 to nov20.plusDays(2), nov20.plusDays(6) to nov20.plusDays(9)), ranges)
    }

    @Test
    fun bookingAtStart_leavesTheRest() {
        val ranges = AvailableDates.freeRanges(lottery, listOf(booking(nov20, nov20.plusDays(1))))
        assertEquals(listOf(nov20.plusDays(2) to nov20.plusDays(9)), ranges)
    }

    @Test
    fun noFreeDays_whenFullyBooked() {
        assertEquals(emptyList<Pair<LocalDate, LocalDate>>(), AvailableDates.freeRanges(lottery, listOf(booking(nov20, nov20.plusDays(9)))))
    }

    @Test
    fun label_saysWhenNothingLeft() {
        assertEquals("No dates left in the booking period", AvailableDates.label(emptyList()))
    }

    // US 01.04.01: checking the days the winner picks

    @Test
    fun freeDaysInsidePeriod_areOk() {
        assertNull(AvailableDates.problemWith(lottery, emptyList(), nov20.plusDays(1), nov20.plusDays(3)))
    }

    @Test
    fun singleDay_isOk() {
        assertNull(AvailableDates.problemWith(lottery, emptyList(), nov20, nov20))
    }

    @Test
    fun lastDayBeforeFirst_isRejected() {
        assertNotNull(AvailableDates.problemWith(lottery, emptyList(), nov20.plusDays(3), nov20.plusDays(1)))
    }

    @Test
    fun daysOutsidePeriod_areRejected() {
        assertNotNull(AvailableDates.problemWith(lottery, emptyList(), nov20.minusDays(1), nov20.plusDays(1)))
        assertNotNull(AvailableDates.problemWith(lottery, emptyList(), nov20.plusDays(8), nov20.plusDays(10)))
    }

    @Test
    fun overlappingBooking_isRejected() {
        val taken = listOf(booking(nov20.plusDays(4), nov20.plusDays(6)))
        assertNotNull(AvailableDates.problemWith(lottery, taken, nov20.plusDays(2), nov20.plusDays(4)))
    }

    @Test
    fun daysNextToBooking_areOk() {
        val taken = listOf(booking(nov20.plusDays(4), nov20.plusDays(6)))
        assertNull(AvailableDates.problemWith(lottery, taken, nov20.plusDays(1), nov20.plusDays(3)))
    }
}
