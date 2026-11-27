package com.example.birchbark.model

import java.time.LocalDate
import java.time.format.DateTimeParseException

// See #056 in LLM.md

/**
 * US 01.04.01: checks the first and last day a lottery winner typed on the
 * pick dates screen. Pure logic with no Firestore or UI, so the screen only
 * shows the result and this can be unit tested.
 */
object PickDatesForm {

    /** What [check] decided. */
    sealed class Result {
        /** The days are fine to book. */
        data class Picked(val firstDay: LocalDate, val lastDay: LocalDate) : Result()

        /** The days can't be booked. [message] is shown to the entrant. */
        data class Problem(val message: String) : Result()
    }

    /**
     * Reads the typed days and checks them against the lottery's booking
     * period and the campsite's existing bookings ([AvailableDates.problemWith]).
     *
     * @param firstDayText what the entrant typed for the first day, like "2026-11-20".
     * @param lastDayText what the entrant typed for the last day.
     * @param lottery the lottery the entrant won.
     * @param bookings bookings already made at the campsite.
     * @param capacity how many parties fit on one night ([Campsite.maxOccupancy]).
     * @return [Result.Picked] with the days, or [Result.Problem] saying what's wrong.
     */
    fun check(
        firstDayText: String,
        lastDayText: String,
        lottery: Lottery,
        bookings: List<CampsiteBooking>,
        capacity: Int = 1
    ): Result {
        val first = parseDay(firstDayText)
        val last = parseDay(lastDayText)
        if (first == null || last == null) {
            return Result.Problem("Enter both days like 2026-11-20")
        }
        val problem = AvailableDates.problemWith(lottery, bookings, first, last, capacity)
        return if (problem == null) Result.Picked(first, last) else Result.Problem(problem)
    }

    /**
     * Reads a typed day like 2026-11-20.
     *
     * @return the day, or null if the text isn't a real date in that format.
     */
    fun parseDay(text: String): LocalDate? {
        return try {
            LocalDate.parse(text.trim())
        } catch (e: DateTimeParseException) {
            null
        }
    }
}
