package com.example.birchbark.qr

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.io.OutputStream

/**
 * US 02.02.01
 *
 * Concrete Android extension converting a platform-independent QR image.
 *
 * @Note See #[RW-15], #[RW-16], #[RW-17], #[RW-18] in References.md
 * @Note See #[RW+91] in LLM.md
 *
 * @return a lossless ARGB bitmap suitable for Compose and PNG export.
 */
fun QrImage.toBitmap(): Bitmap = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)

/** Interface for saving and sharing QR images using Android destinations. */
interface QrImageExporter {
    /**
     * @param image generated QR image.
     * @param destination URI selected by the user through CreateDocument.
     * @throws IOException if the image cannot be saved.
     */
    fun save(image: QrImage, destination: Uri)

    /**
     * @param image generated QR image.
     * @return Android share chooser intent with a temporary read-only PNG URI.
     * @throws IOException if the image cannot be written.
     */
    fun shareIntent(image: QrImage): Intent
}

/**
 * Concrete lossless PNG exporter using the Storage Access Framework
 * and FileProvider. No shared storage permission or Firestore image upload is needed.
 *
 * @param context Android context for content resolver and app-private cache.
 */
class AndroidQrImageExporter(private val context: Context) : QrImageExporter {
    /**
     * Writes PNG data to the user's selected destination; call on an IO thread.
     * @param image generated QR image.
     * @param destination selected writable content URI.
     * @throws IOException if the destination cannot be opened or written.
     */
    override fun save(image: QrImage, destination: Uri) {
        val stream = context.contentResolver.openOutputStream(destination, "w")
            ?: throw IOException("Unable to open destination.")
        stream.use { writePng(image, it) }
    }

    /**
     * Writes a temporary private PNG and prepares a share chooser; call on an IO thread.
     * @param image generated QR image.
     * @return chooser intent to launch from the UI thread.
     * @throws IOException if the temporary image cannot be written.
     */
    override fun shareIntent(image: QrImage): Intent {
        val directory = File(context.cacheDir, "qr")
        if (!directory.exists() && !directory.mkdirs()) throw IOException("Unable to create QR cache.")
        val file = File.createTempFile("birchbark-qr-", ".png", directory)
        try {
            file.outputStream().use { writePng(image, it) }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.qrfiles", file)
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = ClipData.newRawUri("Campsite QR code", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            return Intent.createChooser(send, "Share campsite QR code")
        } catch (error: Exception) {
            file.delete()
            throw error
        }
    }

    /**
     * Writes the QR to a PNG image, recycles
     * bitmap when complete.
     *
     * @param image generated QR image.
     * @param stream caller-owned destination stream.
     * @throws IOException if the encoder cannot write PNG data.
     */
    private fun writePng(image: QrImage, stream: OutputStream) {
        val bitmap = image.toBitmap()
        try {
            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                throw IOException("Unable to encode QR image.")
            }
        } finally {
            bitmap.recycle()
        }
    }
}