package com.example.birchbark.model

import com.example.birchbark.util.Time
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

// See #050 in LLM.md
/**
 * Unit tests for working out a campsite's booked dates.
 * Covers US 02.06.01.
 */
class BookedDatesTest {

    private val nov20 = LocalDate.of(2026, 11, 20)

    //dates built the same way the app stores them: start of the first day, end of the last day
    private fun reservation(
        id: String,
        campsiteId: String = "C1",
        status: ReservationStatus = ReservationStatus.CONFIRMED,
        firstDay: LocalDate? = nov20,
        lastDay: LocalDate? = nov20.plusDays(2)
    ) = Reservation(
        id = id,
        campsiteId = campsiteId,
        status = status,
        startDate = firstDay?.let { Time.startOfDay(it) },
        endDate = lastDay?.let { Time.endOfDay(it) }
    )

    // US 02.06.01: which reservations count as booked

    @Test
    fun confirmedWithDates_isBooked() {
        assertEquals(listOf("a"), BookedDates.forCampsite(listOf(reservation("a")), "C1").map { it.id })
    }

    @Test
    fun otherCampsite_isIgnored() {
        assertEquals(emptyList<Reservation>(), BookedDates.forCampsite(listOf(reservation("a", campsiteId = "C2")), "C1"))
    }

    @Test
    fun notConfirmed_isIgnored() {
        val reservations = listOf(
            reservation("p", status = ReservationStatus.PENDING),
            reservation("d", status = ReservationStatus.DECLINED),
            reservation("c", status = ReservationStatus.CANCELLED),
            reservation("r", status = ReservationStatus.REJECTED),
        )
        assertEquals(emptyList<Reservation>(), BookedDates.forCampsite(reservations, "C1"))
    }

    @Test
    fun confirmedWithoutDates_isIgnored() {
        assertEquals(emptyList<Reservation>(), BookedDates.forCampsite(listOf(reservation("a", firstDay = null, lastDay = null)), "C1"))
    }

    @Test
    fun bookedDates_areEarliestFirst() {
        val later = reservation("later", firstDay = nov20.plusDays(10), lastDay = nov20.plusDays(12))
        val earlier = reservation("earlier")
        assertEquals(listOf("earlier", "later"), BookedDates.forCampsite(listOf(later, earlier), "C1").map { it.id })
    }

    // US 02.06.01: how they're shown

    @Test
    fun label_showsFirstAndLastDay() {
        assertEquals("${Time.format(nov20)} to ${Time.format(nov20.plusDays(2))}", BookedDates.label(listOf(reservation("a"))))
    }

    @Test
    fun label_saysNoneWhenEmpty() {
        assertEquals("No booked dates yet", BookedDates.label(emptyList()))
    }
}
