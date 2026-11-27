package com.example.birchbark

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertCountEquals
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.birchbark.model.AppNotification
import com.example.birchbark.model.NotificationType
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Intent tests for the admin's notification logs.
 * Covers US 03.08.01 (review notification logs).
 *
 * Uses the drawing part of the screen with fake notifications, so these don't need Firestore.
 */
@RunWith(AndroidJUnit4::class)
class AdminNotificationLogsTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val names = mapOf("owner1" to "Riley Whitford", "entrant1" to "Eliza Lazar")

    private val won = AppNotification(
        id = "n1", senderId = "owner1", recipientId = "entrant1",
        type = NotificationType.LOTTERY_WON, title = "You won the lottery!",
        body = "Open My Reservations to choose your dates.", createdAt = 2000L
    )
    private val message = AppNotification(
        id = "n2", senderId = "owner1", recipientId = "someoneGone",
        type = NotificationType.WAITLIST_MESSAGE, title = "Trail update",
        body = "The north trail is closed this weekend.", createdAt = 1000L
    )

    @Test
    fun logs_showEveryNotification() {
        composeRule.setContent { AdminNotificationLogsContent(notifications = listOf(won, message), names = names) }
        composeRule.onAllNodesWithTag("notificationLog").assertCountEquals(2)
    }

    @Test
    fun logs_showSenderAndRecipientNames() {
        composeRule.setContent { AdminNotificationLogsContent(notifications = listOf(won), names = names) }
        composeRule.onNodeWithText("From Riley Whitford to Eliza Lazar").assertExists()
    }

    @Test
    fun logs_showMessage() {
        composeRule.setContent { AdminNotificationLogsContent(notifications = listOf(message), names = names) }
        composeRule.onNodeWithText("Trail update").assertExists()
        composeRule.onNodeWithText("The north trail is closed this weekend.").assertExists()
    }

    @Test
    fun logs_unknownRecipient_saysUnknownUser() {
        composeRule.setContent { AdminNotificationLogsContent(notifications = listOf(message), names = names) }
        composeRule.onNodeWithText("From Riley Whitford to Unknown user").assertExists()
    }

    @Test
    fun logs_empty_showsMessage() {
        composeRule.setContent { AdminNotificationLogsContent(notifications = listOf(), names = names) }
        composeRule.onNodeWithTag("noNotificationLogs").assertExists()
    }
}