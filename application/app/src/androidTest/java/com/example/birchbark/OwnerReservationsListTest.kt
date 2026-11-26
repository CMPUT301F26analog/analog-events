package com.example.birchbark

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.birchbark.model.Reservation
import com.example.birchbark.model.ReservationStatus
import com.example.birchbark.ui.owner.OwnerReservationsList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// See #058 in LLM.md

/**
 * Intent tests for rejecting a reservation from the owner's list (US 02.08.01).
 * Draws [OwnerReservationsList] with fake data, so it doesn't need Firestore.
 */
@RunWith(AndroidJUnit4::class)
class OwnerReservationsListTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val active = Reservation(id = "R1", status = ReservationStatus.PENDING)

    private var rejected: Reservation? = null

    private fun show() {
        composeRule.setContent {
            OwnerReservationsList(
                reservations = listOf(active),
                campsiteNames = emptyMap(),
                now = 1_000L,
                onBack = {},
                onReject = { rejected = it }
            )
        }
    }

    @Test
    fun rejectAsksFirst() {
        show()
        composeRule.onNodeWithTag("rejectButton-R1").performClick()
        composeRule.onNodeWithText("Reject this reservation?").assertIsDisplayed()
        assertNull(rejected)
    }

    @Test
    fun confirmRejects() {
        show()
        composeRule.onNodeWithTag("rejectButton-R1").performClick()
        composeRule.onNodeWithTag("confirmRejectButton").performClick()
        assertEquals("R1", rejected?.id)
    }

    @Test
    fun keepItDoesNothing() {
        show()
        composeRule.onNodeWithTag("rejectButton-R1").performClick()
        composeRule.onNodeWithText("Keep it").performClick()
        assertNull(rejected)
    }
}
