
package com.example.birchbark

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.birchbark.ui.notifications.NotificationSettingsSection
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationSettingsSectionTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun notificationLabelIsDisplayed() {
        composeRule.setContent {
            NotificationSettingsSection(
                enabled = true,
                onEnabledChange = {}
            )
        }

        composeRule.onNodeWithText("Enable Notifications")
            .assertIsDisplayed()
    }

    @Test
    fun switchStartsOnWhenEnabled() {
        composeRule.setContent {
            NotificationSettingsSection(
                enabled = true,
                onEnabledChange = {}
            )
        }

        composeRule.onNode(isToggleable()).assertIsOn()
    }

    @Test
    fun switchStartsOffWhenDisabled() {
        composeRule.setContent {
            NotificationSettingsSection(
                enabled = false,
                onEnabledChange = {}
            )
        }

        composeRule.onNode(isToggleable()).assertIsOff()
    }

    @Test
    fun turningNotificationsOffUpdatesSwitch() {
        var savedValue: Boolean? = null

        composeRule.setContent {
            var enabled by remember { mutableStateOf(true) }

            NotificationSettingsSection(
                enabled = enabled,
                onEnabledChange = {
                    savedValue = it
                    enabled = it
                }
            )
        }

        composeRule.onNode(isToggleable())
            .assertIsOn()
            .performClick()
            .assertIsOff()

        composeRule.runOnIdle {
            assertEquals(false, savedValue)
        }
    }

    @Test
    fun turningNotificationsOnUpdatesSwitch() {
        var savedValue: Boolean? = null

        composeRule.setContent {
            var enabled by remember { mutableStateOf(false) }

            NotificationSettingsSection(
                enabled = enabled,
                onEnabledChange = {
                    savedValue = it
                    enabled = it
                }
            )
        }

        composeRule.onNode(isToggleable())
            .assertIsOff()
            .performClick()
            .assertIsOn()

        composeRule.runOnIdle {
            assertEquals(true, savedValue)
        }
    }

    @Test
    fun switchCannotBeUsedWhileSaving() {
        composeRule.setContent {
            NotificationSettingsSection(
                enabled = true,
                interactive = false,
                onEnabledChange = {}
            )
        }

        composeRule.onNode(isToggleable())
            .assertIsNotEnabled()
            .assertIsOn()
    }

    @Test
    fun switchIsInteractiveWhenAllowed() {
        composeRule.setContent {
            NotificationSettingsSection(
                enabled = true,
                interactive = true,
                onEnabledChange = {}
            )
        }

        composeRule.onNode(isToggleable())
            .assertIsEnabled()
    }
}
