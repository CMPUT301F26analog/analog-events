package com.example.birchbark

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.birchbark.model.Campsite
import com.example.birchbark.ui.entrant.SearchCampsitesContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Intent tests for the entrant search screen.
 * Covers US 01.01.01.
 *
 * Uses fake campsites so these don't need Firestore.
 */
@RunWith(AndroidJUnit4::class)
class SearchCampsitesTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val campsites = listOf(
        Campsite(id = "c1", name = "Whistlers Campground", location = "Jasper National Park", maxOccupancy = 6),
        Campsite(id = "c2", name = "Two Jack Lakeside", location = "Banff National Park", maxOccupancy = 4),
        Campsite(id = "c3", name = "Dinosaur Provincial Park Campground", location = "Near Brooks", maxOccupancy = 8)
    )

    @Test
    fun search_showsEveryCampsite_beforeTyping() {
        composeRule.setContent {
            SearchCampsitesContent(campsites = campsites, onCampsiteClick = {})
        }
        composeRule.onAllNodesWithTag("searchResult").assertCountEquals(3)
    }

    // campsites with a matching name or location show up

    @Test
    fun search_matchesName() {
        composeRule.setContent {
            SearchCampsitesContent(campsites = campsites, onCampsiteClick = {})
        }
        composeRule.onNodeWithTag("searchField").performTextInput("whistlers")
        composeRule.onAllNodesWithTag("searchResult").assertCountEquals(1)
        composeRule.onNodeWithText("Whistlers Campground").assertIsDisplayed()
    }

    @Test
    fun search_matchesLocation() {
        composeRule.setContent {
            SearchCampsitesContent(campsites = campsites, onCampsiteClick = {})
        }
        composeRule.onNodeWithTag("searchField").performTextInput("Banff")
        composeRule.onAllNodesWithTag("searchResult").assertCountEquals(1)
        composeRule.onNodeWithText("Two Jack Lakeside").assertIsDisplayed()
    }

    // if nothing matches, it shows a no campsites found message

    @Test
    fun search_showsNotFound_whenNothingMatches() {
        composeRule.setContent {
            SearchCampsitesContent(campsites = campsites, onCampsiteClick = {})
        }
        composeRule.onNodeWithTag("searchField").performTextInput("Waterton")
        composeRule.onNodeWithTag("notFound").assertIsDisplayed()
        composeRule.onNodeWithText("Site not found").assertIsDisplayed()
    }

    @Test
    fun search_clearShowsEverythingAgain() {
        composeRule.setContent {
            SearchCampsitesContent(campsites = campsites, onCampsiteClick = {})
        }
        composeRule.onNodeWithTag("searchField").performTextInput("Banff")
        composeRule.onNodeWithText("Clear").performClick()
        composeRule.onAllNodesWithTag("searchResult").assertCountEquals(3)
    }

    @Test
    fun search_tappingResultOpensIt() {
        var tapped: Campsite? = null
        composeRule.setContent {
            SearchCampsitesContent(campsites = campsites, onCampsiteClick = { tapped = it })
        }
        composeRule.onNodeWithText("Dinosaur Provincial Park Campground").performClick()
        assertEquals("c3", tapped?.id)
    }
}