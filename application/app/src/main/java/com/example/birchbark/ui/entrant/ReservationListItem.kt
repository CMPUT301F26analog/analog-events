package com.example.birchbark.ui.entrant

import java.time.LocalDate

/**
 * UI model representing a single campsite reservation entry in a list.
 *
 * @property id The unique identifier of the reservation.
 * @property campsiteName The display name of the reserved campsite.
 * @property startDate The check-in date of the reservation.
 * @property endDate The check-out date of the reservation.
 * @property cancelled Whether the reservation has been cancelled. Defaults to `false`.
 * @property imageUrl Optional URL of the thumbnail image for the campsite.
 */
data class ReservationListItem(
    val id: String,
    val campsiteName: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val cancelled: Boolean = false,
    val imageUrl: String? = null
)

/**
 * Filters and sorts [reservations] to return only active (non-cancelled, ongoing, or upcoming)
 * reservations as of [today], ordered chronologically by [ReservationListItem.startDate].
 *
 * A reservation is considered active if [ReservationListItem.cancelled] is `false` and its
 * [ReservationListItem.endDate] is on or after [today].
 *
 * @param reservations The full list of reservations to filter.
 * @param today The reference date used to determine whether a reservation is still active.
 * @return A list of active [ReservationListItem]s sorted by ascending start date.
 */
fun activeReservations(
    reservations: List<ReservationListItem>,
    today: LocalDate
): List<ReservationListItem> {
    return reservations
        .filter { !it.cancelled && !it.endDate.isBefore(today) }
        .sortedBy { it.startDate }
}

/**
 * Filters and sorts [reservations] to return only completed, non-cancelled reservations that
 * ended before [today], ordered from most recent to oldest by [ReservationListItem.endDate].
 *
 * @param reservations The full list of reservations to filter.
 * @param today The reference date used to determine whether a reservation has ended.
 * @return A list of past [ReservationListItem]s sorted by descending end date.
 */
fun pastReservations(
    reservations: List<ReservationListItem>,
    today: LocalDate
): List<ReservationListItem> {
    return reservations
        .filter { !it.cancelled && it.endDate.isBefore(today) }
        .sortedByDescending { it.endDate }
}

/**
 * Filters and sorts [reservations] to return only cancelled reservations, ordered from
 * newest to oldest by [ReservationListItem.startDate].
 *
 * @param reservations The full list of reservations to filter.
 * @return A list of cancelled [ReservationListItem]s sorted by descending start date.
 */
fun cancelledReservations(
    reservations: List<ReservationListItem>
): List<ReservationListItem> {
    return reservations
        .filter { it.cancelled }
        .sortedByDescending { it.startDate }
}