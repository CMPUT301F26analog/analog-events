package com.example.birchbark

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.birchbark.model.Review
import com.example.birchbark.model.ReviewRepository
import com.example.birchbark.ui.entrant.ReviewsScreen
import com.example.birchbark.ui.theme.BirchBarkTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// See [TBD 4] in LLM.md

/**
 * Intent tests for the campground reviews list screen (ReviewsScreen).
 * Verifies displaying overall ratings, individual review cards, empty/loading/error
 * states, and clicking through to write or edit a review.
 */
@RunWith(AndroidJUnit4::class)
class ReviewsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun show(
        reviews: List<Review> = emptyList(),
        loading: Boolean = false,
        errorMessage: String? = null,
        onBack: () -> Unit = {},
        onWriteReview: () -> Unit = {}
    ) {
        composeRule.setContent {
            BirchBarkTheme {
                ReviewsScreen(
                    campsiteName = "Whistlers Campground",
                    overallRatingLabel = "★ 4.5 (2 reviews)",
                    reviews = reviews,
                    loading = loading,
                    errorMessage = errorMessage,
                    onBack = onBack,
                    onWriteReview = onWriteReview
                )
            }
        }
    }

    @Test
    fun screen_showsCampsiteNameAndOverallRating() {
        show()
        composeRule.onNodeWithText("Whistlers Campground").assertIsDisplayed()
        composeRule.onNodeWithText("★ 4.5 (2 reviews)").assertIsDisplayed()
    }

    @Test
    fun emptyState_showsNoReviewsMessage() {
        show(reviews = emptyList())
        composeRule.onNodeWithText("No reviews yet. Be the first to review this campground!")
            .assertIsDisplayed()
    }

    @Test
    fun reviews_areDisplayedWithAuthorStarsAndComment() {
        val sampleReviews = listOf(
            Review(
                campsiteId = "c1",
                entrantId = "u1",
                entrantName = "Alex M.",
                rating = 4,
                comment = "Great lakeside views!"
            ),
            Review(
                campsiteId = "c1",
                entrantId = "u2",
                entrantName = "",
                rating = null,
                comment = "Quiet spot in the woods."
            )
        )
        show(reviews = sampleReviews)

        composeRule.onNodeWithText("Alex M.").assertIsDisplayed()
        composeRule.onNodeWithText("★★★★☆").assertIsDisplayed()
        composeRule.onNodeWithText("Great lakeside views!").assertIsDisplayed()

        // Blank entrantName falls back to Anonymous Camper
        composeRule.onNodeWithText("Anonymous Camper").assertIsDisplayed()
        composeRule.onNodeWithText("Quiet spot in the woods.").assertIsDisplayed()
    }

    @Test
    fun writeReviewButton_callsOnWriteReview() {
        var clicked = false
        show(onWriteReview = { clicked = true })

        composeRule.onNodeWithText("Write or Edit Your Review").performClick()
        assertTrue(clicked)
    }

    @Test
    fun backButton_callsOnBack() {
        var wentBack = false
        show(onBack = { wentBack = true })

        composeRule.onNodeWithText("Back").performClick()
        assertTrue(wentBack)
    }

    @Test
    fun loadingState_isShown() {
        show(loading = true)
        composeRule.onNodeWithText("Loading reviews...").assertIsDisplayed()
    }

    @Test
    fun errorState_isShown() {
        show(errorMessage = "Couldn't load reviews.")
        composeRule.onNodeWithText("Couldn't load reviews.").assertIsDisplayed()
    }


    @Test
    fun getAllReviews_withBlankCampsiteId_returnsEmptyListImmediately() {
        var result: List<Review>? = null
        ReviewRepository().getAllReviews("", object : ReviewRepository.LoadAllCallback {
            override fun onLoaded(reviews: List<Review>) {
                result = reviews
            }
            override fun onError(error: Exception) {}
        })
        assertEquals(emptyList<Review>(), result)
    }
}
