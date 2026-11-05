package com.example.birchbark.ui.owner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.birchbark.model.Campsite
import com.example.birchbark.model.CampsiteRepository

/**
 * US 02.01.04 View campsite details. The owner's campsite details screen from
 * the My Campgrounds mockup. Opens when the owner taps a campsite in their
 * list, shows all the saved details, and has a button to update them.
 *
 * CampsiteDetailsScreen loads the campsite from Firestore, and
 * CampsiteDetailsContent just draws the campsite it's given so the intent
 * tests can use a fake one.
 *
 * Outstanding issues. The mockup also has View Gallery, Campsite Lottery and
 * Generate QR Code buttons, those belong to other stories so they aren't here
 * yet. The image isn't shown since image upload isn't done, and map location
 * just shows the coordinates for now.
 */

/**
 * Loads one campsite and shows its details. Loads again every time the
 * screen opens, so changes from the edit screen show up right away.
 *
 * @param campsiteId the Firestore ID of the campsite to show.
 * @param onEditClick called with the campsite when the owner taps update.
 * @param onBack called when the owner taps back.
 */
@Composable
fun CampsiteDetailsScreen(
    campsiteId: String,
    onEditClick: (Campsite) -> Unit,
    onBack: () -> Unit
) {
    val repository = remember { CampsiteRepository() }
    var loadedCampsite by remember { mutableStateOf<Campsite?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    //load every time the screen opens
    LaunchedEffect(campsiteId) {
        repository.getCampsite(campsiteId, object : CampsiteRepository.LoadCallback {
            override fun onLoaded(campsite: Campsite) {
                loadedCampsite = campsite
            }

            override fun onNotFound() {
                errorMessage = "This campsite doesn't exist anymore"
            }

            override fun onError(error: Exception) {
                errorMessage = "Couldn't load the campsite, check your connection and try again"
            }
        })
    }

    val shown = loadedCampsite
    val error = errorMessage

    if (error != null) {
        Column(
            modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(error)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = onBack) { Text("Back") }
        }
    } else if (shown == null) {
        //still loading
        Box(
            modifier = Modifier.fillMaxSize().safeDrawingPadding(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        CampsiteDetailsContent(
            campsite = shown,
            onEditClick = { onEditClick(shown) },
            onBack = onBack
        )
    }
}

/**
 * Draws every saved detail of a campsite, in the same order as the mockup.
 * Anything the owner left empty says "Not set" so it's clear it's missing.
 *
 * @param campsite the campsite to show.
 * @param onEditClick called when the owner taps update.
 * @param onBack called when the owner taps back.
 */
@Composable
fun CampsiteDetailsContent(campsite: Campsite, onEditClick: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        //back button at the top, where the mockup has its back arrow
        TextButton(onClick = onBack) { Text("Back") }

        Text(
            campsite.name,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.testTag("detailsName")
        )
        Text(campsite.location, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        DetailRow("Description", campsite.description)
        DetailRow("Max occupancy", campsite.maxOccupancy.toString())
        DetailRow("Tags", campsite.tags.joinToString(", "))
        DetailRow("Price per night", campsite.price?.let { "$" + String.format("%.2f", it) } ?: "")
        DetailRow("Rules", campsite.rules)
        DetailRow("Cancellation policy", campsite.cancellationPolicy)
        DetailRow(
            "Map location",
            if (campsite.latitude != null && campsite.longitude != null) {
                "${campsite.latitude}, ${campsite.longitude}"
            } else {
                ""
            }
        )
        DetailRow(
            "Rating",
            campsite.averageRating()?.let { String.format("%.1f", it) } ?: "No ratings"
        )

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onEditClick,
            modifier = Modifier.fillMaxWidth().testTag("editButton")
        ) {
            Text("Update Campsite Details")
        }
    }
}

/**
 * One label and value on the details screen.
 *
 * @param label what the value is, like "Rules".
 * @param value the value, or empty to show "Not set".
 */
@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Text(if (value.isBlank()) "Not set" else value)
    }
}