package com.example.birchbark

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.birchbark.ui.entrant.EntrantReservationsScreen
import com.example.birchbark.ui.entrant.ReservationListItem
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class EntrantReservationsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val today = LocalDate.of(2026, 11, 6)

    private val current = ReservationListItem(
        id = "current",
        campsiteName = "Whistlers Campground",
        startDate = LocalDate.of(2026, 11, 4),
        endDate = LocalDate.of(2026, 11, 8)
    )

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

    private val cancelled = ReservationListItem(
        id = "cancelled",
        campsiteName = "Cancelled Campground",
        startDate = LocalDate.of(2026, 12, 1),
        endDate = LocalDate.of(2026, 12, 5),
        cancelled = true
    )

    private fun showScreen(
        reservations: List<ReservationListItem> =
            listOf(current, upcoming, past, cancelled)
    ) {
        composeRule.setContent {
            EntrantReservationsScreen(
                reservations = reservations,
                today = today
            )
        }
    }

    @Test
    fun activeReservationsShownByDefault() {
        showScreen()

        composeRule.onNodeWithTag("reservation-current")
            .assertIsDisplayed()

        composeRule.onNodeWithTag("reservation-upcoming")
            .assertIsDisplayed()

        composeRule.onNodeWithTag("reservation-past")
            .assertDoesNotExist()
    }

    @Test
    fun pastTabShowsOnlyPastReservations() {
        showScreen()

        composeRule.onNodeWithTag("pastTab").performClick()

        composeRule.onNodeWithTag("reservation-past")
            .assertIsDisplayed()

        composeRule.onNodeWithTag("reservation-current")
            .assertDoesNotExist()

        composeRule.onNodeWithTag("reservation-upcoming")
            .assertDoesNotExist()
    }

    @Test
    fun emptyActiveReservationsShowsMessage() {
        showScreen(listOf(past))

        composeRule.onNodeWithText("No active reservations")
            .assertIsDisplayed()
    }

    @Test
    fun emptyPastReservationsShowsMessage() {
        showScreen(listOf(current, upcoming))

        composeRule.onNodeWithTag("pastTab").performClick()

        composeRule.onNodeWithText("No past reservations")
            .assertIsDisplayed()
    }

    @Test
    fun tappingActiveReservationOpensDetails() {
        showScreen()

        composeRule.onNodeWithTag("reservation-current")
            .performClick()

        composeRule.onNodeWithText("Reservation Details")
            .assertIsDisplayed()

        composeRule.onNodeWithText("Whistlers Campground")
            .assertIsDisplayed()

        composeRule.onNodeWithText("Check-in: Nov 4, 2026")
            .assertIsDisplayed()

        composeRule.onNodeWithText("Check-out: Nov 8, 2026")
            .assertIsDisplayed()
    }

    @Test
    fun backButtonReturnsToReservations() {
        showScreen()

        composeRule.onNodeWithTag("reservation-current")
            .performClick()

        composeRule.onNodeWithText("Back to reservations")
            .performClick()

        composeRule.onNodeWithText("My Reservations")
            .assertIsDisplayed()

        composeRule.onNodeWithTag("reservation-current")
            .assertIsDisplayed()
    }

    @Test
    fun cancelledReservationNeverAppears() {
        showScreen()

        composeRule.onNodeWithTag("reservation-cancelled")
            .assertDoesNotExist()

        composeRule.onNodeWithTag("pastTab").performClick()

        composeRule.onNodeWithTag("reservation-cancelled")
            .assertDoesNotExist()
    }

    @Test
    fun switchingTabsRestoresActiveList() {
        showScreen()

        composeRule.onNodeWithTag("pastTab").performClick()

        composeRule.onNodeWithTag("activeTab").performClick()

        composeRule.onNodeWithTag("reservation-current")
            .assertIsDisplayed()

        composeRule.onNodeWithTag("reservation-past")
            .assertDoesNotExist()
    }
}
