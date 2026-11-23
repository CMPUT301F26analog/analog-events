package com.example.birchbark.model

import com.example.birchbark.util.Time
import java.time.LocalDate

// See #051 in LLM.md

/**
 * US 01.04.01: works out which days a lottery winner can still reserve, and
 * checks the days they pick. A winner can book any days inside the lottery's
 * booking period that no confirmed booking already takes. Pure logic with no
 * Firestore, so it can be unit tested.
 *
 * A stay is described by its first and last day, both included, the same way
 * owners type booking periods.
 */
object AvailableDates {

    /** First day winners can book, from [Lottery.bookingStart]. */
    fun firstBookableDay(lottery: Lottery): LocalDate = Time.toLocalDate(lottery.bookingStart)

    /** Last day winners can book, from [Lottery.bookingEnd]. */
    fun lastBookableDay(lottery: Lottery): LocalDate = Time.lastDayOf(lottery.bookingEnd)

    /**
     * Every day already taken by a booking.
     *
     * @param bookings bookings for one campsite.
     */
    fun bookedDays(bookings: List<CampsiteBooking>): Set<LocalDate> {
        val days = mutableSetOf<LocalDate>()
        for (booking in bookings) {
            var day = Time.toLocalDate(booking.startDate)
            val last = Time.lastDayOf(booking.endDate)
            while (!day.isAfter(last)) {
                days.add(day)
                day = day.plusDays(1)
            }
        }
        return days
    }

    /**
     * The free stretches of days in the booking period, earliest first.
     *
     * @param lottery the lottery the winner won.
     * @param bookings existing bookings for the campsite.
     * @return each free stretch as (first day, last day).
     */
    fun freeRanges(lottery: Lottery, bookings: List<CampsiteBooking>): List<Pair<LocalDate, LocalDate>> {
        val booked = bookedDays(bookings)
        val ranges = mutableListOf<Pair<LocalDate, LocalDate>>()
        var rangeStart: LocalDate? = null
        var day = firstBookableDay(lottery)
        val last = lastBookableDay(lottery)
        while (!day.isAfter(last)) {
            val start = rangeStart
            if (day in booked) {
                if (start != null) ranges.add(start to day.minusDays(1))
                rangeStart = null
            } else if (start == null) {
                rangeStart = day
            }
            day = day.plusDays(1)
        }
        rangeStart?.let { ranges.add(it to last) }
        return ranges
    }

    /**
     * The free stretches as text for the screen, one per line.
     *
     * @return e.g. "Nov 20, 2026 to Nov 22, 2026", or a message if every day is taken.
     */
    fun label(ranges: List<Pair<LocalDate, LocalDate>>): String {
        if (ranges.isEmpty()) return "No dates left in the booking period"
        return ranges.joinToString("\n") { (first, last) ->
            if (first == last) Time.format(first) else "${Time.format(first)} to ${Time.format(last)}"
        }
    }

    /**
     * Checks the days a winner picked.
     *
     * @param lottery the lottery the winner won.
     * @param bookings existing bookings for the campsite.
     * @param firstDay first day of the stay.
     * @param lastDay last day of the stay.
     * @return why the days can't be booked, or null if they can.
     */
    fun problemWith(
        lottery: Lottery,
        bookings: List<CampsiteBooking>,
        firstDay: LocalDate,
        lastDay: LocalDate
    ): String? {
        val periodStart = firstBookableDay(lottery)
        val periodEnd = lastBookableDay(lottery)
        return when {
            lastDay.isBefore(firstDay) -> "The last day can't be before the first day"
            firstDay.isBefore(periodStart) || lastDay.isAfter(periodEnd) ->
                "Pick days between ${Time.format(periodStart)} and ${Time.format(periodEnd)}"
            bookedDays(bookings).any { !it.isBefore(firstDay) && !it.isAfter(lastDay) } ->
                "Some of those days are already booked"
            else -> null
        }
    }
}
