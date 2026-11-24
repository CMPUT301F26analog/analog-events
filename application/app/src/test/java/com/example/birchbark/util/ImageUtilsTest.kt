package com.example.birchbark.util

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for the photo size math in [ImageUtils].
 * Covers US 02.01.02 (optional image), the part that shrinks a photo so it
 * fits in Firestore.
 *
 * Only tests sampleSize and scaledSize, since reading and saving a real photo
 * needs a phone.
 */
class ImageUtilsTest {

    // sampleSize: how much to shrink while loading

    @Test
    fun sampleSize_smallPhoto_isNotShrunk() {
        assertEquals(1, ImageUtils.sampleSize(800, 600, 1024))
    }

    @Test
    fun sampleSize_photoExactlyMaxSide_isNotShrunk() {
        assertEquals(1, ImageUtils.sampleSize(1024, 768, 1024))
    }

    @Test
    fun sampleSize_bigPhoto_isShrunkByPowerOfTwo() {
        //4000 / 2 = 2000 and 4000 / 4 = 1000, 1000 is under 1024 so stop at 2
        assertEquals(2, ImageUtils.sampleSize(4000, 3000, 1024))
    }

    @Test
    fun sampleSize_hugePhoto_keepsShrinking() {
        //8192 / 8 = 1024 is still big enough, 8192 / 16 = 512 isn't
        assertEquals(8, ImageUtils.sampleSize(8192, 6144, 1024))
    }

    @Test
    fun sampleSize_tallPhoto_usesLongestSide() {
        assertEquals(2, ImageUtils.sampleSize(3000, 4000, 1024))
    }

    // scaledSize: the final size of the saved photo

    @Test
    fun scaledSize_smallPhoto_staysTheSame() {
        assertEquals(Pair(800, 600), ImageUtils.scaledSize(800, 600, 1024))
    }

    @Test
    fun scaledSize_widePhoto_longestSideBecomesMax() {
        assertEquals(Pair(1024, 768), ImageUtils.scaledSize(2048, 1536, 1024))
    }

    @Test
    fun scaledSize_tallPhoto_longestSideBecomesMax() {
        assertEquals(Pair(768, 1024), ImageUtils.scaledSize(1536, 2048, 1024))
    }

    @Test
    fun scaledSize_veryThinPhoto_neverGoesToZero() {
        val (width, height) = ImageUtils.scaledSize(10000, 1, 1024)
        assertEquals(1024, width)
        assertEquals(1, height)
    }
}