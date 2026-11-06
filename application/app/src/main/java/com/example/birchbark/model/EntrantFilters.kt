package com.example.birchbark.model

import java.time.LocalDate

/**
 * Criteria selected by a user to narrow down the list of available campsites on the browse screen.
 * Any property left blank or null will be excluded from the filtering logic.
 *
 * @property location The geographical area or campground name to search within.
 * @property startDate The requested check-in date. Note: Captured here for future feature support,
 * but currently inactive until campsite seasonal availability schedules are implemented.
 * @property endDate The requested check-out date.
 * @property minOccupancy The minimum guest capacity the campsite must accommodate.
 * @property minRating The lowest acceptable average user review score, from 1.0 to 5.0.C ampsites with
 *  *   no ratings don't pass if this is set.
 */
data class EntrantFilters(
    val location: String = "",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val minOccupancy: Int? = null,
    val minRating: Double? = null
) {
    /**
     * How many filters are set, for the "Filters (2)" button label. Both dates count as one.
     */
    val activeCount: Int
    get() = listOf(
        location.isNotBlank(),
        startDate != null || endDate != null,
        minOccupancy != null,
        minRating != null
    ).count { it }
}