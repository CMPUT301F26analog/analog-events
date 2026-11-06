package com.example.birchbark.util

import com.example.birchbark.model.Campsite

// Display helpers for the team's Campsite. Kept outside the data class so Firestore mapping isn't touched.

/**
 * Creates a user-facing rating string for a [Campsite].
 *
 * This function evaluates the  average rating of the campsite.
 * If a valid average exists, it formats the value to a single decimal
 * place prefixed by a star symbol. If no rating data is available, it provides
 * a message. i.e., "★ 4.9", or "No ratings" when the campsite has none.
 *
 * @return A formatted string representation of the rating (e.g., "★ 4.9")
 * or "No ratings" if unavailable.
 */
fun Campsite.ratingLabel(): String {
    val average = averageRating()
    return if (average == null) "No ratings" else "★ " + "%.1f".format(average)
}

/**
 * Creates a localized currency label for the nightly cost of a [Campsite].
 *
 * This function inspects the price field of the campsite instance.
 * If a price value is specified, it shows the number as a standard layout.
 * If the value is missing, it returns a message. i.e., "$38.00 per night", or "Price not set"
 *
 *
 * @return A descriptive string showing the rate (e.g., "$38.00 per night")
 * or "Price not set" if unassigned.
 */
fun Campsite.priceLabel(): String {
    val perNight = price
    return if (perNight == null) "Price not set" else String.format("$%.2f per night", perNight)
}