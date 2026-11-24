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
 * The informational categories shown on the campsite details screen.
 *
 * @property label Text on the tab.
 * @property emptyText Shown when the owner left that section blank.
 */
enum class DetailTab(val label: String, val emptyText: String) {
    DESCRIPTION("Description", "No description provided."),
    RULES("Rules", "No rules provided."),
    POLICY("Cancellation", "No cancellation policy provided.")
}