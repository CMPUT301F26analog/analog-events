package com.example.birchbark

/**
 * A campsite an owner registered, as stored in the Firestore "campsites"
 * collection. Plain data only, so Firestore can save and load it. The checks
 * for what makes a campsite valid live in [CampsiteValidator].
 *
 * Every field has a default so Firestore can create an empty Campsite before
 * filling it in.
 *
 * Outstanding issues. Image upload isn't done yet so imageUrl is always null
 * for now. Ratings are Part 4 (US 01.13.01) so nothing updates the rating
 * fields yet.
 *
 * @property id Firestore document ID. Empty until the campsite is first saved.
 * @property ownerId FID of the owner, i.e. their document ID in the "users" collection.
 * @property name Campsite name. Required. US 02.01.02.
 * @property location Where the campsite is, like a park or town name. Required. US 02.01.02.
 * @property description Description of the campsite and its amenities. US 02.01.02.
 * @property maxOccupancy Max number of guests. Required, has to be at least 1. US 02.01.02.
 * @property latitude Map location, picked on a map and saved as coordinates. Null if not picked. US 02.01.02.
 * @property longitude Map location, picked on a map and saved as coordinates. Null if not picked. US 02.01.02.
 * @property imageUrl Link to the promo image. Image is optional, so null means no image. US 02.01.02.
 * @property tags Tags the owner adds, like "lakeside" or "pet friendly". US 02.01.02.
 * @property price Price per night, or null if the owner didn't set one. US 01.01.05.
 * @property rules Campsite rules shown to entrants. US 01.01.05, US 02.11.01.
 * @property cancellationPolicy Cancellation policy shown to entrants. US 01.01.05, US 02.11.01.
 * @property ratingTotal All the ratings added together. US 01.13.01.
 * @property ratingCount How many ratings it has. US 01.13.01.
 */
data class Campsite(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val location: String = "",
    val description: String = "",
    val maxOccupancy: Int = 0,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val imageUrl: String? = null,
    val tags: List<String> = listOf(),
    val price: Double? = null,
    val rules: String = "",
    val cancellationPolicy: String = "",
    val ratingTotal: Int = 0,
    val ratingCount: Int = 0,
) {

    /**
     * Average rating for the campsite list and details screens (US 01.01.04).
     *
     * @return the average rating, or null if it has no ratings yet so the
     *   screen can say no ratings instead of a number.
     */
    fun averageRating(): Double? =
        if (ratingCount == 0) null else ratingTotal.toDouble() / ratingCount

    /**
     * US 01.01.01. Checks if this campsite should show up for a search.
     * Campsites with a matching name or location show up. Doesn't care about
     * upper or lower case, and an empty search matches everything.
     *
     * @param keyword what the entrant typed in the search bar.
     * @return true if the name or location contains the keyword, false otherwise.
     */
    fun matchesSearch(keyword: String): Boolean {
        val search = keyword.trim()
        if (search.isEmpty()) {
            return true
        }
        return name.contains(search, ignoreCase = true) ||
                location.contains(search, ignoreCase = true)
    }
}