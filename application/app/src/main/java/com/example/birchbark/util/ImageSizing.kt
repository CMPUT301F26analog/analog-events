package com.example.birchbark.util

// See [TBD 7] in LLM.md

/**
 * A square cut out of an image.
 *
 * @property left x position of the square's left edge, in pixels.
 * @property top y position of the square's top edge, in pixels.
 * @property side width and height of the square, in pixels.
 */
data class Crop(val left: Int, val top: Int, val side: Int)

/**
 * The maths for shrinking a picked photo into a small square profile image.
 * It's plain numbers with no Android code, so it can be unit tested.
 * [ProfileImageCodec] uses it on the real picture.
 */
object ImageSizing {

    /**
     * Works out how much to shrink a photo while reading it, so a 12 megapixel
     * camera photo doesn't use up the phone's memory. The result is a power of
     * two (1, 2, 4, 8...) and the shorter side stays at least [targetSide] pixels.
     *
     * @param width the photo's width in pixels.
     * @param height the photo's height in pixels.
     * @param targetSide the size the finished image needs, in pixels.
     * @return the sample size, 1 meaning don't shrink. Also 1 if any number is 0 or less.
     */
    fun sampleSize(width: Int, height: Int, targetSide: Int): Int {
        if (width <= 0 || height <= 0 || targetSide <= 0) {
            return 1
        }
        val shorter = minOf(width, height)
        var sample = 1
        while (shorter / (sample * 2) >= targetSide) {
            sample *= 2
        }
        return sample
    }

    /**
     * Finds the biggest square in the middle of a photo, so a wide or tall photo
     * is cut down the middle instead of being squashed.
     *
     * @param width the photo's width in pixels.
     * @param height the photo's height in pixels.
     * @return the square to keep.
     */
    fun squareCrop(width: Int, height: Int): Crop {
        val side = minOf(width, height)
        return Crop(left = (width - side) / 2, top = (height - side) / 2, side = side)
    }

    /**
     * Works out the size of the finished square. A small photo is kept as it is
     * instead of being blown up.
     *
     * @param side the side of the cropped square in pixels.
     * @param maxSide the biggest side allowed.
     * @return [side], or [maxSide] if the square is bigger than that.
     */
    fun outputSide(side: Int, maxSide: Int): Int = minOf(side, maxSide)
}