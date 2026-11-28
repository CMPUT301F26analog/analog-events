package com.example.birchbark.ui.qr

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.birchbark.qr.QrCodeScanner
import com.example.birchbark.qr.QrScanResult
import com.example.birchbark.qr.rememberZXingQrCodeScanner
import com.example.birchbark.ui.theme.ScreenTitle
import com.example.birchbark.ui.theme.BirchBarkColors
import com.example.birchbark.ui.theme.birchBarkButtonColors

/**
 * US 01.12.01
 *
 * Composable entry screen for the dedicated entrant Scan tab.
 * Receives an abstract scanner and forwards successful raw data to the shared route.
 *
 * @param onPayload receives raw QR contents for validation and campsite lookup.
 * @param scannerFactory composable factory; injectable for UI tests without camera hardware.
 */
@Composable
fun QrScanScreen(
    onPayload: (String) -> Unit,
    scannerFactory: @Composable ((QrScanResult) -> Unit) -> QrCodeScanner =
        { callback -> rememberZXingQrCodeScanner(callback) }
) {
    var scanning by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val scanner = scannerFactory { result ->
        scanning = false
        when (result) {
            is QrScanResult.Success -> onPayload(result.data)
            is QrScanResult.Cancelled -> message = "Scan cancelled."
            is QrScanResult.PermissionDenied -> message = "Camera permission denied. Enable it in Android settings and try again."
            is QrScanResult.CameraUnavailable -> message = "No camera is available on this device."
            is QrScanResult.Failed -> message = "Couldn't open the scanner. Please try again."
        }
    }
    Column(Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        ScreenTitle("Scan QR Code")
        Text("Scan a campsite's promotional QR code to view its details.", color = BirchBarkColors.Cream)
        Button(onClick = {
                scanning = true
                message = null
                scanner.scan()
        }, enabled = !scanning, colors = birchBarkButtonColors(),
            modifier = Modifier.fillMaxWidth().testTag("scanQrButton")) {
            Text("Open camera")
        }
        message?.let { Text(it, color = BirchBarkColors.Cream, modifier = Modifier.testTag("qrScanMessage")) }
    }
}