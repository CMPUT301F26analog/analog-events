package com.example.birchbark

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.birchbark.model.Campsite
import com.example.birchbark.model.Lottery
import com.example.birchbark.ui.owner.CreateLotteryForm
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Intent tests for putting more than one campsite in a lottery.
 * Covers US 02.04.02 (multi-campsite lottery).
 *
 * Uses the drawing part of the lottery form with three fake campsites from
 * the same owner, so these don't need Firestore.
 */
@RunWith(AndroidJUnit4::class)
class MultiCampsiteLotteryTest {

    @get:Rule
    val composeRule = createComposeRule()

    //three Jasper campgrounds owned by the same owner
    private val whistlers = Campsite(id = "c1", ownerId = "owner123", name = "Whistlers Campground", location = "Jasper National Park", maxOccupancy = 6)
    private val wapiti = Campsite(id = "c2", ownerId = "owner123", name = "Wapiti Campground", location = "Jasper National Park", maxOccupancy = 4)
    private val pocahontas = Campsite(id = "c3", ownerId = "owner123", name = "Pocahontas Campground", location = "Jasper National Park", maxOccupancy = 8)
    private val allThree = listOf(whistlers, wapiti, pocahontas)

    /**
     * Shows the form opened from Whistlers, with all three campsites to pick from.
     *
     * @param busyIds campsites that are already in a lottery.
     * @param onSave gets the lottery when Set Lottery is tapped.
     */
    private fun showForm(busyIds: Set<String> = setOf(), onSave: (Lottery) -> Unit = {}) {
        composeRule.setContent {
            CreateLotteryForm(
                campsite = whistlers,
                ownerCampsites = allThree,
                busyIds = busyIds,
                errors = listOf(),
                saving = false,
                onSave = onSave,
                onCancel = {}
            )
        }
    }

    /** Fills in valid dates and winners so the form can be saved. */
    private fun fillTheRest() {
        composeRule.onNodeWithTag("registrationStartField").performScrollTo().performTextInput("2026-11-10")
        composeRule.onNodeWithTag("registrationEndField").performScrollTo().performTextInput("2026-11-20")
        composeRule.onNodeWithTag("bookingStartField").performScrollTo().performTextInput("2026-11-25")
        composeRule.onNodeWithTag("bookingEndField").performScrollTo().performTextInput("2026-12-31")
        composeRule.onNodeWithTag("numWinnersField").performScrollTo().performTextInput("5")
    }

    @Test
    fun form_listsEveryOwnerCampsite() {
        showForm()
        composeRule.onNodeWithText("Whistlers Campground, Jasper National Park").assertExists()
        composeRule.onNodeWithText("Wapiti Campground, Jasper National Park").assertExists()
        composeRule.onNodeWithText("Pocahontas Campground, Jasper National Park").assertExists()
    }

    @Test
    fun form_startsWithOpenedCampsiteTicked() {
        showForm()
        composeRule.onNodeWithTag("campsiteCheckbox_c1").assertIsOn()
        composeRule.onNodeWithTag("campsiteCheckbox_c2").assertIsOff()
        composeRule.onNodeWithTag("campsiteCheckbox_c3").assertIsOff()
    }

    @Test
    fun form_savesOnlyOneCampsite_whenNothingElseTicked() {
        var saved: Lottery? = null
        showForm(onSave = { saved = it })
        fillTheRest()
        composeRule.onNodeWithTag("setLotteryButton").performScrollTo().performClick()
        assertEquals(listOf("c1"), saved?.campsiteIds)
    }

    @Test
    fun form_savesEveryTickedCampsite() {
        var saved: Lottery? = null
        showForm(onSave = { saved = it })
        composeRule.onNodeWithTag("campsiteCheckbox_c2").performClick()
        composeRule.onNodeWithTag("campsiteCheckbox_c3").performClick()
        fillTheRest()
        composeRule.onNodeWithTag("setLotteryButton").performScrollTo().performClick()
        assertEquals(listOf("c1", "c2", "c3"), saved?.campsiteIds)
    }

    @Test
    fun form_untickingRemovesCampsite() {
        var saved: Lottery? = null
        showForm(onSave = { saved = it })
        composeRule.onNodeWithTag("campsiteCheckbox_c3").performClick()
        //owner changes their mind about Whistlers
        composeRule.onNodeWithTag("campsiteCheckbox_c1").performClick()
        fillTheRest()
        composeRule.onNodeWithTag("setLotteryButton").performScrollTo().performClick()
        assertEquals(listOf("c3"), saved?.campsiteIds)
    }

    @Test
    fun form_keepsOwnerId() {
        var saved: Lottery? = null
        showForm(onSave = { saved = it })
        composeRule.onNodeWithTag("campsiteCheckbox_c2").performClick()
        fillTheRest()
        composeRule.onNodeWithTag("setLotteryButton").performScrollTo().performClick()
        assertEquals("owner123", saved?.ownerId)
    }

    @Test
    fun form_flagsCampsiteAlreadyInLottery() {
        //Wapiti already has a lottery that isn't drawn yet
        showForm(busyIds = setOf("c2"))
        composeRule.onNodeWithTag("busyFlag_c2").assertExists()
        composeRule.onNodeWithTag("campsiteCheckbox_c2").assertIsNotEnabled()
        composeRule.onNodeWithTag("busyFlag_c3").assertDoesNotExist()
    }

    @Test
    fun form_neverSavesFlaggedCampsite() {
        var saved: Lottery? = null
        showForm(busyIds = setOf("c2"), onSave = { saved = it })
        composeRule.onNodeWithTag("campsiteCheckbox_c3").performClick()
        fillTheRest()
        composeRule.onNodeWithTag("setLotteryButton").performScrollTo().performClick()
        assertEquals(listOf("c1", "c3"), saved?.campsiteIds)
    }
}