package com.example.birchbark

import android.graphics.Bitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.birchbark.ui.entrant.ProfilePhotoSection
import com.example.birchbark.ui.theme.BirchBarkTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream

// See [TBD 10] in LLM.md

/**
 * Intent tests for the profile photo section.
 * Covers US 01.08.02: the entrant can add, change or remove their profile image.
 *
 * Uses a fake picture so these don't need Firestore or the photo picker. That
 * the photo is saved in Firebase is checked by looking at the "users" collection
 * in the Firebase console.
 */
@RunWith(AndroidJUnit4::class)
class ProfilePhotoSectionTest {

    @get:Rule
    val composeRule = createComposeRule()

    /** A small real picture, so the screen has something to decode. */
    private val photo: ByteArray = ByteArrayOutputStream().also { out ->
        Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_8888)
            .compress(Bitmap.CompressFormat.JPEG, 80, out)
    }.toByteArray()

    private fun show(
        imageBytes: ByteArray? = null,
        working: Boolean = false,
        message: String? = null,
        isError: Boolean = false,
        onChoose: () -> Unit = {},
        onRemove: () -> Unit = {}
    ) {
        composeRule.setContent {
            BirchBarkTheme {
                ProfilePhotoSection(
                    name = "Evelyn Alazar",
                    imageBytes = imageBytes,
                    working = working,
                    message = message,
                    isError = isError,
                    onChoose = onChoose,
                    onRemove = onRemove
                )
            }
        }
    }

    // add

    @Test
    fun withoutAPhoto_showsInitialsAndAddButton() {
        show(imageBytes = null)

        composeRule.onNodeWithTag("profileInitials").assertExists()
        composeRule.onNodeWithTag("profileImage").assertDoesNotExist()
        composeRule.onNodeWithText("Add photo").assertIsDisplayed()
    }

    @Test
    fun withoutAPhoto_thereIsNothingToRemove() {
        show(imageBytes = null)
        composeRule.onNodeWithTag("removePhotoButton").assertDoesNotExist()
    }

    @Test
    fun addPhoto_callsOnChoose() {
        var chose = false
        show(onChoose = { chose = true })

        composeRule.onNodeWithTag("choosePhotoButton").performClick()
        assertTrue(chose)
    }

    // update

    @Test
    fun withAPhoto_showsItAndChangeButton() {
        show(imageBytes = photo)

        composeRule.onNodeWithTag("profileImage").assertExists()
        composeRule.onNodeWithTag("profileInitials").assertDoesNotExist()
        composeRule.onNodeWithText("Change photo").assertIsDisplayed()
    }

    @Test
    fun changePhoto_callsOnChoose() {
        var chose = false
        show(imageBytes = photo, onChoose = { chose = true })

        composeRule.onNodeWithTag("choosePhotoButton").performClick()
        assertTrue(chose)
    }

    // remove

    @Test
    fun withAPhoto_showsRemoveButton() {
        show(imageBytes = photo)
        composeRule.onNodeWithText("Remove photo").assertIsDisplayed()
    }

    @Test
    fun removePhoto_callsOnRemove() {
        var removed = false
        show(imageBytes = photo, onRemove = { removed = true })

        composeRule.onNodeWithTag("removePhotoButton").performClick()
        assertTrue(removed)
    }

    // while saving, and messages

    @Test
    fun whileWorking_buttonsAreLockedAndSayWhy() {
        show(imageBytes = photo, working = true)

        composeRule.onNodeWithText("Saving...").assertIsDisplayed()
        composeRule.onNodeWithTag("choosePhotoButton").assertIsNotEnabled()
        composeRule.onNodeWithTag("removePhotoButton").assertIsNotEnabled()
    }

    @Test
    fun message_isShown() {
        show(message = "Photo saved")
        composeRule.onNodeWithText("Photo saved").assertIsDisplayed()
    }

    @Test
    fun errorMessage_isShown() {
        show(message = "Couldn't read that image, try another one", isError = true)
        composeRule.onNodeWithText("Couldn't read that image, try another one").assertIsDisplayed()
    }

    @Test
    fun noMessage_showsNothing() {
        show(message = null)
        composeRule.onNodeWithTag("photoMessage").assertDoesNotExist()
    }
}
