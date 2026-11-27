package com.example.birchbark

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.birchbark.model.ProfileImageValidator
import com.example.birchbark.util.ProfileImageCodec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

// See [TBD 9] in LLM.md

/**
 * Tests the real photo conversion on a device or emulator (the maths behind it
 * is unit tested in ProfileImageTest). Covers US 01.08.02: a picked photo becomes
 * a small square image that is small enough to save.
 *
 * Makes its own test pictures in the app's cache folder, so it needs no photos
 * on the phone and no Firestore.
 */
@RunWith(AndroidJUnit4::class)
class ProfileImageCodecTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    /** Saves a plain red picture of the given size and returns where it is. */
    private fun picture(width: Int, height: Int): Uri {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.RED)
        val file = File(context.cacheDir, "profile-test-${width}x$height.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return Uri.fromFile(file)
    }

    private fun decode(bytes: ByteArray): Bitmap =
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

    @Test
    fun widePhoto_becomesAFullSizeSquare() {
        val bytes = ProfileImageCodec.encode(context.contentResolver, picture(1000, 500))

        assertNotNull(bytes)
        val result = decode(bytes!!)
        assertEquals(ProfileImageCodec.MAX_SIDE, result.width)
        assertEquals(ProfileImageCodec.MAX_SIDE, result.height)
    }

    @Test
    fun tallPhoto_becomesAFullSizeSquare() {
        val bytes = ProfileImageCodec.encode(context.contentResolver, picture(500, 1000))

        val result = decode(bytes!!)
        assertEquals(ProfileImageCodec.MAX_SIDE, result.width)
        assertEquals(ProfileImageCodec.MAX_SIDE, result.height)
    }

    // a small photo is cut square but not blown up
    @Test
    fun smallPhoto_isNotEnlarged() {
        val bytes = ProfileImageCodec.encode(context.contentResolver, picture(100, 50))

        val result = decode(bytes!!)
        assertEquals(50, result.width)
        assertEquals(50, result.height)
    }

    @Test
    fun result_isSmallEnoughToSave() {
        val bytes = ProfileImageCodec.encode(context.contentResolver, picture(3000, 2000))

        assertTrue(bytes!!.size <= ProfileImageValidator.MAX_BYTES)
    }

    @Test
    fun aFileThatIsNotAnImage_givesNull() {
        val file = File(context.cacheDir, "not-an-image.txt")
        file.writeText("this is not a picture")

        assertNull(ProfileImageCodec.encode(context.contentResolver, Uri.fromFile(file)))
    }

    @Test
    fun aMissingFile_givesNull() {
        val missing = Uri.fromFile(File(context.cacheDir, "does-not-exist.png"))

        assertNull(ProfileImageCodec.encode(context.contentResolver, missing))
    }
}
