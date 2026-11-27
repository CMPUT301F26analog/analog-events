package com.example.birchbark

import com.example.birchbark.model.Campsite
import com.example.birchbark.model.Reservation
import com.example.birchbark.model.ReservationStatus
import com.example.birchbark.ui.entrant.toListItems
import com.example.birchbark.util.Time
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

// See #056 in LLM.md
/**
 * Unit tests for turning an entrant's reservations into the rows of their
 * Active / Past reservation lists.
 * Covers US 01.06.01, 01.06.02.
 */
class EntrantReservationItemsTest {

    private val nov20 = LocalDate.of(2026, 11, 20)
    private val campsites = mapOf("C1" to Campsite(id = "C1", name = "Whistlers Campground"))

    //dates stored the way confirm saves them: start of the first day, end of the last day
    private fun reservation(id: String, status: ReservationStatus, withDates: Boolean = true) = Reservation(
        id = id,
        campsiteId = "C1",
        status = status,
        startDate = if (withDates) Time.startOfDay(nov20) else null,
        endDate = if (withDates) Time.endOfDay(nov20.plusDays(2)) else null
    )

    // US 01.06.01, 01.06.02: which reservations show up in the lists

    @Test
    fun confirmed_isListed_withItsCampsiteAndDays() {
        val item = toListItems(listOf(reservation("a", ReservationStatus.CONFIRMED)), campsites).single()
        assertEquals("Whistlers Campground", item.campsiteName)
        assertEquals(nov20, item.startDate)
        assertEquals(nov20.plusDays(2), item.endDate)
    }

    @Test
    fun cancelled_isListedAsCancelled() {
        val item = toListItems(listOf(reservation("a", ReservationStatus.CANCELLED)), campsites).single()
        assertTrue(item.cancelled)
    }

    @Test
    fun pendingAndDeclined_areLeftOut() {
        val reservations = listOf(
            reservation("p", ReservationStatus.PENDING, withDates = false),
            reservation("d", ReservationStatus.DECLINED),
        )
        assertTrue(toListItems(reservations, campsites).isEmpty())
    }

    // US 02.08.01: the entrant sees that the owner rejected their stay
    @Test
    fun rejected_isListedAsCancelled() {
        val item = toListItems(listOf(reservation("r", ReservationStatus.REJECTED)), campsites).single()
        assertTrue(item.cancelled)
    }

    @Test
    fun unknownCampsite_usesGenericName() {
        val item = toListItems(listOf(reservation("a", ReservationStatus.CONFIRMED)), emptyMap()).single()
        assertEquals("Campsite", item.campsiteName)
    }
}
