package com.example.birchbark.qr

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.birchbark.AdminScannedCampsiteDetails
import com.example.birchbark.UserProfile
import com.example.birchbark.model.*
import com.example.birchbark.ui.entrant.EntrantApp
import com.example.birchbark.ui.entrant.CampsiteDetailsScreen
import com.example.birchbark.ui.owner.CampsiteDetailsContent
import com.example.birchbark.ui.owner.CampsiteQrContent
import com.example.birchbark.ui.qr.CampsiteQrRoute
import com.example.birchbark.ui.qr.QrScanScreen
import com.example.birchbark.ui.theme.BirchBarkTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * US 01.12.01 and US 02.02.01: concrete Android instrumented Compose test suite.
 * Reuses actual entrant tab, details content, admin dialog and owner QR UI.
 * A fake scanner/lookup isolates navigation from camera and Firebase.
 * System camera, picker, share chooser and real deep-link delivery are tested
 * separately on an Android device.
 */
class QrCodeUiTest {
    @get:Rule val compose = createComposeRule()
    private val campsite = Campsite(id = "ABC123", ownerId = "owner123", name = "Pine Ridge", location = "Jasper", maxOccupancy = 4)
    private val codec = CampsiteQrCodec()
    private val payload = codec.encode(campsite.id)
    private val resolver = CampsiteQrResolver(CampsiteQrLookup { _, callback ->
        callback(CampsiteQrResult.Found(campsite))
    })

    /** Navigate through the real Scan tab, fake a scan, then return to the Scan tab. */
    @Test fun scan_tab_navigates_to_existing_entrant_details() {
        compose.setContent {
            BirchBarkTheme {
                val holder = rememberSaveableStateHolder()
                var request by remember { mutableStateOf<String?>(null) }
                val current = request
                if (current == null) {
                    holder.SaveableStateProvider("entrantHome") {
                        EntrantApp(
                            profile = UserProfile(fid = "entrant123", firstName = "Test", roles = listOf("entrant")),
                            onProfileDeleted = {},
                            onQrPayload = { request = it },
                            qrScannerFactory = { callback -> QrCodeScanner { callback(QrScanResult.Success(payload)) } }
                        )
                    }
                } else {
                    CampsiteQrRoute(current, "entrant", resolver, onClose = { request = null },
                        entrantDetails = { site, close ->
                            CampsiteDetailsScreen(site, null, false, false, null, close, {}, {}, {})
                        }, adminDetails = { _, _ -> })
                }
            }
        }
        compose.onNodeWithTag("entrant-scan-tab").performClick()
        compose.onNodeWithTag("scanQrButton").performClick()
        compose.onNodeWithText("Pine Ridge").assertIsDisplayed()
        compose.onNodeWithText("Back", substring = false).performClick()
        compose.onNodeWithTag("scanQrButton").assertIsDisplayed()
    }

    /** A fake cancelled scan remains on the scanner screen. */
    @Test fun cancelling_scan_does_not_navigate_from_scan_screen() {
        compose.setContent { BirchBarkTheme {
            QrScanScreen(onPayload = { error("Cancelled scan navigated") },
                scannerFactory = { callback -> QrCodeScanner { callback(QrScanResult.Cancelled()) } })
        } }
        compose.onNodeWithTag("scanQrButton").performClick()
        compose.onNodeWithText("Scan cancelled.").assertIsDisplayed()
        compose.onNodeWithTag("scanQrButton").assertIsEnabled()
    }

    /** Permission denial is communicated without navigating or crashing. */
    @Test fun denied_camera_permission_can_not_communicate_with_camera() {
        compose.setContent { BirchBarkTheme {
            QrScanScreen(onPayload = { error("Denied scan navigated") },
                scannerFactory = { callback -> QrCodeScanner { callback(QrScanResult.PermissionDenied()) } })
        } }
        compose.onNodeWithTag("scanQrButton").performClick()
        compose.onNodeWithText("Camera permission denied.", substring = true).assertIsDisplayed()
    }

    /** Invalid QR shows an error and never opens details. */
    @Test fun invalid_QrCode_displays_error_when_scanned() {
        compose.setContent { BirchBarkTheme {
            CampsiteQrRoute("invalid", "entrant", resolver, {},
                entrantDetails = { _, _ -> error("Unexpected entrant route") },
                adminDetails = { _, _ -> error("Unexpected admin route") })
        } }
        compose.onNodeWithText("Invalid QR code").assertIsDisplayed()
        compose.onNodeWithTag("qrNoticeOk").assertIsDisplayed()
    }

    /** Server-reported missing campsite produces its own error. */
    @Test fun deleted_campsite_shows_not_found_from_deprecated_code() {
        val missing = CampsiteQrResolver(CampsiteQrLookup { _, callback -> callback(CampsiteQrResult.Missing()) })
        compose.setContent { BirchBarkTheme {
            CampsiteQrRoute(payload, "entrant", missing, {},
                entrantDetails = { _, _ -> error("Missing site navigated") }, adminDetails = { _, _ -> })
        } }
        compose.onNodeWithText("Campsite not found").assertIsDisplayed()
    }

    /** Owner mode displays only its acknowledgement, never the entrant/admin details. */
    @Test fun owner_can_only_acknowledge_details_can_not_interact_with_details() {
        var closed = false
        compose.setContent { BirchBarkTheme {
            CampsiteQrRoute(payload, "owner", resolver, { closed = true },
                entrantDetails = { _, _ -> error("Unexpected entrant route") },
                adminDetails = { _, _ -> error("Unexpected admin route") })
        } }
        compose.onNodeWithText("Pine Ridge").assertIsDisplayed()
        compose.onNodeWithTag("adminQrDeleteButton").assertDoesNotExist()
        compose.onNodeWithTag("qrNoticeOk").performClick()
        compose.runOnIdle { assertEquals(true, closed) }
    }

    /** Admin uses the same moderation dialog as the campsite browse screen. */
    @Test fun admin_gets_existing_moderation_details() {
        compose.setContent { BirchBarkTheme {
            CampsiteQrRoute(payload, "admin", resolver, {},
                entrantDetails = { _, _ -> error("Unexpected entrant route") },
                adminDetails = { site, close -> AdminScannedCampsiteDetails(site, close) })
        } }
        compose.onNodeWithText("Pine Ridge").assertIsDisplayed()
        compose.onNodeWithTag("adminQrDeleteButton").performClick()
        compose.onNodeWithText("Delete campsite?").assertIsDisplayed()
        compose.onNodeWithText("Cancel", substring = false).performClick()
        compose.onNodeWithTag("adminQrCloseButton").assertIsDisplayed()
    }

    /** Owner can navigate from actual details to the QR content and request export. */
    @Test fun owner_Qr_navigation_from_QR_image_and_go_to_export_buttons() {
        var saved = 0
        var shared = 0
        val bitmap = ZXingQrCodeGenerator().generate(payload, 512).toBitmap()
        compose.setContent { BirchBarkTheme {
            var showQr by remember { mutableStateOf(false) }
            if (showQr) {
                CampsiteQrContent(campsite.name, bitmap, false, null,
                    onBack = { showQr = false }, onSave = { saved++ }, onShare = { shared++ })
            } else {
                CampsiteDetailsContent(campsite, onEditClick = {}, onBack = {}, onQrClick = { showQr = true })
            }
        } }
        compose.onNodeWithTag("viewQrButton").performScrollTo().performClick()
        compose.onNodeWithTag("campsiteQrImage").assertExists()
        compose.onNodeWithTag("saveQrButton").performScrollTo().performClick()
        compose.onNodeWithTag("shareQrButton").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, saved); assertEquals(1, shared) }
        compose.onNodeWithTag("qrBackButton").performScrollTo().performClick()
        compose.onNodeWithTag("viewQrButton").assertExists()
    }
}