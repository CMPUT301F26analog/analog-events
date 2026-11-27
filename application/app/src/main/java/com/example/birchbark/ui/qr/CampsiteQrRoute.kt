package com.example.birchbark.ui.qr

import androidx.activity.compose.BackHandler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.window.DialogProperties
import com.example.birchbark.model.*

/**
 * US 01.12.01
 *
 * Shared composable coordinator for internal scans and external links.
 * Loading/error/owner outcomes are dialogs; successful entrant and admin outcomes
 * are delegated to existing role-specific interfaces. No Firebase calls live here.
 *
 * @Note See #[RW-15], #[RW-16], #[RW-17], #[RW-18] in References.md
 * @Note See #[RW+91] in LLM.md
 *
 * @param payload raw scanner/deep-link contents.
 * @param activeRole role already authorized by RoleHome.
 * @param resolver injected campsite resolver.
 * @param onClose clears the pending request and returns to the previous role UI.
 * @param entrantDetails existing entrant details route (injectable for tests).
 * @param adminDetails existing admin details flow (injectable for tests).
 */
@Composable
fun CampsiteQrRoute(
    payload: String,
    activeRole: String,
    resolver: CampsiteQrResolver,
    onClose: () -> Unit,
    entrantDetails: @Composable (Campsite, () -> Unit) -> Unit,
    adminDetails: @Composable (Campsite, () -> Unit) -> Unit
) {
    val destination = campsiteQrDestinationForRole(activeRole)
    if (destination == CampsiteQrDestination.DENIED) {
        QrNotice("QR code unavailable", "Select an authorized role first.", onClose)
        return
    }
    var result by remember(payload, resolver) { mutableStateOf<CampsiteQrResult?>(null) }
    DisposableEffect(payload, resolver) {
        var active = true
        resolver.resolve(payload) { found -> if (active) result = found }
        onDispose { active = false }
    }
    when (val found = result) {
        null -> AlertDialog(onDismissRequest = onClose,
            title = { Text("Loading campsite") },
            text = { CircularProgressIndicator() },
            confirmButton = { TextButton(onClick = onClose) { Text("Cancel") } })
        is CampsiteQrResult.Invalid -> QrNotice("Invalid QR code", "This is not a BirchBark campsite QR code.", onClose)
        is CampsiteQrResult.Missing -> QrNotice("Campsite not found", "This campsite no longer exists or the code is incorrect.", onClose)
        is CampsiteQrResult.Failed -> QrNotice("Unable to load campsite", "Check your connection or access permissions and try again.", onClose)
        is CampsiteQrResult.Found -> when (destination) {
            CampsiteQrDestination.ENTRANT_DETAILS -> {
                BackHandler { onClose() }
                entrantDetails(found.campsite, onClose)
            }
            CampsiteQrDestination.ADMIN_DETAILS -> adminDetails(found.campsite, onClose)
            CampsiteQrDestination.OWNER_NOTICE -> QrNotice(
                found.campsite.name,
                "Campsite QR codes can be used to interact with campsites in entrant mode only.",
                onClose
            )
            CampsiteQrDestination.DENIED -> Unit
        }
    }
}

/**
 * A single-action dialog for QR outcomes.
 * @param title dialog title.
 * @param message explanatory text.
 * @param onClose invoked only by the OK button.
 */
@Composable
private fun QrNotice(title: String, message: String, onClose: () -> Unit) {
    AlertDialog(onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        title = { Text(title) }, text = { Text(message) },
        confirmButton = { TextButton(onClick = onClose, modifier = Modifier.testTag("qrNoticeOk")) { Text("OK") } })
}