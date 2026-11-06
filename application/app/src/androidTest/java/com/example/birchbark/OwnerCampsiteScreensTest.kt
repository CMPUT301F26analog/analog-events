package com.example.birchbark

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.birchbark.model.Campsite
import com.example.birchbark.ui.owner.CampsiteDetailsContent
import com.example.birchbark.ui.owner.EditCampsiteForm
import com.example.birchbark.ui.owner.MyCampsitesList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Intent tests for the owner campsite screens.
 * Covers US 02.03.01, US 02.01.04 and US 02.01.03.
 *
 * Uses fake campsites and the drawing part of each screen, so these
 * don't need Firestore or the dev login.
 */
@RunWith(AndroidJUnit4::class)
class OwnerCampsiteScreensTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val whistlers = Campsite(
        id = "c1",
        ownerId = "owner123",
        name = "Whistlers Campground",
        location = "Jasper National Park",
        description = "Big wooded sites near the Athabasca River",
        maxOccupancy = 6,
        tags = listOf("riverside", "pet friendly"),
        price = 38.0,
        rules = "Quiet hours 10pm to 7am"
    )

    private val twoJack = Campsite(
        id = "c2",
        ownerId = "owner123",
        name = "Two Jack Lakeside",
        location = "Banff National Park",
        maxOccupancy = 4
    )

    // US 02.03.01: view my campsites

    @Test
    fun myCampsites_showsNameAndLocation() {
        composeRule.setContent {
            MyCampsitesList(campsites = listOf(whistlers, twoJack), onCampsiteClick = {})
        }
        composeRule.onNodeWithText("Whistlers Campground").assertIsDisplayed()
        composeRule.onNodeWithText("Jasper National Park").assertIsDisplayed()
        composeRule.onNodeWithText("Two Jack Lakeside").assertExists()
    }

    @Test
    fun myCampsites_showsMessage_whenNone() {
        composeRule.setContent {
            MyCampsitesList(campsites = listOf(), onCampsiteClick = {})
        }
        composeRule.onNodeWithTag("emptyMessage").assertIsDisplayed()
    }

    @Test
    fun myCampsites_tappingOneOpensIt() {
        var tapped: Campsite? = null
        composeRule.setContent {
            MyCampsitesList(campsites = listOf(whistlers), onCampsiteClick = { tapped = it })
        }
        composeRule.onNodeWithText("Whistlers Campground").performClick()
        assertEquals("c1", tapped?.id)
    }

    // US 02.01.04: view campsite details

    @Test
    fun details_showsSavedDetails() {
        composeRule.setContent {
            CampsiteDetailsContent(campsite = whistlers, onEditClick = {}, onBack = {})
        }
        composeRule.onNodeWithText("Whistlers Campground").assertIsDisplayed()
        composeRule.onNodeWithText("Jasper National Park").assertIsDisplayed()
        composeRule.onNodeWithText("riverside, pet friendly").assertExists()
        composeRule.onNodeWithText("$38.00").assertExists()
        composeRule.onNodeWithText("Quiet hours 10pm to 7am").assertExists()
        composeRule.onNodeWithText("No ratings").assertExists()
    }

    @Test
    fun details_showsNotSet_whenEmpty() {
        composeRule.setContent {
            CampsiteDetailsContent(campsite = twoJack, onEditClick = {}, onBack = {})
        }
        composeRule.onAllNodesWithText("Not set").onFirst().assertExists()
    }

    @Test
    fun details_updateButtonOpensEdit() {
        var clicked = false
        composeRule.setContent {
            CampsiteDetailsContent(campsite = whistlers, onEditClick = { clicked = true }, onBack = {})
        }
        composeRule.onNodeWithTag("editButton").performScrollTo().performClick()
        assertTrue(clicked)
    }

    // US 02.01.03: update campsite details

    @Test
    fun edit_startsFilledIn() {
        composeRule.setContent {
            EditCampsiteForm(campsite = whistlers, errors = listOf(), saving = false, onSave = {}, onCancel = {})
        }
        composeRule.onNodeWithText("Whistlers Campground").assertExists()
        composeRule.onNodeWithText("Jasper National Park").assertExists()
    }

    @Test
    fun edit_savesChangedName() {
        var saved: Campsite? = null
        composeRule.setContent {
            EditCampsiteForm(campsite = whistlers, errors = listOf(), saving = false, onSave = { saved = it }, onCancel = {})
        }
        composeRule.onNodeWithTag("nameField").performTextClearance()
        composeRule.onNodeWithTag("nameField").performTextInput("Whistlers Campground Test")
        composeRule.onNodeWithTag("saveButton").performScrollTo().performClick()

        assertEquals("Whistlers Campground Test", saved?.name)
        //same campsite, so the update goes to the same Firestore document
        assertEquals("c1", saved?.id)
    }

    @Test
    fun edit_doesNotSave_whenPriceNotNumber() {
        var saved: Campsite? = null
        composeRule.setContent {
            EditCampsiteForm(campsite = whistlers, errors = listOf(), saving = false, onSave = { saved = it }, onCancel = {})
        }
        composeRule.onNodeWithText("38.0").performScrollTo().performTextClearance()
        composeRule.onNodeWithText("Price per night (optional)").performTextInput("abc")
        composeRule.onNodeWithTag("saveButton").performScrollTo().performClick()

        assertNull(saved)
        composeRule.onNodeWithText("Price must be a number").assertExists()
    }

    @Test
    fun edit_showsRuleErrors() {
        composeRule.setContent {
            EditCampsiteForm(
                campsite = whistlers,
                errors = listOf("Name is required"),
                saving = false,
                onSave = {},
                onCancel = {}
            )
        }
        composeRule.onNodeWithText("Name is required").assertExists()
    }
}