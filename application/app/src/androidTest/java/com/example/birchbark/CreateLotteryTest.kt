package com.example.birchbark

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.birchbark.model.Campsite
import com.example.birchbark.model.Lottery
import com.example.birchbark.ui.owner.CampsiteDetailsContent
import com.example.birchbark.ui.owner.CreateLotteryForm
import com.example.birchbark.util.Time
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * Intent tests for creating a lottery from the campsite details screen.
 * Covers US 02.04.01.
 *
 * Uses the form and details drawing directly so these don't need Firestore.
 */
@RunWith(AndroidJUnit4::class)
class CreateLotteryTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val whistlers = Campsite(
        id = "c1",
        ownerId = "owner123",
        name = "Whistlers Campground",
        location = "Jasper National Park",
        maxOccupancy = 6
    )

    private fun fillDates() {
        composeRule.onNodeWithTag("registrationStartField").performScrollTo().performTextInput("2026-11-10")
        composeRule.onNodeWithTag("registrationEndField").performScrollTo().performTextInput("2026-11-20")
        composeRule.onNodeWithTag("bookingStartField").performScrollTo().performTextInput("2026-11-25")
        composeRule.onNodeWithTag("bookingEndField").performScrollTo().performTextInput("2026-12-31")
    }

    // owner can create a lottery from a campsite's details screen

    @Test
    fun details_lotteryButtonOpensForm() {
        var clicked = false
        composeRule.setContent {
            CampsiteDetailsContent(
                campsite = whistlers,
                onEditClick = {},
                onBack = {},
                canCreateLottery = true,
                onLotteryClick = { clicked = true }
            )
        }
        composeRule.onNodeWithTag("lotteryButton").performScrollTo().performClick()
        assertTrue(clicked)
    }

    // the campsite details show that a lottery is open

    @Test
    fun details_showsLotteryOpen() {
        composeRule.setContent {
            CampsiteDetailsContent(
                campsite = whistlers,
                onEditClick = {},
                onBack = {},
                lotteryText = "Lottery open until Nov 20, 2026",
                canCreateLottery = false
            )
        }
        composeRule.onNodeWithText("Lottery open until Nov 20, 2026").assertExists()
        composeRule.onNodeWithTag("lotteryButton").assertDoesNotExist()
    }

    // the lottery is linked to the campsite

    @Test
    fun form_makesLotteryForThisCampsite() {
        var saved: Lottery? = null
        composeRule.setContent {
            CreateLotteryForm(campsite = whistlers, errors = listOf(), saving = false, onSave = { saved = it }, onCancel = {})
        }
        fillDates()
        composeRule.onNodeWithTag("numWinnersField").performScrollTo().performTextInput("5")
        composeRule.onNodeWithTag("setLotteryButton").performScrollTo().performClick()

        assertEquals(listOf("c1"), saved?.campsiteIds)
        assertEquals("owner123", saved?.ownerId)
        assertEquals(5, saved?.numWinners)
        assertEquals(Time.startOfDay(LocalDate.of(2026, 11, 10)), saved?.registrationStart)
        assertEquals(Time.endOfDay(LocalDate.of(2026, 11, 20)), saved?.registrationEnd)
        //left blank, so no limit
        assertNull(saved?.waitlistCapacity)
    }

    @Test
    fun form_savesWaitlistCapacity() {
        var saved: Lottery? = null
        composeRule.setContent {
            CreateLotteryForm(campsite = whistlers, errors = listOf(), saving = false, onSave = { saved = it }, onCancel = {})
        }
        fillDates()
        composeRule.onNodeWithTag("numWinnersField").performScrollTo().performTextInput("5")
        composeRule.onNodeWithTag("waitlistField").performScrollTo().performTextInput("150")
        composeRule.onNodeWithTag("setLotteryButton").performScrollTo().performClick()

        assertEquals(150, saved?.waitlistCapacity)
    }

    @Test
    fun form_doesNotSave_whenDateWrong() {
        var saved: Lottery? = null
        composeRule.setContent {
            CreateLotteryForm(campsite = whistlers, errors = listOf(), saving = false, onSave = { saved = it }, onCancel = {})
        }
        composeRule.onNodeWithTag("registrationStartField").performTextInput("Nov 10")
        composeRule.onNodeWithTag("setLotteryButton").performScrollTo().performClick()

        assertNull(saved)
        composeRule.onNodeWithText("Enter every date like 2026-11-20").assertExists()
    }

    @Test
    fun form_showsRuleErrors() {
        composeRule.setContent {
            CreateLotteryForm(
                campsite = whistlers,
                errors = listOf("Number of winners must be at least 1"),
                saving = false,
                onSave = {},
                onCancel = {}
            )
        }
        composeRule.onNodeWithText("Number of winners must be at least 1").assertExists()
    }
}