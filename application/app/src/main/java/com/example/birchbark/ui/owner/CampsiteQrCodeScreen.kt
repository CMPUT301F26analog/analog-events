package com.example.birchbark.ui.owner

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.birchbark.model.Campsite
import com.example.birchbark.model.CampsiteQrCodec
import com.example.birchbark.qr.*
import com.example.birchbark.ui.theme.ScreenTitle
import com.example.birchbark.ui.theme.BirchBarkColors
import com.example.birchbark.ui.theme.birchBarkButtonColors
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * US 02.02.01
 *
 * Owner route screens for viewing, saving and sharing the promotional QR code.
 * The unique QR payload is derived from the campsite ID created at registration;
 * the bitmap is rendered only when this route (screen) is opened.
 *
 * @Note See [RW-3-LLM] in LLM.md
 *
 * @param campsite the owner's existing, registered campsite.
 * @param onBack returns to its details.
 * @param generator abstract QR encoder; injectable without ZXing.
 * @param exporter optional injected Android export implementation.
 * @param codec instance used to encode the campsite ID.
 */
@Composable
fun CampsiteQrCodeScreen(
    campsite: Campsite,
    onBack: () -> Unit,
    generator: QrCodeGenerator = remember { ZXingQrCodeGenerator() },
    exporter: QrImageExporter? = null,
    codec: CampsiteQrCodec = remember { CampsiteQrCodec() }
) {
    val context = LocalContext.current
    val defaultExporter = remember(context) { AndroidQrImageExporter(context) }
    val imageExporter = exporter ?: defaultExporter
    val scope = rememberCoroutineScope()
    val generated = remember(campsite.id, generator, codec) {
        runCatching { generator.generate(codec.encode(campsite.id), 512) }
    }
    val image = generated.getOrNull()
    val bitmap = remember(image) { image?.toBitmap() }
    var working by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    val saveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { uri ->
        if (uri != null && image != null) {
            working = true
            message = null
            scope.launch {
                try {
                    withContext(Dispatchers.IO) { imageExporter.save(image, uri) }
                    message = "QR code saved."
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    message = "Couldn't save the QR code. Please try again."
                } finally { working = false }
            }
        } else {
            working = false
            message = "Save cancelled."
        }
    }
    CampsiteQrContent(campsite.name, bitmap, working,
        message ?: if (image == null) "Couldn't generate a QR code for this campsite." else null,
        onBack = onBack,
        onSave = {
            working = true
            message = null
            try { saveLauncher.launch("birchbark-${campsite.id}.png") }
            catch (_: Exception) { working = false; message = "Couldn't open the save dialog." }
        },
        onShare = {
            if (image != null) {
                working = true
                message = null
                scope.launch {
                    try {
                        val intent = withContext(Dispatchers.IO) { imageExporter.shareIntent(image) }
                        context.startActivity(intent)
                    } catch (error: CancellationException) {
                        throw error
                    } catch (_: Exception) {
                        message = "Couldn't share the QR code. Please try again."
                    } finally { working = false }
                }
            }
        }
    )
}

/**
 * Reusable presentation-only QR content, independent of camera, database
 * and Android export launchers. Tests can supply a bitmap and record button callbacks.
 *
 * @param campsiteName name displayed above the image.
 * @param bitmap generated QR, or null if generation failed.
 * @param working true while an export is in progress.
 * @param message optional status or error text.
 * @param onBack returns to campsite details.
 * @param onSave requests PNG export.
 * @param onShare requests the share chooser.
 */
@Composable
fun CampsiteQrContent(
    campsiteName: String,
    bitmap: Bitmap?,
    working: Boolean,
    message: String?,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(
        rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                    TextButton(onClick = onBack,
                                enabled = !working,
                                modifier = Modifier.align(Alignment.Start).testTag("qrBackButton"))
                    {
                        Text("Back")
                    }

                ScreenTitle("Campsite QR Code")
                Text(campsiteName,
                        color = BirchBarkColors.Cream,
                        style = MaterialTheme.typography.titleLarge)
                        bitmap?.let {
                                Image(bitmap = it.asImageBitmap(),
                                    filterQuality = FilterQuality.None,
                                    contentDescription = "Promotional QR code for $campsiteName",
                                     modifier = Modifier.sizeIn(maxWidth = 320.dp, maxHeight = 320.dp)
                                            .fillMaxWidth().aspectRatio(1f).testTag("campsiteQrImage"))
                        }

                Button(onClick = onSave,
                        enabled = bitmap != null && !working,
                        colors = birchBarkButtonColors(),
                        modifier = Modifier.fillMaxWidth().testTag("saveQrButton"))
                        {
                            Text("Save QR Code")
                        }

                Button(onClick = onShare,
                        enabled = bitmap != null && !working,
                        colors = birchBarkButtonColors(),
                        modifier = Modifier.fillMaxWidth().testTag("shareQrButton"))
                        {
                            Text("Share QR Code")
                        }
        message?.let { Text(it, color = BirchBarkColors.Cream) }
    }
}