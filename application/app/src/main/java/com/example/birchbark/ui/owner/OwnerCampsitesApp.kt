package com.example.birchbark.ui.owner

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.birchbark.model.Campsite

/**
 * Switches between the owner campsite screens. Starts on the list of the
 * owner's campsites (US 02.03.01). Tapping one opens its details (US 02.01.04),
 * the update button opens the edit screen (US 02.01.03), and the register
 * button opens the register form (US 02.01.01, US 02.01.02).
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

    val editing = editingCampsite
    val selectedId = selectedCampsiteId

    if (registering) {
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
    } else if (selectedId != null) {
        //details screen, back goes to the list
        BackHandler { selectedCampsiteId = null }
        CampsiteDetailsScreen(
            campsiteId = selectedId,
            onEditClick = { campsite -> editingCampsite = campsite },
            onBack = { selectedCampsiteId = null }
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