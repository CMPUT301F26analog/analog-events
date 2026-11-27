package com.example.birchbark

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.birchbark.waitlist.WaitlistMessageSection
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Intent tests for messaging a lottery waitlist.
 * Covers US 02.10.01 (notify waitlist entrants).
 *
 * Draws [WaitlistMessageSection] on its own, so it doesn't need Firestore.
 */
@RunWith(AndroidJUnit4::class)
class WaitlistMessageSectionTest {

    @get:Rule
    val composeRule = createComposeRule()

    /**
     * Shows the message box, keeping what's typed like the real screen does.
     *
     * @param canSend false to act like it's sending or still loading.
     * @param status the message shown under the button.
     * @param onSend called when Send to Waitlist is tapped.
     */
    private fun show(canSend: Boolean = true, status: String? = null, onSend: () -> Unit = {}) {
        composeRule.setContent {
            var message by remember { mutableStateOf("") }
            WaitlistMessageSection(
                message = message,
                onMessageChange = { message = it },
                canSend = canSend,
                status = status,
                onSend = onSend
            )
        }
    }

    @Test
    fun emptyMessage_sendIsDisabled() {
        show()
        composeRule.onNodeWithTag("sendWaitlistMessageButton").assertIsNotEnabled()
    }

    @Test
    fun typedMessage_sendCallsOnSend() {
        var sent = false
        show(onSend = { sent = true })
        composeRule.onNodeWithTag("waitlistMessageField").performTextInput("Gates open at 9am")
        composeRule.onNodeWithTag("sendWaitlistMessageButton").assertIsEnabled().performClick()
        assertTrue(sent)
    }

    @Test
    fun whileSending_sendIsDisabled() {
        show(canSend = false)
        composeRule.onNodeWithTag("waitlistMessageField").performTextInput("Hi")
        composeRule.onNodeWithTag("sendWaitlistMessageButton").assertIsNotEnabled()
    }

    @Test
    fun status_isShown() {
        show(status = "Sent to all 3 entrants")
        composeRule.onNodeWithText("Sent to all 3 entrants").assertExists()
    }
}