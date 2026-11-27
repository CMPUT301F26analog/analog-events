package com.example.birchbark

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

// See #056 in LLM.md

/**
 * Intent tests for the pick dates screen a lottery winner sees.
 * Covers US 01.04.01 (see free dates, pick days), US 01.04.03 (decline,
 * with an "are you sure" step) and US 02.11.01 (rules shown before confirming).
 *
 * Draws [PickDatesContent] with a fake state, so it doesn't need Firestore.
 */
@RunWith(AndroidJUnit4::class)
class PickDatesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val nov20 = LocalDate.of(2026, 11, 20)

    private val loaded = PickDatesUiState(
        lottery = Lottery(
            id = "L1",
            bookingStart = Time.startOfDay(nov20),
            bookingEnd = Time.endOfDay(nov20.plusDays(9))
        ),
        campsite = Campsite(id = "C1", name = "Whistlers Campground", rules = "Quiet hours 10pm to 7am"),
        bookings = emptyList()
    )

    private fun show(
        state: PickDatesUiState,
        onNext: () -> Unit = {},
        onDeclineConfirm: () -> Unit = {}
    ) {
        composeRule.setContent {
            var current by remember { mutableStateOf(state) }
            PickDatesContent(
                state = current,
                onBack = {},
                onFirstDayChange = {},
                onLastDayChange = {},
                onNext = onNext,
                onChangeDates = {},
                onConfirm = {},
                onDeclineClick = { current = current.copy(confirmingDecline = true) },
                onDeclineDismiss = { current = current.copy(confirmingDecline = false) },
                onDeclineConfirm = onDeclineConfirm
            )
        }
    }

    // US 01.04.01: seeing and picking dates

    @Test
    fun showsFreeDates_whenLoaded() {
        show(loaded)
        composeRule.onNodeWithTag("freeDates").assertIsDisplayed()
        composeRule.onNodeWithTag("firstDayField").assertIsDisplayed()
    }

    @Test
    fun nextButton_callsOnNext() {
        var tapped = false
        show(loaded, onNext = { tapped = true })
        composeRule.onNodeWithTag("nextButton").performClick()
        assertTrue(tapped)
    }

    @Test
    fun showsFormError() {
        show(loaded.copy(formError = "Some of those days are already booked"))
        composeRule.onNodeWithText("Some of those days are already booked").assertIsDisplayed()
    }

    // US 02.11.01: rules are shown once days are picked

    @Test
    fun showsRules_afterDaysPicked() {
        show(loaded.copy(pickedDays = nov20.plusDays(1) to nov20.plusDays(2)))
        composeRule.onNodeWithTag("campsiteRules").assertIsDisplayed()
        composeRule.onNodeWithTag("confirmReservationButton").assertIsDisplayed()
    }

    // US 01.04.03: declining asks first

    @Test
    fun decline_asksBeforeDeclining() {
        var declined = false
        show(loaded, onDeclineConfirm = { declined = true })
        composeRule.onNodeWithTag("declineButton").performClick()
        //the dialog is open, nothing declined yet
        composeRule.onNodeWithText("Decline this reservation?").assertIsDisplayed()
        assertFalse(declined)

        composeRule.onNodeWithTag("confirmDeclineButton").performClick()
        assertTrue(declined)
    }

    @Test
    fun keepIt_closesDialogWithoutDeclining() {
        var declined = false
        show(loaded, onDeclineConfirm = { declined = true })
        composeRule.onNodeWithTag("declineButton").performClick()
        composeRule.onNodeWithText("Keep it").performClick()
        composeRule.onNodeWithText("Decline this reservation?").assertDoesNotExist()
        assertFalse(declined)
    }
}
