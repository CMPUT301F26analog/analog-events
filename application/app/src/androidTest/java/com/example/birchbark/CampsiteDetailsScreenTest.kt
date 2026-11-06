package com.example.birchbark

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.birchbark.model.Campsite
import com.example.birchbark.ui.screens.CampsiteDetailsScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// See [3] in LLM.md, used to help write Test for CampsiteDetailsScreen.kt

/**
 * Intent tests for the entrant campsite details screen.
 * Covers US 01.01.05, the details screen shows the name, location,
 * description, max occupancy, price, rating, rules and cancellation policy,
 * shows the promo image if there is one, and the entrant can go back.
 *
 * Uses a fake campsite so these don't need Firestore.
 */
@RunWith(AndroidJUnit4::class)
class CampsiteDetailsTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val whistlers = Campsite(
        id = "c1",
        name = "Whistlers Campground",
        location = "Jasper National Park",
        description = "Big wooded sites near the Athabasca River",
        maxOccupancy = 6,
        price = 38.0,
        rules = "Quiet hours 10pm to 7am",
        cancellationPolicy = "Full refund if you cancel at least 3 days before",
        ratingTotal = 9,
        ratingCount = 2
    )

    /** Shows the details screen with no lottery, using whatever back action the test gives it. */
    private fun showDetails(campsite: Campsite, onBack: () -> Unit = {}) {
        composeRule.setContent {
            CampsiteDetailsScreen(
                campsite = campsite,
                lottery = null,
                joined = false,
                isFull = false,
                message = null,
                onBack = onBack,
                onJoin = {},
                onLeave = {}
            )
        }
    }

    @Test
    fun details_showsMainInfo() {
        showDetails(whistlers)
        composeRule.onNodeWithText("Whistlers Campground").assertIsDisplayed()
        composeRule.onNodeWithText("Jasper National Park").assertIsDisplayed()
        composeRule.onNodeWithText("Up to 6 guests").assertIsDisplayed()
        composeRule.onNodeWithText("$38.00 per night").assertIsDisplayed()
        composeRule.onNodeWithText("★ 4.5").assertIsDisplayed()
    }

    @Test
    fun details_saysPriceNotSet_whenNoPrice() {
        showDetails(whistlers.copy(price = null))
        composeRule.onNodeWithText("Price not set").assertIsDisplayed()
    }

    @Test
    fun details_showsDescriptionFirst() {
        showDetails(whistlers)
        composeRule.onNodeWithText("Big wooded sites near the Athabasca River").assertIsDisplayed()
    }

    @Test
    fun details_rulesTabShowsRules() {
        showDetails(whistlers)
        composeRule.onNodeWithText("Rules").performClick()
        composeRule.onNodeWithText("Quiet hours 10pm to 7am").assertIsDisplayed()
    }

    @Test
    fun details_cancellationTabShowsPolicy() {
        showDetails(whistlers)
        composeRule.onNodeWithText("Cancellation").performClick()
        composeRule.onNodeWithText("Full refund if you cancel at least 3 days before").assertIsDisplayed()
    }

    @Test
    fun details_showsPromoImage_whenOwnerAddedOne() {
        showDetails(whistlers.copy(imageUrl = "https://example.com/whistlers.jpg"))
        composeRule.onNodeWithText("Promo Image").assertIsDisplayed()
    }

    @Test
    fun details_noPromoImage_whenNone() {
        showDetails(whistlers.copy(imageUrl = null))
        composeRule.onNodeWithText("Promo Image").assertDoesNotExist()
    }

    @Test
    fun details_backButtonGoesBack() {
        var backs = 0
        showDetails(whistlers, onBack = { backs++ })
        composeRule.onNodeWithText("Back").performClick()
        assertEquals(1, backs)
    }
}