package com.example.birchbark

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.birchbark.model.Review
import com.example.birchbark.ui.entrant.ReviewScreen
import com.example.birchbark.ui.theme.BirchBarkTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// See [TBD 1] in LLM.md

/**
 * Intent tests for the review screen.
 * Covers US 01.13.01: the entrant can give a rating, a review, or both.
 *
 * Uses fake data so these don't need Firestore. That the review is saved and the
 * campsite's overall rating updates is checked by the rating maths in ReviewTest
 * and by looking at the campsite and its reviews in the Firebase console.
 */
@RunWith(AndroidJUnit4::class)
class ReviewScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun show(
        existing: Review? = null,
        loading: Boolean = false,
        saving: Boolean = false,
        errors: List<String> = emptyList(),
        successMessage: String? = null,
        onSubmit: (Int?, String) -> Unit = { _, _ -> },
        onBack: () -> Unit = {},
        canReview: Boolean = true
    ) {
        composeRule.setContent {
            BirchBarkTheme {
                ReviewScreen(
                    campsiteName = "Whistlers Campground",
                    existing = existing,
                    loading = loading,
                    saving = saving,
                    errors = errors,
                    successMessage = successMessage,
                    onSubmit = onSubmit,
                    onBack = onBack,
                    canReview = canReview
                )
            }
        }
    }

    @Test
    fun screen_showsCampsiteName() {
        show()
        composeRule.onNodeWithText("Whistlers Campground").assertIsDisplayed()
    }

    // rating, review, or both: the button needs at least one

    @Test
    fun postButton_isDisabled_untilThereIsARatingOrReview() {
        show()
        composeRule.onNodeWithTag("postReviewButton").assertIsNotEnabled()
    }

    @Test
    fun rating_only_canBePosted() {
        var submitted: Pair<Int?, String>? = null
        show(onSubmit = { rating, comment -> submitted = rating to comment })

        composeRule.onNodeWithTag("star-4").performClick()
        composeRule.onNodeWithTag("postReviewButton").assertIsEnabled().performClick()

        assertEquals(4 to "", submitted)
    }

    @Test
    fun review_only_canBePosted() {
        var submitted: Pair<Int?, String>? = null
        show(onSubmit = { rating, comment -> submitted = rating to comment })

        composeRule.onNodeWithTag("reviewField").performTextInput("Lovely and quiet")
        composeRule.onNodeWithTag("postReviewButton").assertIsEnabled().performClick()

        assertEquals(Pair<Int?, String>(null, "Lovely and quiet"), submitted)
    }

    @Test
    fun ratingAndReview_canBePostedTogether() {
        var submitted: Pair<Int?, String>? = null
        show(onSubmit = { rating, comment -> submitted = rating to comment })

        composeRule.onNodeWithTag("star-5").performClick()
        composeRule.onNodeWithTag("reviewField").performTextInput("Best weekend ever")
        composeRule.onNodeWithTag("postReviewButton").performClick()

        assertEquals(5 to "Best weekend ever", submitted)
    }

    // the stars

    @Test
    fun tappingAStar_fillsThatManyStars() {
        show()
        composeRule.onNodeWithTag("star-3").performClick()

        composeRule.onNodeWithTag("star-1").assertTextEquals("★")
        composeRule.onNodeWithTag("star-3").assertTextEquals("★")
        composeRule.onNodeWithTag("star-4").assertTextEquals("☆")
        composeRule.onNodeWithTag("star-5").assertTextEquals("☆")
    }

    @Test
    fun clearRating_removesTheStars() {
        show()
        composeRule.onNodeWithTag("star-2").performClick()
        composeRule.onNodeWithText("Clear rating").performClick()

        composeRule.onNodeWithTag("star-2").assertTextEquals("☆")
        composeRule.onNodeWithTag("postReviewButton").assertIsNotEnabled()
    }

    // an earlier review can be updated

    @Test
    fun existingReview_isShownAndCanBeUpdated() {
        show(existing = Review(campsiteId = "c1", entrantId = "e1", rating = 3, comment = "Nice"))

        composeRule.onNodeWithText("Update Review").assertIsDisplayed()
        composeRule.onNodeWithTag("star-3").assertTextEquals("★")
        composeRule.onNodeWithTag("star-4").assertTextEquals("☆")
        composeRule.onNodeWithTag("reviewField").assertTextContains("Nice")
    }

    @Test
    fun newReview_saysPostReview() {
        show()
        composeRule.onNodeWithText("Post Review").assertIsDisplayed()
    }

    // messages and states

    @Test
    fun errors_areShown() {
        show(errors = listOf("Rating must be 1 to 5 stars"))
        composeRule.onNodeWithText("Rating must be 1 to 5 stars").assertIsDisplayed()
    }

    @Test
    fun successMessage_isShown() {
        show(successMessage = "Review posted. Overall rating is now ★ 4.5")
        composeRule.onNodeWithText("Review posted. Overall rating is now ★ 4.5").assertIsDisplayed()
    }

    @Test
    fun whileSaving_buttonSaysSavingAndIsDisabled() {
        show(saving = true)
        composeRule.onNodeWithText("Saving...").assertIsDisplayed()
        composeRule.onNodeWithTag("postReviewButton").assertIsNotEnabled()
    }

    @Test
    fun whileLoading_showsLoading() {
        show(loading = true)
        composeRule.onNodeWithText("Loading...").assertIsDisplayed()
    }

    @Test
    fun back_callsOnBack() {
        var wentBack = false
        show(onBack = { wentBack = true })
        composeRule.onNodeWithText("Back").performClick()
        assertTrue(wentBack)
    }

    @Test
    fun whenIneligibleToReview_formAndPostButtonAreDisabled() {
        show(
            canReview = false,
            errors = listOf("You can only rate or review a campsite after your confirmed stay has ended.")
        )
        composeRule.onNodeWithText("You can only rate or review a campsite after your confirmed stay has ended.")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("reviewField").assertIsNotEnabled()
        composeRule.onNodeWithTag("postReviewButton").assertIsNotEnabled()
    }

}
