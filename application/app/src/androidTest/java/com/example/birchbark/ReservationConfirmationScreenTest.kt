package com.example.birchbark

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.birchbark.ui.entrant.ReservationConfirmationScreen
import com.example.birchbark.ui.theme.BirchBarkTheme
import com.example.birchbark.util.Time
import com.example.birchbark.util.datesLabel
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

// See [TBD 3] in LLM.md

/**
 * Intent tests for the reservation confirmation screen.
 * Covers US 01.05.01: after confirming, the entrant sees a confirmation with
 * the campsite name and dates.
 *
 * Uses fake text so these don't need Firestore. That the reservation is confirmed
 * in Firebase is checked by looking at the "reservations" collection in the
 * Firebase console.
 */
@RunWith(AndroidJUnit4::class)
class ReservationConfirmationScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val dates = datesLabel(
        Time.startOfDay(LocalDate.of(2026, 11, 25)),
        Time.endOfDay(LocalDate.of(2026, 11, 27))
    )

    private fun show(
        campsiteName: String = "Whistlers Campground",
        dates: String = this.dates,
        onDone: () -> Unit = {}
    ) {
        composeRule.setContent {
            BirchBarkTheme {
                ReservationConfirmationScreen(campsiteName = campsiteName, dates = dates, onDone = onDone)
            }
        }
    }

    @Test
    fun confirmation_showsConfirmedMessage() {
        show()
        composeRule.onNodeWithText("Reservation confirmed").assertIsDisplayed()
    }

    @Test
    fun confirmation_showsCampsiteName() {
        show()
        composeRule.onNodeWithText("Whistlers Campground").assertIsDisplayed()
    }

    @Test
    fun confirmation_showsDates() {
        show()
        composeRule.onNodeWithText("Nov 25, 2026 to Nov 27, 2026").assertIsDisplayed()
    }

    @Test
    fun confirmation_showsOneDate_whenSingleDayStay() {
        val day = LocalDate.of(2026, 12, 3)
        show(dates = datesLabel(Time.startOfDay(day), Time.endOfDay(day)))
        composeRule.onNodeWithText("Dec 3, 2026").assertIsDisplayed()
    }

    @Test
    fun confirmation_doneCallsOnDone() {
        var done = false
        show(onDone = { done = true })
        composeRule.onNodeWithText("Done").performClick()
        assertTrue(done)
    }
}
