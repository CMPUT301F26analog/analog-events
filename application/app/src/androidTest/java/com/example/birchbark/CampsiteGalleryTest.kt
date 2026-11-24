package com.example.birchbark

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.birchbark.model.Campsite
import com.example.birchbark.model.CampsiteImage
import com.example.birchbark.ui.owner.CampsiteDetailsContent
import com.example.birchbark.ui.owner.CampsiteGalleryContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Intent tests for the owner's campsite gallery.
 * Covers US 02.01.02 (optional image), adding, viewing and removing photos,
 * and the View Gallery button on the owner's details screen (US 02.01.04).
 *
 * Uses fake photos and the drawing part of each screen, so these don't need
 * Firestore or a real photo. The fake photo text isn't a real picture, so each
 * one shows the grey "Photo" box, which is enough to check the screen.
 */
@RunWith(AndroidJUnit4::class)
class CampsiteGalleryTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val hopeLake = Campsite(
        id = "c1",
        ownerId = "owner123",
        name = "Hope Lake Campground",
        location = "Kananaskis, Alberta",
        maxOccupancy = 6
    )

    private val lakePhoto = CampsiteImage(id = "i1", campsiteId = "c1", ownerId = "owner123", data = "fake1", createdAt = 1L)
    private val tentPhoto = CampsiteImage(id = "i2", campsiteId = "c1", ownerId = "owner123", data = "fake2", createdAt = 2L)

    /** Draws the gallery with the given photos and records what the owner tapped. */
    private fun showGallery(
        images: List<CampsiteImage>,
        adding: Boolean = false,
        message: String? = null,
        onAdd: () -> Unit = {},
        onRemove: (CampsiteImage) -> Unit = {},
        onBack: () -> Unit = {}
    ) {
        composeRule.setContent {
            CampsiteGalleryContent(
                campsiteName = hopeLake.name,
                images = images,
                adding = adding,
                message = message,
                onAddClick = onAdd,
                onRemove = onRemove,
                onBack = onBack
            )
        }
    }

    @Test
    fun gallery_showsTitleWithCampsiteName() {
        showGallery(listOf(lakePhoto))
        composeRule.onNodeWithText("Hope Lake Campground Gallery").assertIsDisplayed()
    }

    @Test
    fun gallery_showsMessage_whenNoPhotos() {
        showGallery(emptyList())
        composeRule.onNodeWithTag("emptyGallery").assertIsDisplayed()
    }

    @Test
    fun gallery_showsEveryPhoto() {
        showGallery(listOf(lakePhoto, tentPhoto))
        composeRule.onAllNodesWithTag("galleryImage").assertCountEquals(2)
    }

    @Test
    fun gallery_addButtonOpensPicker() {
        var addTapped = false
        showGallery(emptyList(), onAdd = { addTapped = true })
        composeRule.onNodeWithTag("addImageButton").performClick()
        assertTrue(addTapped)
    }

    @Test
    fun gallery_addButtonOff_whileAddingPhoto() {
        showGallery(emptyList(), adding = true)
        composeRule.onNodeWithTag("addImageButton").assertIsNotEnabled()
        composeRule.onNodeWithText("Adding photo...").assertIsDisplayed()
    }

    @Test
    fun gallery_showsProblemMessage() {
        showGallery(emptyList(), message = "That photo is too big, try a different one")
        composeRule.onNodeWithText("That photo is too big, try a different one").assertIsDisplayed()
    }

    @Test
    fun gallery_tappingPhotoShowsItBig() {
        showGallery(listOf(lakePhoto, tentPhoto))
        composeRule.onAllNodesWithTag("galleryImage").onFirst().performClick()
        composeRule.onNodeWithTag("bigImage").assertIsDisplayed()
        composeRule.onNodeWithTag("removeImageButton").assertIsDisplayed()
    }

    @Test
    fun gallery_removeButtonRemovesThatPhoto() {
        var removed: CampsiteImage? = null
        showGallery(listOf(lakePhoto, tentPhoto), onRemove = { removed = it })
        composeRule.onAllNodesWithTag("galleryImage").onFirst().performClick()
        composeRule.onNodeWithTag("removeImageButton").performClick()
        assertEquals(lakePhoto, removed)
    }

    @Test
    fun gallery_backButtonGoesBack() {
        var backTapped = false
        showGallery(listOf(lakePhoto), onBack = { backTapped = true })
        composeRule.onNodeWithText("Back").performClick()
        assertTrue(backTapped)
    }

    @Test
    fun details_viewGalleryButtonOpensGallery() {
        var galleryTapped = false
        composeRule.setContent {
            CampsiteDetailsContent(
                campsite = hopeLake,
                onEditClick = {},
                onBack = {},
                onGalleryClick = { galleryTapped = true }
            )
        }
        composeRule.onNodeWithTag("viewGalleryButton").performScrollTo().performClick()
        assertTrue(galleryTapped)
    }
}