package com.example.birchbark

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.birchbark.model.Campsite
import com.example.birchbark.model.Lottery
import com.example.birchbark.ui.entrant.PickDatesContent
import com.example.birchbark.ui.entrant.PickDatesUiState
import com.example.birchbark.util.Time
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * Intent tests for winning a multi-campsite lottery.
 * Covers US 01.04.02 (pick which campsite, then pick dates for it).
 *
 * Draws [PickDatesContent] with a fake state, so it doesn't need Firestore.
 */
@RunWith(AndroidJUnit4::class)
class MultiCampsiteWinTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val nov20 = LocalDate.of(2026, 11, 20)
    private val lottery = Lottery(
        id = "L1",
        campsiteIds = listOf("C1", "C2"),
        bookingStart = Time.startOfDay(nov20),
        bookingEnd = Time.endOfDay(nov20.plusDays(9))
    )
    private val whistlers = Campsite(id = "C1", name = "Whistlers Campground", location = "Jasper")
    private val wapiti = Campsite(id = "C2", name = "Wapiti Campground", location = "Jasper")

    //a multi-campsite win before the winner picks
    private val picking = PickDatesUiState(lottery = lottery, choices = listOf(whistlers, wapiti))

    /**
     * Shows the screen with the given state.
     *
     * @param state what the screen shows.
     * @param onChoose gets the campsite when one is chosen.
     * @param onChange called when the winner wants a different campsite.
     */
    private fun show(state: PickDatesUiState, onChoose: (Campsite) -> Unit = {}, onChange: () -> Unit = {}) {
        composeRule.setContent {
            PickDatesContent(
                state = state,
                onBack = {},
                onFirstDayChange = {},
                onLastDayChange = {},
                onNext = {},
                onChangeDates = {},
                onConfirm = {},
                onDeclineClick = {},
                onDeclineDismiss = {},
                onDeclineConfirm = {},
                onChooseCampsite = onChoose,
                onChangeCampsite = onChange
            )
        }
    }

    @Test
    fun multiCampsiteWin_listsEveryCampsite() {
        show(picking)
        composeRule.onNodeWithText("Whistlers Campground").assertIsDisplayed()
        composeRule.onNodeWithText("Wapiti Campground").assertIsDisplayed()
    }

    @Test
    fun choosingACampsite_passesThatCampsite() {
        var chosen: Campsite? = null
        show(picking, onChoose = { chosen = it })
        composeRule.onNodeWithTag("chooseCampsite_C2").performClick()
        assertEquals("C2", chosen?.id)
    }

    @Test
    fun afterChoosing_showsDatesAndChangeButton() {
        show(picking.copy(campsite = wapiti, bookings = emptyList()))
        composeRule.onNodeWithTag("freeDates").assertIsDisplayed()
        composeRule.onNodeWithTag("changeCampsiteButton").assertIsDisplayed()
    }

    @Test
    fun changeButton_goesBackToTheList() {
        var changed = false
        show(picking.copy(campsite = wapiti, bookings = emptyList()), onChange = { changed = true })
        composeRule.onNodeWithTag("changeCampsiteButton").performClick()
        assertTrue(changed)
    }

    @Test
    fun singleCampsiteWin_hasNoChangeButton() {
        show(PickDatesUiState(lottery = lottery.copy(campsiteIds = listOf("C1")), campsite = whistlers, bookings = emptyList()))
        composeRule.onNodeWithTag("freeDates").assertIsDisplayed()
        composeRule.onNodeWithTag("changeCampsiteButton").assertDoesNotExist()
    }
}