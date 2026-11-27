package com.example.birchbark.ui.entrant

import java.time.LocalDate

data class ReservationListItem(
    val id: String,
    val campsiteName: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val cancelled: Boolean = false
)

fun activeReservations(
    reservations: List<ReservationListItem>,
    today: LocalDate
): List<ReservationListItem> {
    return reservations
        .filter { !it.cancelled && !it.endDate.isBefore(today) }
        .sortedBy { it.startDate }
}

fun pastReservations(
    reservations: List<ReservationListItem>,
    today: LocalDate
): List<ReservationListItem> {
    return reservations
        .filter { !it.cancelled && it.endDate.isBefore(today) }
        .sortedByDescending { it.endDate }
}

/**
 * US 01.07.01: whether the entrant can still cancel a reservation. It can't
 * already be cancelled, and the stay can't be over.
 *
 * @param item the reservation.
 * @param today the date to compare with.
 * @return true if the Cancel Reservation button should show.
 */
fun canCancel(item: ReservationListItem, today: LocalDate): Boolean {
    return !item.cancelled && !item.endDate.isBefore(today)
}