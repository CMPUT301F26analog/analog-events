package com.example.birchbark.qr

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * US 02.02.01
 *
 * Concrete implementation of [QrCodeGenerator] using ZXing.
 *
 * Adapts the BitMatrix-to-pixels approach from Blair Fernandes' article:
 * https://blair49.medium.com/how-to-add-qr-code-generation-and-scanning-in-your-android-app-7baff15bd7c6
 *
 * @Note See #[RW-15], #[RW-16], #[RW-17], #[RW-18] in References.md
 * @Note See #[RW+91] in LLM.md
 *
 * A black-on-white symbol and four-module quiet zone preserve scan contrast.
 * No Android Bitmap is needed until presentation/export, enabling JVM tests.
 *
 */
class ZXingQrCodeGenerator : QrCodeGenerator {
    /**
     * @param data text to encode.
     * @param size requested square image dimensions, from 64 to 2048 pixels.
     * @return QR image containing the encoded text.
     * @throws IllegalArgumentException for blank text or unsupported dimensions.
     * @throws WriterException if ZXing cannot encode the supplied data.
     */
    override fun generate(data: String, size: Int): QrImage {
        require(data.isNotBlank()) { "QR data cannot be blank." }
        require(size in 64..2048) { "Unsupported QR image size." }
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.MARGIN to 4,
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M
        )
        val matrix = MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, size, size, hints)
        val pixels = IntArray(matrix.width * matrix.height) { index ->
            if (matrix[index % matrix.width, index / matrix.width]) 0xFF000000.toInt()
            else 0xFFFFFFFF.toInt()
        }
        return QrImage(matrix.width, matrix.height, pixels)
    }
}