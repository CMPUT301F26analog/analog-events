package com.example.birchbark.model

import com.example.birchbark.util.Time
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

// See #057 in LLM.md
/**
 * Unit tests for splitting a stay into one booking per night, which is what
 * stops two reservations from booking the same night.
 * Covers US 01.04.01.
 */
class BookingNightsTest {

    private val nov27 = LocalDate.of(2026, 11, 27)

    // US 01.04.01: one document per night

    @Test
    fun id_isCampsiteAndDate() {
        assertEquals("C1_2026-11-27", BookingNights.id("C1", nov27))
    }

    @Test
    fun sameNightSameCampsite_hasSameId() {
        //this is what makes a second booking of the night collide with the first
        assertEquals(BookingNights.id("C1", nov27), BookingNights.id("C1", LocalDate.of(2026, 11, 27)))
    }

    @Test
    fun sameNightOtherCampsite_hasDifferentId() {
        assertNotEquals(BookingNights.id("C1", nov27), BookingNights.id("C2", nov27))
    }

    @Test
    fun nights_includeFirstAndLastDay() {
        assertEquals(listOf(nov27, nov27.plusDays(1), nov27.plusDays(2)), BookingNights.nights(nov27, nov27.plusDays(2)))
    }

    @Test
    fun singleDayStay_isOneNight() {
        assertEquals(listOf(nov27), BookingNights.nights(nov27, nov27))
    }

    @Test
    fun lastDayBeforeFirst_hasNoNights() {
        assertTrue(BookingNights.nights(nov27, nov27.minusDays(1)).isEmpty())
    }

    @Test
    fun overlappingStays_shareTheOverlappingNightIds() {
        //Nov 27-29 and Nov 29-30 both include Nov 29, so they'd write the same document
        val first = BookingNights.bookingsFor("C1", nov27, nov27.plusDays(2)).map { it.id }
        val second = BookingNights.bookingsFor("C1", nov27.plusDays(2), nov27.plusDays(3)).map { it.id }
        assertEquals(listOf("C1_2026-11-29"), first.intersect(second.toSet()).toList())
    }

    @Test
    fun bookings_coverEachNightAsOneDay() {
        val booking = BookingNights.bookingsFor("C1", nov27, nov27).single()
        assertEquals("C1", booking.campsiteId)
        assertEquals(Time.startOfDay(nov27), booking.startDate)
        assertEquals(Time.endOfDay(nov27), booking.endDate)
    }

    @Test
    fun nightlyBookings_stillBlockDaysInAvailableDates() {
        //AvailableDates reads per-night bookings the same way as before
        val lottery = Lottery(bookingStart = Time.startOfDay(nov27), bookingEnd = Time.endOfDay(nov27.plusDays(5)))
        val taken = BookingNights.bookingsFor("C1", nov27.plusDays(1), nov27.plusDays(2))
        assertEquals(setOf(nov27.plusDays(1), nov27.plusDays(2)), AvailableDates.bookedDays(taken))
        assertTrue(AvailableDates.problemWith(lottery, taken, nov27.plusDays(2), nov27.plusDays(3)) != null)
    }
}
