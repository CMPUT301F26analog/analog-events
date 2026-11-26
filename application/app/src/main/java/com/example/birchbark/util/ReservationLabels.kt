package com.example.birchbark.util

import com.example.birchbark.model.Reservation

// Display helpers for reservation dates. Kept outside the data class so Firestore mapping isn't touched.

/**
 * Creates the dates text for a stay, using the app's date format. Reservation
 * dates are stored the way [com.example.birchbark.model.BookedDates] reads them:
 * the start is the first moment of the first day, and the end is the moment the
 * last day finishes (Time.endOfDay), so the last day is found with [Time.lastDayOf].
 *
 * @param startDate when the stay starts, in epoch millis.
 * @param endDate when the stay ends, in epoch millis.
 * @return "Nov 25, 2026 to Nov 27, 2026" for a stay of more than one day,
 *   or just "Nov 25, 2026" if the stay is a single day.
 */
fun datesLabel(startDate: Long, endDate: Long): String {
    val first = Time.toLocalDate(startDate)
    val last = Time.lastDayOf(endDate)
    return if (first == last) {
        Time.format(first)
    } else {
        Time.format(first) + " to " + Time.format(last)
    }
}

/**
 * The dates text for a [Reservation]. A reservation from the draw has no dates
 * until the entrant picks them, so this can be null.
 *
 * @return the same text as [datesLabel], or null if the start or end date isn't set yet.
 */
fun Reservation.datesLabelOrNull(): String? {
    val start = startDate ?: return null
    val end = endDate ?: return null
    return datesLabel(start, end)
}
