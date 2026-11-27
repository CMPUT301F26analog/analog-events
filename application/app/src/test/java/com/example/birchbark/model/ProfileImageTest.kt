package com.example.birchbark.model

import com.example.birchbark.util.Crop
import com.example.birchbark.util.ImageSizing
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// See [TBD 8] in LLM.md

/**
 * Unit tests for the profile image: the sizing maths, the validator, and that an
 * invalid image is refused before Firestore is touched.
 * Covers US 01.08.02. (Reading a real photo is tested in ProfileImageCodecTest.)
 */
class ProfileImageTest {

    // sampleSize: how much to shrink a photo while reading it

    @Test
    fun sampleSize_isOne_forASmallPhoto() {
        assertEquals(1, ImageSizing.sampleSize(200, 200, 256))
    }

    @Test
    fun sampleSize_shrinksABigCameraPhoto() {
        //3000 -> 1500 -> 750 -> 375 is still over 256, one more (187) would be under
        assertEquals(8, ImageSizing.sampleSize(4000, 3000, 256))
    }

    @Test
    fun sampleSize_usesTheShorterSide() {
        assertEquals(8, ImageSizing.sampleSize(3000, 4000, 256))
    }

    @Test
    fun sampleSize_stopsWhenTheShorterSideWouldGoUnderTheTarget() {
        assertEquals(2, ImageSizing.sampleSize(512, 512, 256))
        assertEquals(1, ImageSizing.sampleSize(511, 511, 256))
    }

    @Test
    fun sampleSize_isOne_forBadNumbers() {
        assertEquals(1, ImageSizing.sampleSize(0, 100, 256))
        assertEquals(1, ImageSizing.sampleSize(100, -5, 256))
        assertEquals(1, ImageSizing.sampleSize(100, 100, 0))
    }

    // squareCrop: the square from the middle

    @Test
    fun squareCrop_cutsTheMiddleOfAWidePhoto() {
        assertEquals(Crop(left = 100, top = 0, side = 200), ImageSizing.squareCrop(400, 200))
    }

    @Test
    fun squareCrop_cutsTheMiddleOfATallPhoto() {
        assertEquals(Crop(left = 0, top = 100, side = 200), ImageSizing.squareCrop(200, 400))
    }

    @Test
    fun squareCrop_keepsASquarePhoto() {
        assertEquals(Crop(left = 0, top = 0, side = 300), ImageSizing.squareCrop(300, 300))
    }

    @Test
    fun squareCrop_roundsDown_whenTheDifferenceIsOdd() {
        assertEquals(Crop(left = 1, top = 0, side = 2), ImageSizing.squareCrop(5, 2))
    }

    // outputSide: shrink big photos, never enlarge small ones

    @Test
    fun outputSide_isCappedAtTheMax() {
        assertEquals(256, ImageSizing.outputSide(1000, 256))
    }

    @Test
    fun outputSide_keepsASmallPhoto() {
        assertEquals(100, ImageSizing.outputSide(100, 256))
    }

    // validator

    private val image = ByteArray(1000)

    @Test
    fun validatorPasses_forARealImage() {
        assertTrue(ProfileImageValidator.validate("fid123", image).isEmpty())
    }

    @Test
    fun validatorFails_whenNoProfile() {
        assertEquals(listOf("Profile is required"), ProfileImageValidator.validate("  ", image))
    }

    @Test
    fun validatorFails_whenImageIsEmpty() {
        assertEquals(listOf("Choose an image"), ProfileImageValidator.validate("fid123", ByteArray(0)))
    }

    @Test
    fun validatorPasses_whenExactlyTheLimit() {
        val atLimit = ByteArray(ProfileImageValidator.MAX_BYTES)
        assertTrue(ProfileImageValidator.validate("fid123", atLimit).isEmpty())
    }

    @Test
    fun validatorFails_whenOneByteOverTheLimit() {
        val tooBig = ByteArray(ProfileImageValidator.MAX_BYTES + 1)
        assertEquals(listOf("Image is too large"), ProfileImageValidator.validate("fid123", tooBig))
    }

    @Test
    fun validatorReturnsAllErrors_whenEverythingWrong() {
        assertEquals(2, ProfileImageValidator.validate("", ByteArray(0)).size)
    }

    // repository: an invalid image is refused before touching Firestore

    /** Remembers which callback function was called. */
    private class RecordingCallback : ProfileImageRepository.SaveCallback {
        var saved: ByteArray? = null
        var errors: List<String>? = null
        var failure: Exception? = null

        override fun onSaved(image: ByteArray) {
            saved = image
        }

        override fun onInvalid(errors: List<String>) {
            this.errors = errors
        }

        override fun onError(error: Exception) {
            failure = error
        }
    }

    @Test
    fun setImage_callsOnInvalidOnly_whenImageIsEmpty() {
        val callback = RecordingCallback()
        ProfileImageRepository().setImage("fid123", ByteArray(0), callback)

        assertEquals(listOf("Choose an image"), callback.errors)
        assertNull(callback.saved)
        assertNull(callback.failure)
    }

    @Test
    fun setImage_reportsEveryProblem_whenEverythingWrong() {
        val callback = RecordingCallback()
        ProfileImageRepository().setImage("", ByteArray(0), callback)

        assertEquals(2, callback.errors?.size)
        assertNull(callback.saved)
    }
}