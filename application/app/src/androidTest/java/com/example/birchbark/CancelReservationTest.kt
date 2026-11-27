package com.example.birchbark

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.birchbark.ui.entrant.EntrantReservationsScreen
import com.example.birchbark.ui.entrant.ReservationListItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * Intent tests for cancelling a reservation.
 * Covers US 01.07.01 (cancel my campsite reservation).
 *
 * Draws [EntrantReservationsScreen] with fake reservations, so it doesn't need Firestore.
 */
@RunWith(AndroidJUnit4::class)
class CancelReservationTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val today = LocalDate.of(2026, 11, 6)

    private val upcoming = ReservationListItem(
        id = "upcoming",
        campsiteName = "Two Jack Lakeside",
        startDate = LocalDate.of(2026, 12, 1),
        endDate = LocalDate.of(2026, 12, 5)
    )

    private val past = ReservationListItem(
        id = "past",
        campsiteName = "Dinosaur Campground",
        startDate = LocalDate.of(2026, 10, 1),
        endDate = LocalDate.of(2026, 10, 4)
    )

    /**
     * Shows the reservations screen.
     *
     * @param onCancel gets the reservation once cancelling is confirmed.
     */
    private fun show(onCancel: (ReservationListItem) -> Unit = {}) {
        composeRule.setContent {
            EntrantReservationsScreen(reservations = listOf(upcoming, past), today = today, onCancel = onCancel)
        }
    }

    @Test
    fun activeReservation_showsCancelButton() {
        show()
        composeRule.onNodeWithTag("reservation-upcoming").performClick()
        composeRule.onNodeWithTag("cancelReservationButton").assertIsDisplayed()
    }

    @Test
    fun confirmingCancel_cancelsThatReservation() {
        var cancelled: ReservationListItem? = null
        show(onCancel = { cancelled = it })
        composeRule.onNodeWithTag("reservation-upcoming").performClick()
        composeRule.onNodeWithTag("cancelReservationButton").performClick()
        composeRule.onNodeWithTag("confirmCancelReservationButton").performClick()
        assertEquals("upcoming", cancelled?.id)
    }

    @Test
    fun keepIt_doesNotCancel() {
        var cancelled: ReservationListItem? = null
        show(onCancel = { cancelled = it })
        composeRule.onNodeWithTag("reservation-upcoming").performClick()
        composeRule.onNodeWithTag("cancelReservationButton").performClick()
        composeRule.onNodeWithTag("keepReservationButton").performClick()
        assertNull(cancelled)
        composeRule.onNodeWithTag("cancelReservationButton").assertIsDisplayed()
    }

    @Test
    fun pastReservation_hasNoCancelButton() {
        show()
        composeRule.onNodeWithTag("pastTab").performClick()
        composeRule.onNodeWithTag("reservation-past").performClick()
        composeRule.onNodeWithTag("cancelReservationButton").assertDoesNotExist()
    }
}