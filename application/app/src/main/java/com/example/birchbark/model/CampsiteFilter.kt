package com.example.birchbark.model

import com.example.birchbark.util.Time
import java.time.LocalDate

/**
 * US 01.01.02 Filter campsites by location, date and number of guests, and
 * US 01.01.03 See a list of campsites based on my filters. Takes what the entrant
 * picked on the filter screen ([EntrantFilters]) and works out which campsites
 * match. Only campsites that match every filter they picked show up, and
 * anything left blank isn't checked.
 *
 * Outstanding issues. There are no reservations yet (Part 4), so for dates a
 * campsite counts as available if it's in a lottery whose booking period
 * overlaps the dates picked. The screen has to load the lotteries and pass them in.
 */
object CampsiteFilter {

    /**
     * Gives back only the campsites that match the filters.
     *
     * @param campsites every campsite to filter.
     * @param filters what the entrant picked on the filter screen.
     * @param lotteries every lottery, only needed if dates were picked.
     * @return the campsites that match every filter, in the same order.
     */
    fun apply(
        campsites: List<Campsite>,
        filters: EntrantFilters,
        lotteries: List<Lottery> = listOf()
    ): List<Campsite> {
        return campsites.filter { matches(it, filters, lotteries) }
    }

    /**
     * Checks one campsite against every filter.
     *
     * @param campsite the campsite to check.
     * @param filters what the entrant picked on the filter screen.
     * @param lotteries every lottery, only needed if dates were picked.
     * @return true if it passes every filter that was picked, false otherwise.
     */
    fun matches(
        campsite: Campsite,
        filters: EntrantFilters,
        lotteries: List<Lottery> = listOf()
    ): Boolean {
        return matchesLocation(campsite, filters.location) &&
                matchesGuests(campsite, filters.minOccupancy) &&
                matchesRating(campsite, filters.minRating) &&
                matchesDates(campsite, filters.startDate, filters.endDate, lotteries)
    }

    /**
     * Checks the location filter. Doesn't care about upper or lower case.
     *
     * @param campsite the campsite to check.
     * @param location what the entrant typed, or blank for any location.
     * @return true if the location is blank or the campsite's location contains it.
     */
    fun matchesLocation(campsite: Campsite, location: String): Boolean {
        val search = location.trim()
        if (search.isEmpty()) {
            return true
        }
        return campsite.location.contains(search, ignoreCase = true)
    }

    /**
     * Checks the number of guests filter.
     *
     * @param campsite the campsite to check.
     * @param minOccupancy how many guests the entrant needs room for, or null for any.
     * @return true if it's null or the campsite fits at least that many.
     */
    fun matchesGuests(campsite: Campsite, minOccupancy: Int?): Boolean {
        return minOccupancy == null || campsite.maxOccupancy >= minOccupancy
    }

    /**
     * Checks the minimum rating filter. A campsite with no ratings yet
     * doesn't pass if a minimum was picked.
     *
     * @param campsite the campsite to check.
     * @param minRating the lowest average rating allowed, or null for any.
     * @return true if it's null or the campsite's average rating is at least that.
     */
    fun matchesRating(campsite: Campsite, minRating: Double?): Boolean {
        if (minRating == null) {
            return true
        }
        val average = campsite.averageRating() ?: return false
        return average >= minRating
    }

    /**
     * Checks the date filter. If only one date was picked it's treated as a single day.
     *
     * @param campsite the campsite to check.
     * @param startDate first day the entrant wants, or null.
     * @param endDate last day the entrant wants, or null.
     * @param lotteries every lottery, to find the campsite's booking periods.
     * @return true if no dates were picked, or the campsite is in a lottery
     *   whose booking period overlaps those dates.
     */
    fun matchesDates(
        campsite: Campsite,
        startDate: LocalDate?,
        endDate: LocalDate?,
        lotteries: List<Lottery>
    ): Boolean {
        val firstDay = startDate ?: endDate
        val lastDay = endDate ?: startDate
        if (firstDay == null || lastDay == null) {
            return true
        }

        val from = Time.startOfDay(firstDay)
        val until = Time.endOfDay(lastDay)
        for (lottery in lotteries) {
            //booking period overlaps if it starts before they leave and ends after they arrive
            if (campsite.id in lottery.campsiteIds &&
                lottery.bookingStart < until &&
                lottery.bookingEnd > from
            ) {
                return true
            }
        }
        return false
    }
}