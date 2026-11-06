package com.example.birchbark

import com.example.birchbark.model.Campsite
import com.example.birchbark.model.CampsiteFilter
import com.example.birchbark.model.EntrantFilters
import com.example.birchbark.model.Lottery
import com.example.birchbark.util.Time
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

// See [8] in LLM.md, used to help write Test for CampsiteFilter.kt

/**
 * Unit tests for filtering campsites.
 * Covers US 01.01.02 and US 01.01.03, only campsites that match all the
 * filters picked show up, and clearing the filters shows every campsite.
 */
class CampsiteFilterTest {

    private val whistlers = Campsite(
        id = "c1", name = "Whistlers Campground", location = "Jasper National Park",
        maxOccupancy = 6, ratingTotal = 9, ratingCount = 2
    )
    private val twoJack = Campsite(
        id = "c2", name = "Two Jack Lakeside", location = "Banff National Park",
        maxOccupancy = 4
    )
    private val dinosaur = Campsite(
        id = "c3", name = "Dinosaur Provincial Park Campground", location = "Near Brooks",
        maxOccupancy = 8, ratingTotal = 7, ratingCount = 2
    )
    private val all = listOf(whistlers, twoJack, dinosaur)

    /** Whistlers can be booked Nov 25 to Dec 31, 2026. */
    private val whistlersLottery = Lottery(
        id = "l1",
        campsiteIds = listOf("c1"),
        registrationStart = Time.startOfDay(LocalDate.of(2026, 11, 10)),
        registrationEnd = Time.endOfDay(LocalDate.of(2026, 11, 20)),
        bookingStart = Time.startOfDay(LocalDate.of(2026, 11, 25)),
        bookingEnd = Time.endOfDay(LocalDate.of(2026, 12, 31)),
        numWinners = 5
    )

    // clearing the filters shows every campsite

    @Test
    fun noFilters_showsEverything() {
        assertEquals(all, CampsiteFilter.apply(all, EntrantFilters()))
    }

    // location

    @Test
    fun location_matchesIgnoringCase() {
        val result = CampsiteFilter.apply(all, EntrantFilters(location = "jasper"))
        assertEquals(listOf(whistlers), result)
    }

    @Test
    fun location_matchesPartOfIt() {
        val result = CampsiteFilter.apply(all, EntrantFilters(location = "National Park"))
        assertEquals(listOf(whistlers, twoJack), result)
    }

    @Test
    fun location_blankIsIgnored() {
        assertTrue(CampsiteFilter.matchesLocation(twoJack, "   "))
    }

    // number of guests

    @Test
    fun guests_passesWhenEnoughRoom() {
        assertTrue(CampsiteFilter.matchesGuests(whistlers, 4))
    }

    @Test
    fun guests_passesWhenExactlyFull() {
        assertTrue(CampsiteFilter.matchesGuests(whistlers, 6))
    }

    @Test
    fun guests_failsWhenTooMany() {
        assertFalse(CampsiteFilter.matchesGuests(twoJack, 5))
    }

    // rating

    @Test
    fun rating_passesWhenHighEnough() {
        //9 total from 2 ratings averages to 4.5
        assertTrue(CampsiteFilter.matchesRating(whistlers, 4.0))
    }

    @Test
    fun rating_failsWhenTooLow() {
        //7 total from 2 ratings averages to 3.5
        assertFalse(CampsiteFilter.matchesRating(dinosaur, 4.0))
    }

    @Test
    fun rating_failsWhenNoRatings() {
        assertFalse(CampsiteFilter.matchesRating(twoJack, 1.0))
    }

    // dates

    @Test
    fun dates_matchesWhenInsideBookingPeriod() {
        val filters = EntrantFilters(
            startDate = LocalDate.of(2026, 12, 1),
            endDate = LocalDate.of(2026, 12, 3)
        )
        assertEquals(listOf(whistlers), CampsiteFilter.apply(all, filters, listOf(whistlersLottery)))
    }

    @Test
    fun dates_matchesWhenOnlyOverlappingTheStart() {
        val filters = EntrantFilters(
            startDate = LocalDate.of(2026, 11, 20),
            endDate = LocalDate.of(2026, 11, 25)
        )
        assertTrue(CampsiteFilter.matches(whistlers, filters, listOf(whistlersLottery)))
    }

    @Test
    fun dates_noMatchWhenOutsideBookingPeriod() {
        val filters = EntrantFilters(
            startDate = LocalDate.of(2027, 1, 5),
            endDate = LocalDate.of(2027, 1, 7)
        )
        assertEquals(listOf<Campsite>(), CampsiteFilter.apply(all, filters, listOf(whistlersLottery)))
    }

    @Test
    fun dates_onlyStartPicked_countsAsOneDay() {
        val filters = EntrantFilters(startDate = LocalDate.of(2026, 12, 31))
        assertTrue(CampsiteFilter.matches(whistlers, filters, listOf(whistlersLottery)))
    }

    // only campsites that match all the filters show up

    @Test
    fun allFilters_mustAllMatch() {
        val filters = EntrantFilters(
            location = "Jasper",
            minOccupancy = 8,
            minRating = 4.0
        )
        //Whistlers is in Jasper with a 4.5 rating but only fits 6
        assertEquals(listOf<Campsite>(), CampsiteFilter.apply(all, filters))
    }
}