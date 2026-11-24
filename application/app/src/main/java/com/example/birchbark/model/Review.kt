package com.example.birchbark.model

/**
 * An entrant's rating and/or written review of a campsite, as stored in the
 * "reviews" subcollection of that campsite in Firestore
 * (campsites/{campsiteId}/reviews/{entrantId}). The document ID is the
 * entrant's ID, so an entrant has at most one review per campsite and posting
 * again updates it. Plain data only, so Firestore can save and load it. The
 * checks live in [ReviewValidator].
 *
 * An entrant can give a rating, a review, or both (US 01.13.01 and 01.13.02). Only the
 * rating counts towards the campsite's overall rating ([RatingCalculator]).
 *
 * Every field has a default so Firestore can create an empty Review before
 * filling it in.
 *
 * Outstanding issues. Ratings are whole stars (1 to 5). The half stars in the
 * mockup would need the campsite's rating total stored in half stars. Photos
 * and videos on reviews aren't here yet. Nothing checks the entrant actually
 * stayed at the campsite. Showing the list of reviews comes later.
 *
 * @property campsiteId ID of the reviewed campsite ([Campsite.id]).
 * @property entrantId FID of the entrant, i.e. their document ID in the "users" collection.
 * @property entrantName The entrant's name when they posted, so a list of reviews
 *   can show it without loading every profile.
 * @property rating Whole stars from 1 to 5, or null if they only wrote a review.
 * @property comment The written review, or blank if they only gave a rating.
 * @property postedAt When the review was last posted or updated, in epoch millis.
 */
data class Review(
    val campsiteId: String = "",
    val entrantId: String = "",
    val entrantName: String = "",
    val rating: Int? = null,
    val comment: String = "",
    val postedAt: Long = 0L,
)
