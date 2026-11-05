package com.example.birchbark

// Display helpers for the team's Campsite. Kept outside the data class so Firestore mapping isn't touched.

/** "★ 4.9", or "No ratings" when the campsite has none (uses Campsite.averageRating). */
fun Campsite.ratingLabel(): String {
    val average = averageRating()
    return if (average == null) "No ratings" else "★ " + "%.1f".format(average)
}

/** "$38.00 per night", or "Price not set" when the owner didn't enter one. */
fun Campsite.priceLabel(): String {
    val perNight = price
    return if (perNight == null) "Price not set" else String.format("$%.2f per night", perNight)
}