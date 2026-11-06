package com.example.birchbark.ui.owner

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.birchbark.model.Campsite
import com.example.birchbark.ui.screens.EditLotteryRoute
import com.example.birchbark.waitlist.OwnerWaitlistScreen

/**
 * Switches between the owner campsite screens. Starts on the list of the
 * owner's campsites (US 02.03.01). Tapping one opens its details (US 02.01.04),
 * the update button opens the edit screen (US 02.01.03), the Campsite Lottery
 * button opens the lottery form (US 02.04.01), the Edit Lottery button opens
 * the lottery edit screen (US 02.04.06), and the register button opens the
 * register form (US 02.01.01, US 02.01.02).
 *
 * Uses a few variables to remember which screen is open instead of a navigation
 * library, so nothing new had to be added to the build file. The phone's back
 * button goes back one screen.
 */

/**
 * Shows whichever owner campsite screen is open.
 *
 * @param ownerId the owner's ID, passed to the list and register screens.
 */
@Composable
fun OwnerCampsitesApp(ownerId: String) {
    //null or false means that screen isn't open
    var registering by remember { mutableStateOf(false) }
    var selectedCampsiteId by remember { mutableStateOf<String?>(null) }
    var editingCampsite by remember { mutableStateOf<Campsite?>(null) }
    var lotteryCampsite by remember { mutableStateOf<Campsite?>(null) }
    var editingLotteryFor by remember { mutableStateOf<Campsite?>(null) }
    var waitlistLotteryId by remember { mutableStateOf<String?>(null) }
    val editing = editingCampsite
    val lotteryFor = lotteryCampsite
    val editLotteryFor = editingLotteryFor
    val selectedId = selectedCampsiteId
    val waitlistId = waitlistLotteryId

    if (waitlistId != null) {

        BackHandler {
            waitlistLotteryId = null
        }

        OwnerWaitlistScreen(
            lotteryId = waitlistId,
            onBack = { waitlistLotteryId = null }
        )

    } else if (registering) {
        //register screen, back or finishing goes to the list
        BackHandler { registering = false }
        RegisterCampsiteScreen(
            ownerId = ownerId,
            onDone = { registering = false },
            onCancel = { registering = false }
        )
    } else if (editing != null) {
        //edit screen, back goes to the details screen
        BackHandler { editingCampsite = null }
        EditCampsiteScreen(
            campsite = editing,
            onSaved = { editingCampsite = null },
            onCancel = { editingCampsite = null }
        )
    } else if (lotteryFor != null) {
        //lottery screen, back or saving goes to the details screen
        BackHandler { lotteryCampsite = null }
        CreateLotteryScreen(
            campsite = lotteryFor,
            onCreated = { lotteryCampsite = null },
            onCancel = { lotteryCampsite = null }
        )
    } else if (editLotteryFor != null) {
        //edit lottery screen (US 02.04.06), back goes to the details screen
        BackHandler { editingLotteryFor = null }
        EditLotteryRoute(
            campsite = editLotteryFor,
            onBack = { editingLotteryFor = null }
        )
    } else if (selectedId != null) {
        //details screen, back goes to the list
        BackHandler { selectedCampsiteId = null }
    CampsiteDetailsScreen(
        campsiteId = selectedId,
        onEditClick = { campsite -> editingCampsite = campsite },
        onBack = { selectedCampsiteId = null },
        onLotteryClick = { campsite -> lotteryCampsite = campsite },
        onEditLotteryClick = { campsite -> editingLotteryFor = campsite },
        onViewWaitlist = { lotteryId -> waitlistLotteryId = lotteryId }
    )
    } else {
        //list screen
        MyCampsitesScreen(
            ownerId = ownerId,
            onCampsiteClick = { campsite -> selectedCampsiteId = campsite.id },
            onRegisterClick = { registering = true }
        )
    }
}