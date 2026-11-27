package com.example.birchbark

import com.example.birchbark.model.Reservation
import com.example.birchbark.util.Time
import com.example.birchbark.util.datesLabel
import com.example.birchbark.util.datesLabelOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

// See [TBD 2] in LLM.md

/**
 * Unit tests for the dates text shown on the reservation confirmation.
 * Covers US 01.05.01: the confirmation shows the dates that were booked.
 */
class ReservationLabelsTest {

    private val start = Time.startOfDay(LocalDate.of(2026, 11, 25))
    private val end = Time.endOfDay(LocalDate.of(2026, 11, 27))

    @Test
    fun datesLabel_showsStartAndLastDay() {
        assertEquals("Nov 25, 2026 to Nov 27, 2026", datesLabel(start, end))
    }

    //the end is the moment the last day finishes, so it must not show the day after
    @Test
    fun datesLabel_doesNotShowTheDayAfterTheLastDay() {
        assertEquals("Nov 27, 2026", datesLabel(start, end).substringAfter(" to "))
    }

    @Test
    fun datesLabel_showsOneDate_whenSingleDayStay() {
        val day = LocalDate.of(2026, 11, 25)
        assertEquals("Nov 25, 2026", datesLabel(Time.startOfDay(day), Time.endOfDay(day)))
    }

    @Test
    fun datesLabel_worksAcrossAMonthEnd() {
        val label = datesLabel(
            Time.startOfDay(LocalDate.of(2026, 11, 30)),
            Time.endOfDay(LocalDate.of(2026, 12, 2))
        )
        assertEquals("Nov 30, 2026 to Dec 2, 2026", label)
    }

    // a reservation from the draw has no dates until the entrant picks them

    @Test
    fun reservationLabel_usesItsDates() {
        val reservation = Reservation(id = "l1_e1", startDate = start, endDate = end)
        assertEquals("Nov 25, 2026 to Nov 27, 2026", reservation.datesLabelOrNull())
    }

    @Test
    fun reservationLabel_isNull_beforeDatesArePicked() {
        assertNull(Reservation(id = "l1_e1").datesLabelOrNull())
    }

    @Test
    fun reservationLabel_isNull_whenOnlyTheStartIsSet() {
        assertNull(Reservation(id = "l1_e1", startDate = start).datesLabelOrNull())
    }

    @Test
    fun reservationLabel_isNull_whenOnlyTheEndIsSet() {
        assertNull(Reservation(id = "l1_e1", endDate = end).datesLabelOrNull())
    }
}
