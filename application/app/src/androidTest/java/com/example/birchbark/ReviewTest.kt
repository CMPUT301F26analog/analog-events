package com.example.birchbark

import com.example.birchbark.model.Campsite
import com.example.birchbark.model.Review
import com.example.birchbark.model.ReviewCalculator
import com.example.birchbark.model.ReviewRepository
import com.example.birchbark.model.ReviewValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// See [TBD 2] in LLM.md

/**
 * Unit tests for reviews: the validator, the rating maths, and that an invalid
 * review is refused before Firestore is touched.
 * Covers US 01.13.01: an entrant can give a rating, a review, or both, and the
 * campsite's overall rating includes it.
 */
class ReviewTest {

    /** A review that passes every check, tests change one field at a time. */
    private val valid = Review(
        campsiteId = "c1",
        entrantId = "entrant456",
        entrantName = "Evelyn A.",
        rating = 4,
        comment = "Great spot by the river"
    )

    // validator: rating, review, or both

    @Test
    fun validatorPasses_withRatingAndReview() {
        assertTrue(ReviewValidator.validate(valid).isEmpty())
    }

    @Test
    fun validatorPasses_withRatingOnly() {
        assertTrue(ReviewValidator.validate(valid.copy(comment = "")).isEmpty())
    }

    @Test
    fun validatorPasses_withReviewOnly() {
        assertTrue(ReviewValidator.validate(valid.copy(rating = null)).isEmpty())
    }

    @Test
    fun validatorFails_withNeitherRatingNorReview() {
        val errors = ReviewValidator.validate(valid.copy(rating = null, comment = "   "))
        assertEquals(listOf("Add a rating, a review, or both"), errors)
    }

    // validator: stars

    @Test
    fun validatorPasses_forOneAndFiveStars() {
        assertTrue(ReviewValidator.validate(valid.copy(rating = 1)).isEmpty())
        assertTrue(ReviewValidator.validate(valid.copy(rating = 5)).isEmpty())
    }

    @Test
    fun validatorFails_whenRatingIsZero() {
        val errors = ReviewValidator.validate(valid.copy(rating = 0))
        assertEquals(listOf("Rating must be 1 to 5 stars"), errors)
    }

    @Test
    fun validatorFails_whenRatingIsSix() {
        val errors = ReviewValidator.validate(valid.copy(rating = 6))
        assertEquals(listOf("Rating must be 1 to 5 stars"), errors)
    }

    // validator: ids

    @Test
    fun validatorFails_whenNoCampsite() {
        val errors = ReviewValidator.validate(valid.copy(campsiteId = ""))
        assertEquals(listOf("Campsite is required"), errors)
    }

    @Test
    fun validatorFails_whenNoEntrant() {
        val errors = ReviewValidator.validate(valid.copy(entrantId = ""))
        assertEquals(listOf("Entrant is required"), errors)
    }

    // validator: length

    @Test
    fun validatorPasses_whenReviewIsExactlyTheLimit() {
        val comment = "a".repeat(ReviewValidator.MAX_COMMENT_LENGTH)
        assertTrue(ReviewValidator.validate(valid.copy(comment = comment)).isEmpty())
    }

    @Test
    fun validatorFails_whenReviewIsOneOverTheLimit() {
        val comment = "a".repeat(ReviewValidator.MAX_COMMENT_LENGTH + 1)
        val errors = ReviewValidator.validate(valid.copy(comment = comment))
        assertEquals(listOf("Review can be at most 500 characters"), errors)
    }

    // spaces at the ends aren't saved, so they don't count
    @Test
    fun validatorIgnoresSpacesAroundReviewLength() {
        val comment = "  " + "a".repeat(ReviewValidator.MAX_COMMENT_LENGTH) + "  "
        assertTrue(ReviewValidator.validate(valid.copy(comment = comment)).isEmpty())
    }

    // rating maths: the campsite's overall rating includes it

    /** Two ratings that add up to 9, so the average is 4.5. */
    private val rated = Campsite(id = "c1", ratingTotal = 9, ratingCount = 2)

    @Test
    fun firstRating_becomesTheAverage() {
        val updated = ReviewCalculator.update(Campsite(id = "c1"), oldRating = null, newRating = 4)
        assertEquals(4, updated.ratingTotal)
        assertEquals(1, updated.ratingCount)
        assertEquals(4.0, updated.averageRating()!!, 0.0001)
    }

    @Test
    fun newRating_isAddedToTheOthers() {
        val updated = ReviewCalculator.update(rated, oldRating = null, newRating = 3)
        assertEquals(12, updated.ratingTotal)
        assertEquals(3, updated.ratingCount)
        assertEquals(4.0, updated.averageRating()!!, 0.0001)
    }

    // changing a rating swaps the old one for the new one, it doesn't count twice
    @Test
    fun changedRating_replacesTheOldOne() {
        val updated = ReviewCalculator.update(rated, oldRating = 4, newRating = 5)
        assertEquals(10, updated.ratingTotal)
        assertEquals(2, updated.ratingCount)
        assertEquals(5.0, updated.averageRating()!!, 0.0001)
    }

    @Test
    fun removedRating_isTakenOut() {
        val updated = ReviewCalculator.update(rated, oldRating = 4, newRating = null)
        assertEquals(5, updated.ratingTotal)
        assertEquals(1, updated.ratingCount)
    }

    // a review with only text doesn't change the rating
    @Test
    fun reviewWithoutRating_leavesTheTotalsAlone() {
        val updated = ReviewCalculator.update(rated, oldRating = null, newRating = null)
        assertEquals(rated, updated)
    }

    @Test
    fun removingTheOnlyRating_leavesNoRatings() {
        val one = Campsite(id = "c1", ratingTotal = 4, ratingCount = 1)
        val updated = ReviewCalculator.update(one, oldRating = 4, newRating = null)
        assertNull(updated.averageRating())
    }

    // bad data in the database shouldn't make the totals go negative
    @Test
    fun totals_neverGoBelowZero() {
        val updated = ReviewCalculator.update(Campsite(id = "c1"), oldRating = 4, newRating = null)
        assertEquals(0, updated.ratingTotal)
        assertEquals(0, updated.ratingCount)
    }

    @Test
    fun updateKeepsTheRestOfTheCampsite() {
        val campsite = rated.copy(name = "Whistlers Campground", location = "Jasper National Park")
        val updated = ReviewCalculator.update(campsite, oldRating = null, newRating = 5)
        assertEquals("Whistlers Campground", updated.name)
        assertEquals("Jasper National Park", updated.location)
    }

    // repository: an invalid review is refused before touching Firestore

    /** Remembers which callback function was called. */
    private class RecordingCallback : ReviewRepository.SaveCallback {
        var savedReview: Review? = null
        var errors: List<String>? = null
        var failure: Exception? = null

        override fun onSuccess(review: Review, campsite: Campsite) {
            savedReview = review
        }

        override fun onInvalid(errors: List<String>) {
            this.errors = errors
        }

        override fun onError(error: Exception) {
            failure = error
        }
    }

    @Test
    fun save_callsOnInvalidOnly_whenNeitherRatingNorReview() {
        val callback = RecordingCallback()
        ReviewRepository().saveReview(valid.copy(rating = null, comment = ""), callback)

        assertEquals(listOf("Add a rating, a review, or both"), callback.errors)
        assertNull(callback.savedReview)
        assertNull(callback.failure)
    }

    @Test
    fun save_reportsEveryProblem_whenEverythingMissing() {
        val callback = RecordingCallback()
        ReviewRepository().saveReview(Review(), callback)

        //no campsite, no entrant, and neither a rating nor a review
        assertEquals(3, callback.errors?.size)
        assertNull(callback.savedReview)
    }
}
