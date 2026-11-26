package com.example.birchbark

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.birchbark.model.Campsite
import com.example.birchbark.ui.owner.CampsiteDetailsContent
import com.example.birchbark.ui.owner.EditCampsiteForm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Intent tests for the campsite map location.
 * Covers US 02.01.02 (map location) on the register and update form, and
 * showing the pin on the owner's details screen (US 02.01.04).
 *
 * Uses fake campsites and the drawing part of each screen, so these don't
 * need Firestore. Tapping the map itself isn't tested since the map is an
 * osmdroid view, so these check the text and buttons around it.
 */
@RunWith(AndroidJUnit4::class)
class CampsiteMapTest {

    @get:Rule
    val composeRule = createComposeRule()

    //Two Jack Lakeside in Banff, with a pin
    private val twoJack = Campsite(
        id = "c1",
        ownerId = "owner123",
        name = "Two Jack Lakeside",
        location = "Banff National Park",
        maxOccupancy = 4,
        latitude = 51.2349,
        longitude = -115.5051
    )

    //same campsite before the owner placed a pin
    private val noPin = twoJack.copy(latitude = null, longitude = null)

    @Test
    fun form_showsMap() {
        composeRule.setContent {
            EditCampsiteForm(campsite = noPin, errors = listOf(), saving = false, onSave = {}, onCancel = {})
        }
        composeRule.onNodeWithText("Place your Camp Pin on Maps!").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("mapPicker").assertExists()
    }

    @Test
    fun form_noPin_asksToTapMap() {
        composeRule.setContent {
            EditCampsiteForm(campsite = noPin, errors = listOf(), saving = false, onSave = {}, onCancel = {})
        }
        composeRule.onNodeWithTag("pinText").assertTextContains("Tap the map where your campsite is")
        composeRule.onNodeWithTag("removePinButton").assertDoesNotExist()
    }

    @Test
    fun form_savedPin_showsWhereItIs() {
        composeRule.setContent {
            EditCampsiteForm(campsite = twoJack, errors = listOf(), saving = false, onSave = {}, onCancel = {})
        }
        composeRule.onNodeWithTag("pinText").assertTextContains("Pin at 51.23490, -115.50510")
    }

    @Test
    fun form_keepsPin_whenSaving() {
        var saved: Campsite? = null
        composeRule.setContent {
            EditCampsiteForm(campsite = twoJack, errors = listOf(), saving = false, onSave = { saved = it }, onCancel = {})
        }
        composeRule.onNodeWithTag("saveButton").performScrollTo().performClick()
        assertEquals(51.2349, saved!!.latitude!!, 0.00001)
        assertEquals(-115.5051, saved!!.longitude!!, 0.00001)
    }

    @Test
    fun form_removePin_savesNoLocation() {
        var saved: Campsite? = null
        composeRule.setContent {
            EditCampsiteForm(campsite = twoJack, errors = listOf(), saving = false, onSave = { saved = it }, onCancel = {})
        }
        composeRule.onNodeWithTag("removePinButton").performScrollTo().performClick()
        composeRule.onNodeWithTag("pinText").assertTextContains("Tap the map where your campsite is")
        composeRule.onNodeWithTag("saveButton").performScrollTo().performClick()
        assertNull(saved!!.latitude)
        assertNull(saved!!.longitude)
    }

    @Test
    fun details_showsPinOnMap() {
        composeRule.setContent {
            CampsiteDetailsContent(campsite = twoJack, onEditClick = {}, onBack = {})
        }
        composeRule.onNodeWithTag("mapLocationText").performScrollTo().assertTextContains("51.23490, -115.50510")
        composeRule.onNodeWithTag("detailsMap").assertExists()
    }

    @Test
    fun details_noPin_showsNotSet() {
        composeRule.setContent {
            CampsiteDetailsContent(campsite = noPin, onEditClick = {}, onBack = {})
        }
        composeRule.onNodeWithTag("detailsMap").assertDoesNotExist()
        composeRule.onNodeWithText("Map location").performScrollTo().assertIsDisplayed()
    }
}