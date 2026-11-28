package com.example.birchbark

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.birchbark.model.NotificationType
import com.example.birchbark.model.SentNotifications
import com.example.birchbark.ui.owner.MyCampsitesList
import com.example.birchbark.ui.owner.SentNotificationsContent
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Intent tests for the owner's sent notifications.
 * Covers US 02.10.02 (view sent notifications).
 *
 * Uses the drawing part of the screen with fake messages, so these don't need Firestore.
 */
@RunWith(AndroidJUnit4::class)
class SentNotificationsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val waitlistMessage = SentNotifications.SentMessage(
        type = NotificationType.WAITLIST_MESSAGE, title = "Trail update",
        body = "The north trail is closed this weekend.", lotteryId = "L1",
        createdAt = 2000L, recipientCount = 4
    )
    private val drawResult = SentNotifications.SentMessage(
        type = NotificationType.LOTTERY_WON, title = "You won the lottery!",
        body = "Open My Reservations to choose your dates.", lotteryId = "L1",
        createdAt = 1000L, recipientCount = 1
    )
    private val labels = mapOf("L1" to "Lottery for Whistlers Campground")

    @Test
    fun sent_showsEveryMessage() {
        composeRule.setContent { SentNotificationsContent(messages = listOf(waitlistMessage, drawResult), lotteryLabels = labels, onBack = {}) }
        composeRule.onAllNodesWithTag("sentNotification").assertCountEquals(2)
    }

    @Test
    fun sent_showsMessageLotteryAndCount() {
        composeRule.setContent { SentNotificationsContent(messages = listOf(waitlistMessage), lotteryLabels = labels, onBack = {}) }
        composeRule.onNodeWithText("The north trail is closed this weekend.").assertExists()
        composeRule.onNodeWithText("Lottery for Whistlers Campground").assertExists()
        composeRule.onNodeWithText("Sent to 4 people").assertExists()
    }

    @Test
    fun sent_empty_showsMessage() {
        composeRule.setContent { SentNotificationsContent(messages = listOf(), lotteryLabels = labels, onBack = {}) }
        composeRule.onNodeWithTag("noSentNotifications").assertExists()
    }

    @Test
    fun myCampsites_sentButtonOpensScreen() {
        var opened = false
        composeRule.setContent {
            MyCampsitesList(campsites = listOf(), onCampsiteClick = {}, onSentClick = { opened = true })
        }
        composeRule.onNodeWithTag("sentNotificationsButton").performClick()
        assertTrue(opened)
    }
}