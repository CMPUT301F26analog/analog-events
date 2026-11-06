package com.example.birchbark

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.birchbark.model.Campsite
import com.example.birchbark.ui.owner.EditCampsiteForm
import com.example.birchbark.ui.owner.MyCampsitesList
import com.example.birchbark.ui.owner.RegistrationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Intent tests for registering a campsite.
 * Covers US 02.01.01 and US 02.01.02.
 *
 * Uses the form and result screens directly so these don't need Firestore.
 */
@RunWith(AndroidJUnit4::class)
class RegisterCampsiteTest {

    @get:Rule
    val composeRule = createComposeRule()

    // US 02.01.01: owner can open a form to register a new campsite

    @Test
    fun myCampsites_registerButtonOpensForm() {
        var clicked = false
        composeRule.setContent {
            MyCampsitesList(campsites = listOf(), onCampsiteClick = {}, onRegisterClick = { clicked = true })
        }
        composeRule.onNodeWithTag("registerButton").performClick()
        assertTrue(clicked)
    }

    @Test
    fun registerForm_showsRegisterLabels() {
        composeRule.setContent {
            EditCampsiteForm(
                campsite = Campsite(ownerId = "owner123"),
                errors = listOf(),
                saving = false,
                onSave = {},
                onCancel = {},
                title = "Register my Campsite",
                buttonText = "Register my Campsite!"
            )
        }
        composeRule.onNodeWithText("Register my Campsite").assertIsDisplayed()
        composeRule.onNodeWithText("Register my Campsite!").assertExists()
    }

    // US 02.01.01: saving stores the campsite under that owner

    @Test
    fun registerForm_savesNewCampsiteUnderOwner() {
        var saved: Campsite? = null
        composeRule.setContent {
            EditCampsiteForm(
                campsite = Campsite(ownerId = "owner123"),
                errors = listOf(),
                saving = false,
                onSave = { saved = it },
                onCancel = {},
                title = "Register my Campsite",
                buttonText = "Register my Campsite!"
            )
        }
        composeRule.onNodeWithTag("nameField").performTextInput("Whistlers Campground")
        composeRule.onNodeWithTag("locationField").performTextInput("Jasper National Park")
        composeRule.onNodeWithTag("maxOccupancyField").performScrollTo().performTextInput("6")
        composeRule.onNodeWithTag("saveButton").performScrollTo().performClick()

        assertEquals("owner123", saved?.ownerId)
        assertEquals("Whistlers Campground", saved?.name)
        assertEquals("Jasper National Park", saved?.location)
        assertEquals(6, saved?.maxOccupancy)
    }

    // US 02.01.02: owner gets an error if a required field is missing

    @Test
    fun registerForm_showsErrorsForMissingFields() {
        composeRule.setContent {
            EditCampsiteForm(
                campsite = Campsite(ownerId = "owner123"),
                errors = listOf("Name is required", "Location is required"),
                saving = false,
                onSave = {},
                onCancel = {},
                title = "Register my Campsite",
                buttonText = "Register my Campsite!"
            )
        }
        composeRule.onNodeWithText("Name is required").assertExists()
        composeRule.onNodeWithText("Location is required").assertExists()
    }

    // after registering, the owner can go back to home

    @Test
    fun registrationComplete_backToHome() {
        var clicked = false
        composeRule.setContent {
            RegistrationResult(
                title = "Registration Complete for your Campsite!",
                message = "Welcome, Whistlers Campground!",
                onBackToHome = { clicked = true }
            )
        }
        composeRule.onNodeWithText("Registration Complete for your Campsite!").assertIsDisplayed()
        composeRule.onNodeWithText("Back to Home").performClick()
        assertTrue(clicked)
    }
}