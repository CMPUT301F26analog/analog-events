package com.example.birchbark.qr

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

/**
 * US 01.12.01
 * Concrete scanner adapter; hides the Android camera launcher.
 *
 * @Note See [RW-2] in References.md
 * @Note Seet [RW-1-LLM] in LLM.md
 *
 * @param launch action that starts the configured JourneyApps activity.
 */
class ZXingQrCodeScanner(private val launch: () -> Unit) : QrCodeScanner {
    /** Starts the configured camera scan. */
    override fun scan() = launch()
}

/**
 * Composable factory binding the scanner to the Activity Result lifecycle.
 *
 * Uses JourneyApps' ScanContract instead of the deprecated IntentIntegrator
 * from Blair Fernandes' article. JourneyApps requests runtime camera permission.
 *
 * @param onResult callback receiving the scan outcome on the UI thread.
 * @return reusable scanner interface; the latest callback is used after recomposition.
 */
@Composable
fun rememberZXingQrCodeScanner(onResult: (QrScanResult) -> Unit): QrCodeScanner {
    val context = LocalContext.current
    val currentResult = rememberUpdatedState(onResult)
    val launcher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val data = result.contents
        currentResult.value(when {
            data != null -> QrScanResult.Success(data)
            result.originalIntent?.getBooleanExtra(Intents.Scan.MISSING_CAMERA_PERMISSION, false) == true ->
                QrScanResult.PermissionDenied()
            else -> QrScanResult.Cancelled()
        })
    }
    return remember(context, launcher) {
        ZXingQrCodeScanner {
            if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                currentResult.value(QrScanResult.CameraUnavailable())
            } else {
                try {
                    launcher.launch(ScanOptions().apply {
                        setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                        setPrompt("Scan a BirchBark campsite QR code")
                        setBeepEnabled(false)
                        setOrientationLocked(true)
                    })
                } catch (_: Exception) {
                    currentResult.value(QrScanResult.Failed())
                }
            }
        }
    }
}