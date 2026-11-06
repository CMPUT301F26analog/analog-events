package com.example.birchbark


import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.birchbark.ui.entrant.DeleteProfileSection
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeleteProfileSectionTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun deleteButtonIsDisplayed() {
        rule.setContent {
            DeleteProfileSection(
                working = false,
                onConfirmDelete = {}
            )
        }

        rule.onNodeWithTag("deleteProfileButton")
            .assertIsDisplayed()
    }

    @Test
    fun confirmationIsNotShownInitially() {
        rule.setContent {
            DeleteProfileSection(
                working = false,
                onConfirmDelete = {}
            )
        }

        rule.onNodeWithTag("confirmDeleteProfileButton")
            .assertDoesNotExist()
    }

    @Test
    fun cancellingDoesNotTriggerDeletion() {
        var calls = 0

        rule.setContent {
            DeleteProfileSection(
                working = false,
                onConfirmDelete = { calls++ }
            )
        }

        rule.onNodeWithTag("deleteProfileButton")
            .performClick()

        rule.onNodeWithTag("cancelDeleteProfileButton")
            .performClick()

        assertEquals(0, calls)

        rule.onNodeWithTag("confirmDeleteProfileButton")
            .assertDoesNotExist()
    }

    @Test
    fun confirmingTriggersDeletionOnce() {
        var calls = 0

        rule.setContent {
            DeleteProfileSection(
                working = false,
                onConfirmDelete = { calls++ }
            )
        }

        rule.onNodeWithTag("deleteProfileButton")
            .performClick()

        rule.onNodeWithTag("confirmDeleteProfileButton")
            .performClick()

        assertEquals(1, calls)
    }

    @Test
    fun buttonDisabledDuringDeletion() {
        rule.setContent {
            DeleteProfileSection(
                working = true,
                onConfirmDelete = {}
            )
        }

        rule.onNodeWithTag("deleteProfileButton")
            .assertIsNotEnabled()
    }
}
