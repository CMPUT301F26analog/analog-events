package com.example.birchbark.util

// See #053 in LLM.md

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayOutputStream

/**
 * Turns photos into text so they can be saved in Firestore, and back into
 * pictures so screens can show them. US 02.01.02 (optional image) and the
 * image gallery.
 *
 * Firebase Storage needs a paid plan, so photos are saved in Firestore as
 * Base64 text instead. Phone photos are way too big for that (a Firestore
 * document can't go over 1 MB), so every photo gets shrunk first.
 *
 * The size math is in its own small functions ([sampleSize], [scaledSize])
 * so it can be unit tested without a phone.
 *
 * Outstanding issues. Photos taken sideways on some phones may show up
 * rotated, since the rotation info in the photo isn't read.
 */
object ImageUtils {

    /** Longest side of a saved photo, in pixels. Big enough to look good on a phone screen. */
    const val MAX_SIDE = 1024

    /** JPEG quality to start at. Goes lower if the photo is still too big. */
    private const val START_QUALITY = 80

    /** Lowest JPEG quality we'll try before giving up. */
    private const val MIN_QUALITY = 30

    /**
     * How much to shrink a photo while loading it, so a huge photo never has
     * to be loaded at full size. Android only shrinks by powers of 2 here
     * (1, 2, 4, 8...), so this picks the biggest one that still keeps the
     * photo at least [maxSide] on its longest side.
     *
     * @param width the photo's width in pixels.
     * @param height the photo's height in pixels.
     * @param maxSide the longest side we want in the end.
     * @return 1 for no shrinking, otherwise 2, 4, 8 and so on.
     */
    fun sampleSize(width: Int, height: Int, maxSide: Int = MAX_SIDE): Int {
        val longest = maxOf(width, height)
        var size = 1
        while (longest / (size * 2) >= maxSide) {
            size *= 2
        }
        return size
    }

    /**
     * The final size of a photo so its longest side is at most [maxSide],
     * keeping the same shape. Photos already small enough stay the same.
     *
     * @param width the photo's width in pixels.
     * @param height the photo's height in pixels.
     * @param maxSide the longest side allowed.
     * @return the new width and height.
     */
    fun scaledSize(width: Int, height: Int, maxSide: Int = MAX_SIDE): Pair<Int, Int> {
        val longest = maxOf(width, height)
        if (longest <= maxSide) return Pair(width, height)
        val scale = maxSide.toDouble() / longest
        val newWidth = maxOf(1, (width * scale).toInt())
        val newHeight = maxOf(1, (height * scale).toInt())
        return Pair(newWidth, newHeight)
    }

    /**
     * Reads a photo the owner picked, shrinks it and turns it into Base64
     * text ready to save in a [com.example.birchbark.model.CampsiteImage].
     *
     * @param context used to open the picked photo.
     * @param uri the photo the owner picked from their phone.
     * @param maxChars the biggest the text is allowed to be.
     * @return the photo as Base64 text, or null if it couldn't be read or
     *   couldn't be made small enough.
     */
    fun uriToBase64(context: Context, uri: Uri, maxChars: Int): String? {
        return try {
            //first only read the photo's size, not the whole photo
            val sizeOptions = BitmapFactory.Options()
            sizeOptions.inJustDecodeBounds = true
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, sizeOptions)
            }
            if (sizeOptions.outWidth <= 0 || sizeOptions.outHeight <= 0) return null

            //then load it already shrunk a bit so big photos don't run out of memory
            val loadOptions = BitmapFactory.Options()
            loadOptions.inSampleSize = sampleSize(sizeOptions.outWidth, sizeOptions.outHeight)
            val loaded = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, loadOptions)
            } ?: return null

            //shrink it to the final size
            val (width, height) = scaledSize(loaded.width, loaded.height)
            val shrunk = Bitmap.createScaledBitmap(loaded, width, height, true)

            //save as JPEG, lowering the quality until it fits
            var quality = START_QUALITY
            while (quality >= MIN_QUALITY) {
                val bytes = ByteArrayOutputStream()
                shrunk.compress(Bitmap.CompressFormat.JPEG, quality, bytes)
                val text = Base64.encodeToString(bytes.toByteArray(), Base64.NO_WRAP)
                if (text.length <= maxChars) return text
                quality -= 10
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Turns saved Base64 text back into a picture a screen can show.
     *
     * @param data the photo as Base64 text ([com.example.birchbark.model.CampsiteImage.data]).
     * @return the picture, or null if the text isn't a valid photo.
     */
    fun base64ToImageBitmap(data: String): ImageBitmap? {
        return try {
            val bytes = Base64.decode(data, Base64.NO_WRAP)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
}