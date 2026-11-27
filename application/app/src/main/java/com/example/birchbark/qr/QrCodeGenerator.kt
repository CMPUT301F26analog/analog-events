package com.example.birchbark.qr

/**
 * US 02.02.01
 *
 * Concrete, platform-independent QR pixel image generator.
 *
 * Interfaces with the QRCodeGenerator class to encode text as QR image.
 *
 * @Note See #[RW+91] in LLM.md
 *
 * @property width image width in pixels.
 * @property height image height in pixels.
 * @property pixels row-major ARGB pixels; treated as read-only by consumers.
 */
class QrImage(val width: Int, val height: Int, val pixels: IntArray)

/**
 * Interface for encoding arbitrary text into a QR image.
 * Does not depend on Android, Firestore or a specific barcode library.
 */
interface QrCodeGenerator {
    /**
     * @param data text to encode.
     * @param size requested square image size in pixels.
     * @return scannable QR pixel image (actual dimensions may exceed the request).
     * @throws IllegalArgumentException for invalid input.
     */
    fun generate(data: String, size: Int): QrImage
}