package com.example.birchbark.qr

/** US 01.12.01
 *
 * @Note See #[RW+91] in LLM.md
 *
 *  Sealed result interface for a single scanner request.
 **/
sealed interface QrScanResult {
    /** @property data raw, untrusted contents of the scanned QR code. */
    data class Success(val data: String) : QrScanResult
    /** The scan ended without a QR payload. */
    class Cancelled : QrScanResult
    /** Camera access was not granted. */
    class PermissionDenied : QrScanResult
    /** The device has no available camera feature. */
    class CameraUnavailable : QrScanResult
    /** The scanner could not be launched. */
    class Failed : QrScanResult
}

/**
 * Scanner interface. A concrete implementation delivers results
 * through the callback configured at construction time; it knows no campsite model.
 */
fun interface QrCodeScanner {
    /** Starts a single QR scan. Results arrive asynchronously through its callback. */
    fun scan()
}