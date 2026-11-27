package com.example.birchbark.util

// See [TBD 6] in LLM.md

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.ByteArrayOutputStream

/**
 * Turns a photo the entrant picked into a small square JPEG, small enough to be
 * saved in their profile document in Firestore (documents can't be over 1 MB).
 *
 * The photo is shrunk while it's read, turned the right way up (phone cameras
 * often save photos sideways with a note saying so), cut to a square from the
 * middle, scaled down to at most [MAX_SIDE] pixels, and saved as a JPEG.
 * The sizes come from [ImageSizing].
 *
 * Do this off the main thread, reading and shrinking a photo takes a moment.
 */
object ProfileImageCodec {

    /** The biggest side of the finished image, in pixels. */
    const val MAX_SIDE = 256

    /** JPEG quality from 0 to 100. 80 is small and still looks fine at avatar size. */
    const val JPEG_QUALITY = 80

    /**
     * Reads a picked photo and makes the small square JPEG.
     *
     * @param resolver used to open the photo's [uri].
     * @param uri where the photo is, e.g. from the photo picker.
     * @return the JPEG bytes, or null if the file can't be read as an image.
     */
    fun encode(resolver: ContentResolver, uri: Uri): ByteArray? {
        return try {
            //1. find the photo's size without loading it
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                return null
            }

            //2. load a shrunk copy, so a big photo doesn't use up the memory
            val options = BitmapFactory.Options().apply {
                inSampleSize = ImageSizing.sampleSize(bounds.outWidth, bounds.outHeight, MAX_SIDE)
            }
            val decoded = resolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            } ?: return null

            //3. turn it the right way up, then cut a square from the middle
            val upright = rotate(decoded, orientationOf(resolver, uri))
            val crop = ImageSizing.squareCrop(upright.width, upright.height)
            val square = Bitmap.createBitmap(upright, crop.left, crop.top, crop.side, crop.side)

            //4. scale down (never up) and save as a JPEG
            val side = ImageSizing.outputSide(square.width, MAX_SIDE)
            val scaled = if (side == square.width) {
                square
            } else {
                Bitmap.createScaledBitmap(square, side, side, true)
            }
            ByteArrayOutputStream().use { out ->
                scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
                out.toByteArray()
            }
        } catch (e: Exception) {
            null
        } catch (e: OutOfMemoryError) {
            null
        }
    }

    /** Reads which way up the camera said the photo is, or "normal" if it doesn't say. */
    private fun orientationOf(resolver: ContentResolver, uri: Uri): Int {
        return try {
            resolver.openInputStream(uri)?.use {
                ExifInterface(it).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (e: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }
    }

    /** Turns the picture so it's the right way up. Mirrored photos are left as they are. */
    private fun rotate(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
