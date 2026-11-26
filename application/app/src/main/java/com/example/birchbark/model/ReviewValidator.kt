package com.example.birchbark.model

/**
 * Checks a review has what it needs before it's saved. Call this before saving.
 *
 * One small function instead of one object per rule (like [CampsiteValidator])
 * because there are only a few checks.
 */
object ReviewValidator {

    /** The longest written review allowed, in characters (not counting spaces at the ends). */
    const val MAX_COMMENT_LENGTH = 500

    /**
     * Checks a review. A review needs a rating, some written text, or both.
     *
     * @param review the review to check.
     * @return the message of every check that failed, so all the problems show at
     *   once. An empty list means the review is valid and safe to save.
     */
    fun validate(review: Review): List<String> {
        val errors = mutableListOf<String>()

        if (review.campsiteId.isBlank()) {
            errors.add("Campsite is required")
        }
        if (review.entrantId.isBlank()) {
            errors.add("Entrant is required")
        }

        //rating is optional, but if it's there it has to be a real star count
        val rating = review.rating
        if (rating != null && rating !in 1..5) {
            errors.add("Rating must be 1 to 5 stars")
        }

        //review text is optional too, but there has to be a rating or some text
        val comment = review.comment.trim()
        if (rating == null && comment.isEmpty()) {
            errors.add("Add a rating, a review, or both")
        }
        if (comment.length > MAX_COMMENT_LENGTH) {
            errors.add("Review can be at most $MAX_COMMENT_LENGTH characters")
        }

        return errors
    }
}