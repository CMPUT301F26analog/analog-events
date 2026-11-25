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
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.birchbark.model.AppPolicy
import com.example.birchbark.ui.screens.PolicyMenuItem
import com.example.birchbark.ui.screens.PolicyScreen
import com.example.birchbark.ui.theme.BirchBarkTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// See [TBD 1] in LLM.md

/**
 * Intent tests for the app policy screen.
 * Covers US 01.11.01: the entrant can open the policy from the menu, and the
 * page shows the full policy and guidelines.
 *
 * Doesn't need Firestore, the policy is plain text in [AppPolicy].
 */
@RunWith(AndroidJUnit4::class)
class PolicyScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    // the page shows the full policy

    @Test
    fun policy_showsHeading() {
        composeRule.setContent { BirchBarkTheme { PolicyScreen(onBack = {}) } }
        composeRule.onNodeWithText(AppPolicy.TITLE).assertIsDisplayed()
    }

    @Test
    fun policy_showsEverySection() {
        composeRule.setContent { BirchBarkTheme { PolicyScreen(onBack = {}) } }
        for (section in AppPolicy.sections) {
            //scroll to it first, long policies don't all fit on screen
            composeRule.onNodeWithText(section.title).performScrollTo().assertIsDisplayed()
            composeRule.onNodeWithText(section.body).assertExists()
        }
    }

    @Test
    fun policy_backCallsOnBack() {
        var wentBack = false
        composeRule.setContent { BirchBarkTheme { PolicyScreen(onBack = { wentBack = true }) } }
        composeRule.onNodeWithText("Back").performClick()
        assertTrue(wentBack)
    }

    // the entrant can open it from the menu

    @Test
    fun menuItem_showsLabel() {
        composeRule.setContent { BirchBarkTheme { PolicyMenuItem(onClick = {}) } }
        composeRule.onNodeWithTag("policyMenuItem").assertIsDisplayed()
    }

    @Test
    fun menuItem_callsOnClick() {
        var tapped = false
        composeRule.setContent { BirchBarkTheme { PolicyMenuItem(onClick = { tapped = true }) } }
        composeRule.onNodeWithTag("policyMenuItem").performClick()
        assertTrue(tapped)
    }

    // the whole flow: menu, open the policy, go back to the menu
    @Test
    fun tappingMenuItem_opensPolicy_andBackReturnsToMenu() {
        composeRule.setContent {
            BirchBarkTheme {
                var showPolicy by remember { mutableStateOf(false) }
                if (showPolicy) {
                    PolicyScreen(onBack = { showPolicy = false })
                } else {
                    PolicyMenuItem(onClick = { showPolicy = true })
                }
            }
        }

        composeRule.onNodeWithTag("policyMenuItem").assertIsDisplayed()

        composeRule.onNodeWithTag("policyMenuItem").performClick()
        composeRule.onNodeWithText(AppPolicy.TITLE).assertIsDisplayed()

        composeRule.onNodeWithText("Back").performClick()
        composeRule.onNodeWithTag("policyMenuItem").assertIsDisplayed()
    }
}
