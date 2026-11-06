package com.example.birchbark

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.birchbark.model.Campsite
import com.example.birchbark.ui.screens.CampsiteSummary
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// See [5] in LLM.md, used to help write Test for CampsiteSummary.kt

/**
 * Intent tests for the campsite summary card.
 * Covers US 01.01.04, every campsite in the list shows its name, location,
 * max occupancy and rating, and says no ratings if it has none.
 *
 * Uses a fake campsite so these don't need Firestore.
 */
@RunWith(AndroidJUnit4::class)
class CampsiteSummaryTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val whistlers = Campsite(
        id = "c1",
        name = "Whistlers Campground",
        location = "Jasper National Park",
        maxOccupancy = 6,
        ratingTotal = 9,
        ratingCount = 2
    )

    @Test
    fun summary_showsNameLocationAndGuests() {
        composeRule.setContent {
            CampsiteSummary(campsite = whistlers, onClick = {})
        }
        composeRule.onNodeWithText("Whistlers Campground").assertIsDisplayed()
        composeRule.onNodeWithText("Jasper National Park").assertIsDisplayed()
        composeRule.onNodeWithText("Up to 6 guests").assertIsDisplayed()
    }

    @Test
    fun summary_showsRating() {
        composeRule.setContent {
            CampsiteSummary(campsite = whistlers, onClick = {})
        }
        //9 out of 2 ratings is 4.5
        composeRule.onNodeWithText("★ 4.5").assertIsDisplayed()
    }

    @Test
    fun summary_saysNoRatings_whenNone() {
        composeRule.setContent {
            CampsiteSummary(campsite = whistlers.copy(ratingTotal = 0, ratingCount = 0), onClick = {})
        }
        composeRule.onNodeWithText("No ratings").assertIsDisplayed()
    }

    @Test
    fun summary_viewButtonOpensIt() {
        var clicks = 0
        composeRule.setContent {
            CampsiteSummary(campsite = whistlers, onClick = { clicks++ })
        }
        composeRule.onNodeWithText("View").performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun summary_tappingCardOpensIt() {
        var clicks = 0
        composeRule.setContent {
            CampsiteSummary(campsite = whistlers, onClick = { clicks++ })
        }
        composeRule.onNodeWithText("Whistlers Campground").performClick()
        assertEquals(1, clicks)
    }
}