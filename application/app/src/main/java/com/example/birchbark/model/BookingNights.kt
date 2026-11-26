package com.example.birchbark.model

import com.example.birchbark.util.Time
import java.time.LocalDate

// See #079 in LLM.md

/**
 * US 01.04.01: splits a stay into one [CampsiteBooking] per night, so no two
 * reservations can ever book the same night at a campsite.
 *
 * Each night's document ID is "<campsiteId>_<date>", e.g. "C1_2026-11-27".
 * Two entrants booking the same night would write the same document, and the
 * Firestore rules only allow creating a booking, never overwriting one, so the
 * second confirm is refused by the database itself, even if both tap Confirm
 * at the same moment. Pure logic with no Firestore, so it can be unit tested.
 */
object BookingNights {

    /**
     * Builds a night's document ID.
     *
     * @param campsiteId the campsite's document ID.
     * @param night the night being booked.
     * @return "<campsiteId>_<yyyy-MM-dd>".
     */
    fun id(campsiteId: String, night: LocalDate): String {
        return "${campsiteId}_$night"
    }

    /**
     * Every night of a stay, first to last, both included.
     *
     * @param firstDay first day of the stay.
     * @param lastDay last day of the stay.
     * @return the nights in order, or an empty list if [lastDay] is before [firstDay].
     */
    fun nights(firstDay: LocalDate, lastDay: LocalDate): List<LocalDate> {
        return generateSequence(firstDay) { it.plusDays(1) }
            .takeWhile { !it.isAfter(lastDay) }
            .toList()
    }

    /**
     * One booking per night of a stay, ready to save.
     *
     * @param campsiteId the campsite being booked.
     * @param firstDay first day of the stay.
     * @param lastDay last day of the stay.
     * @return a [CampsiteBooking] for each night, each covering just that day.
     */
    fun bookingsFor(campsiteId: String, firstDay: LocalDate, lastDay: LocalDate): List<CampsiteBooking> {
        return nights(firstDay, lastDay).map { night ->
            CampsiteBooking(
                id = id(campsiteId, night),
                campsiteId = campsiteId,
                startDate = Time.startOfDay(night),
                endDate = Time.endOfDay(night)
            )
        }
    }
}
