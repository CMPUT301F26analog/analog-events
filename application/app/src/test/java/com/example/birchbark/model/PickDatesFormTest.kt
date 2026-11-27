package com.example.birchbark.model

import com.example.birchbark.util.Time
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

// See #056 in LLM.md
/**
 * Unit tests for checking the days a lottery winner types on the pick dates screen.
 * Covers US 01.04.01.
 */
class PickDatesFormTest {

    private val nov20 = LocalDate.of(2026, 11, 20)

    //booking period Nov 20 to Nov 29, stored the same way the lottery form stores it
    private val lottery = Lottery(
        id = "L1",
        bookingStart = Time.startOfDay(nov20),
        bookingEnd = Time.endOfDay(nov20.plusDays(9))
    )

    //another winner already has Nov 24 to Nov 26
    private val taken = listOf(
        CampsiteBooking(
            id = "b", campsiteId = "C1",
            startDate = Time.startOfDay(nov20.plusDays(4)),
            endDate = Time.endOfDay(nov20.plusDays(6))
        )
    )

    // US 01.04.01: typing the days

    @Test
    fun freeDays_arePicked() {
        val result = PickDatesForm.check("2026-11-21", "2026-11-23", lottery, taken)
        assertEquals(PickDatesForm.Result.Picked(nov20.plusDays(1), nov20.plusDays(3)), result)
    }

    @Test
    fun spacesAroundDays_areIgnored() {
        val result = PickDatesForm.check(" 2026-11-21 ", "2026-11-21 ", lottery, taken)
        assertEquals(PickDatesForm.Result.Picked(nov20.plusDays(1), nov20.plusDays(1)), result)
    }

    @Test
    fun blankDay_isAProblem() {
        val result = PickDatesForm.check("", "2026-11-23", lottery, taken)
        assertEquals(PickDatesForm.Result.Problem("Enter both days like 2026-11-20"), result)
    }

    @Test
    fun wrongFormat_isAProblem() {
        val result = PickDatesForm.check("21/11/2026", "2026-11-23", lottery, taken)
        assertTrue(result is PickDatesForm.Result.Problem)
    }

    @Test
    fun notARealDate_isNotParsed() {
        assertNull(PickDatesForm.parseDay("2026-02-30"))
    }

    // US 01.04.01: checking against the lottery and other bookings

    @Test
    fun lastDayBeforeFirst_isAProblem() {
        val result = PickDatesForm.check("2026-11-23", "2026-11-21", lottery, taken)
        assertTrue(result is PickDatesForm.Result.Problem)
    }

    @Test
    fun daysOutsideBookingPeriod_areAProblem() {
        val result = PickDatesForm.check("2026-11-28", "2026-12-02", lottery, taken)
        assertTrue(result is PickDatesForm.Result.Problem)
    }

    @Test
    fun daysAlreadyBooked_areAProblem() {
        val result = PickDatesForm.check("2026-11-23", "2026-11-25", lottery, taken)
        assertEquals(PickDatesForm.Result.Problem("Some of those days are already booked"), result)
    }
}
