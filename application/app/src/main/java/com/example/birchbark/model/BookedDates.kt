package com.example.birchbark.model

import com.example.birchbark.util.Time

// See #050 in LLM.md

/**
 * US 02.06.01: works out which dates of a campsite are booked. A date range
 * counts as booked once the entrant has confirmed it; pending reservations
 * haven't picked or accepted dates yet, so they don't block anything.
 * Pure logic with no Firestore, so it can be unit tested.
 */
object BookedDates {

    /**
     * The confirmed reservations with dates for one campsite, earliest first.
     *
     * @param reservations reservations to look through, e.g. all of the owner's.
     * @param campsiteId the campsite to check.
     */
    fun forCampsite(reservations: List<Reservation>, campsiteId: String): List<Reservation> {
        return reservations
            .filter {
                it.campsiteId == campsiteId &&
                    it.status == ReservationStatus.CONFIRMED &&
                    it.startDate != null && it.endDate != null
            }
            .sortedBy { it.startDate }
    }

    /**
     * The booked ranges as text for the details screen, one per line, e.g.
     * "Nov 20, 2026 to Nov 22, 2026". The end is stored as the moment the
     * last night ends, so it is shown with [Time.lastDayOf].
     *
     * @param booked the result of [forCampsite].
     * @return the ranges, or "No booked dates yet" if there are none.
     */
    fun label(booked: List<Reservation>): String {
        if (booked.isEmpty()) return "No booked dates yet"
        return booked.joinToString("\n") { reservation ->
            val start = Time.format(Time.toLocalDate(reservation.startDate!!))
            val end = Time.format(Time.lastDayOf(reservation.endDate!!))
            "$start to $end"
        }
    }
}
