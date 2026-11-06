package com.example.birchbark

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.birchbark.model.Campsite
import com.example.birchbark.ui.entrant.ReservationRulesConfirmation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReservationRulesConfirmationTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val campsite = Campsite(
        id = "campsite-1",
        name = "Dinosaur Campground",
        rules = "No campfires after 10 PM.",
        cancellationPolicy = "Cancel at least 48 hours in advance."
    )

    @Test
    fun campsiteRulesAreDisplayed() {
        composeRule.setContent {
            ReservationRulesConfirmation(
                campsite = campsite,
                onConfirm = {}
            )
        }

        composeRule.onNodeWithTag("campsiteRules")
            .assertTextEquals("No campfires after 10 PM.")
    }

    @Test
    fun cancellationPolicyIsDisplayed() {
        composeRule.setContent {
            ReservationRulesConfirmation(
                campsite = campsite,
                onConfirm = {}
            )
        }

        composeRule.onNodeWithTag("cancellationPolicy")
            .assertTextEquals("Cancel at least 48 hours in advance.")
    }

    @Test
    fun confirmButtonIsInitiallyDisabled() {
        composeRule.setContent {
            ReservationRulesConfirmation(
                campsite = campsite,
                onConfirm = {}
            )
        }

        composeRule.onNodeWithTag("confirmReservationButton")
            .assertIsNotEnabled()
    }

    @Test
    fun checkingAgreementEnablesConfirmation() {
        composeRule.setContent {
            ReservationRulesConfirmation(
                campsite = campsite,
                onConfirm = {}
            )
        }

        composeRule.onNodeWithTag("acceptRulesCheckbox")
            .performClick()

        composeRule.onNodeWithTag("confirmReservationButton")
            .assertIsEnabled()
    }

    @Test
    fun uncheckingAgreementDisablesConfirmation() {
        composeRule.setContent {
            ReservationRulesConfirmation(
                campsite = campsite,
                onConfirm = {}
            )
        }

        composeRule.onNodeWithTag("acceptRulesCheckbox")
            .performClick()

        composeRule.onNodeWithTag("acceptRulesCheckbox")
            .performClick()

        composeRule.onNodeWithTag("confirmReservationButton")
            .assertIsNotEnabled()
    }

    @Test
    fun confirmingPassesAgreementToParent() {
        var confirmed = false
        var callCount = 0

        composeRule.setContent {
            ReservationRulesConfirmation(
                campsite = campsite,
                onConfirm = {
                    confirmed = it
                    callCount++
                }
            )
        }

        assertFalse(confirmed)
        assertEquals(0, callCount)

        composeRule.onNodeWithTag("acceptRulesCheckbox")
            .performClick()

        composeRule.onNodeWithTag("confirmReservationButton")
            .performClick()

        assertTrue(confirmed)
        assertEquals(1, callCount)
    }

    @Test
    fun disabledFormPreventsConfirmation() {
        composeRule.setContent {
            ReservationRulesConfirmation(
                campsite = campsite,
                onConfirm = {},
                enabled = false
            )
        }

        composeRule.onNodeWithTag("acceptRulesCheckbox")
            .assertIsNotEnabled()

        composeRule.onNodeWithTag("confirmReservationButton")
            .assertIsNotEnabled()
    }

    @Test
    fun changingCampsiteRulesResetsAgreement() {
        var currentCampsite by mutableStateOf(campsite)

        composeRule.setContent {
            ReservationRulesConfirmation(
                campsite = currentCampsite,
                onConfirm = {}
            )
        }

        composeRule.onNodeWithTag("acceptRulesCheckbox")
            .performClick()

        composeRule.onNodeWithTag("confirmReservationButton")
            .assertIsEnabled()

        composeRule.runOnIdle {
            currentCampsite = campsite.copy(
                rules = "No pets are allowed."
            )
        }

        composeRule.onNodeWithTag("campsiteRules")
            .assertTextEquals("No pets are allowed.")

        composeRule.onNodeWithTag("confirmReservationButton")
            .assertIsNotEnabled()
    }
}
