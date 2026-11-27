package com.example.birchbark

import com.example.birchbark.model.CampsiteImage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the admin Images screen's search and delete-list rules.
 * Covers US 03.06.01: browse uploaded images, identify their campsite/profile,
 * and remove any image from the admin list after deletion succeeds.
 * References.md prompts #81 to #89.
 */
class AdminImageRulesTest {

    // US 03.06.01: search images by their associated campsite or profile name

    /** Verifies that a search finds a campsite when its name contains the query. */
    @Test
    fun searchMatchesCampsiteName() {
        assertTrue(
            AdminImageRules.matchesSearch(
                name = "Hope Lake Campground",
                query = "Hope Lake"
            )
        )
    }

    /** Verifies that a search finds a profile when its name contains the query. */
    @Test
    fun searchMatchesProfileName() {
        assertTrue(
            AdminImageRules.matchesSearch(
                name = "Anahita Houshyar",
                query = "Anahita"
            )
        )
    }

    /** Verifies that searching for part of a name still finds the image. */
    @Test
    fun searchMatchesPartOfName() {
        assertTrue(
            AdminImageRules.matchesSearch(
                name = "Hope Lake Campground",
                query = "Camp"
            )
        )
    }

    /** Verifies that search matching ignores uppercase and lowercase differences. */
    @Test
    fun searchIsCaseInsensitive() {
        assertTrue(
            AdminImageRules.matchesSearch(
                name = "Hope Lake Campground",
                query = "hOpE lAkE"
            )
        )
    }

    /** Verifies that leading and trailing spaces in the query are ignored. */
    @Test
    fun searchTrimsWhitespaceFromQuery() {
        assertTrue(
            AdminImageRules.matchesSearch(
                name = "Anahita Houshyar",
                query = "  Houshyar  "
            )
        )
    }

    /** Verifies that an empty query includes images even when no name is available. */
    @Test
    fun blankSearchMatchesAnyImage() {
        assertTrue(AdminImageRules.matchesSearch(name = null, query = ""))
    }

    /** Verifies that a whitespace-only query behaves like an empty query. */
    @Test
    fun whitespaceOnlySearchMatchesAnyImage() {
        assertTrue(AdminImageRules.matchesSearch(name = null, query = "   "))
    }

    /** Verifies that a non-empty query does not match an image with no associated name. */
    @Test
    fun nonBlankSearchDoesNotMatchMissingName() {
        assertFalse(AdminImageRules.matchesSearch(name = null, query = "Hope"))
    }

    /** Verifies that a query does not match an unrelated campsite or profile name. */
    @Test
    fun searchDoesNotMatchUnrelatedName() {
        assertFalse(
            AdminImageRules.matchesSearch(
                name = "Hope Lake Campground",
                query = "Banff"
            )
        )
    }

    // US 03.06.01 / US 03.03.01: remove a photo from the admin image list

    /** Verifies that deletion removes the selected image and keeps the others. */
    @Test
    fun deleteRemovesSelectedImageAndKeepsOtherImages() {
        val images = listOf(
            image(id = "image-1", ownerId = "owner-1"),
            image(id = "image-2", ownerId = "owner-2"),
            image(id = "image-3", ownerId = "owner-3")
        )

        val remainingImages = AdminImageRules.removeDeletedImage(
            images = images,
            imageId = "image-2"
        )

        assertEquals(listOf("image-1", "image-3"), remainingImages.map { it.id })
    }

    /** Verifies that the admin can remove an image uploaded by another owner. */
    @Test
    fun adminCanRemoveImageUploadedByAnotherOwner() {
        val images = listOf(
            image(id = "owner-photo", ownerId = "different-owner-id")
        )

        val remainingImages = AdminImageRules.removeDeletedImage(
            images = images,
            imageId = "owner-photo"
        )

        assertTrue(remainingImages.isEmpty())
    }

    /** Verifies that an unknown image ID leaves the image list unchanged. */
    @Test
    fun deleteWithUnknownIdLeavesImageListUnchanged() {
        val images = listOf(
            image(id = "image-1", ownerId = "owner-1"),
            image(id = "image-2", ownerId = "owner-2")
        )

        val remainingImages = AdminImageRules.removeDeletedImage(
            images = images,
            imageId = "missing-image"
        )

        assertEquals(images, remainingImages)
    }

    /** Builds realistic image records without needing Firebase in a unit test. */
    private fun image(id: String, ownerId: String) = CampsiteImage(
        id = id,
        campsiteId = "hope-lake-campground",
        ownerId = ownerId,
        data = "aGVsbG8=",
        createdAt = 1_795_652_706_246L
    )
}