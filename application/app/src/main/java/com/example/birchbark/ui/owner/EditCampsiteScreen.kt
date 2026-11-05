package com.example.birchbark.ui.owner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.birchbark.model.Campsite
import com.example.birchbark.model.CampsiteRepository

/**
 * US 02.01.03 Update campsite details. The "Update my Campsite" screen from the
 * My Campgrounds mockup. It's already filled in with the current details, and
 * saving updates the campsite in Firebase.
 *
 * EditCampsiteScreen does the saving, and EditCampsiteForm is just the form so
 * the intent tests can check it without needing Firestore.
 *
 * Outstanding issues. The mockup also has Replace Camp Pin and Add more images
 * buttons, those need the map and image upload so they aren't here yet. Map
 * location and image just keep whatever was saved before.
 */

/**
 * Shows the edit form and saves the changes to Firestore.
 *
 * @param campsite the campsite being edited, as it is right now.
 * @param onSaved called once the changes are saved.
 * @param onCancel called if the owner goes back without saving.
 */
@Composable
fun EditCampsiteScreen(campsite: Campsite, onSaved: () -> Unit, onCancel: () -> Unit) {
    val repository = remember { CampsiteRepository() }
    var saveErrors by remember { mutableStateOf<List<String>>(listOf()) }
    var saving by remember { mutableStateOf(false) }

    EditCampsiteForm(
        campsite = campsite,
        errors = saveErrors,
        saving = saving,
        onSave = { updated ->
            saving = true
            repository.updateCampsite(updated, object : CampsiteRepository.SaveCallback {
                override fun onSuccess(campsiteId: String) {
                    saving = false
                    onSaved()
                }

                override fun onInvalid(errors: List<String>) {
                    saving = false
                    saveErrors = errors
                }

                override fun onError(error: Exception) {
                    saving = false
                    saveErrors = listOf("Couldn't save, check your connection and try again")
                }
            })
        },
        onCancel = onCancel
    )
}

/**
 * The edit form. Starts filled in with the campsite's current details.
 *
 * @param campsite the campsite being edited, used to fill in the fields.
 * @param errors messages to show above the update button, empty if there are none.
 * @param saving true while it's saving, so the button can't be tapped twice.
 * @param onSave called with the edited campsite when the owner taps update.
 * @param onCancel called when the owner taps back.
 */
@Composable
fun EditCampsiteForm(
    campsite: Campsite,
    errors: List<String>,
    saving: Boolean,
    onSave: (Campsite) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(campsite.name) }
    var location by remember { mutableStateOf(campsite.location) }
    var description by remember { mutableStateOf(campsite.description) }
    var maxOccupancy by remember { mutableStateOf(campsite.maxOccupancy.toString()) }
    var tags by remember { mutableStateOf(campsite.tags.joinToString(", ")) }
    var price by remember { mutableStateOf(campsite.price?.toString() ?: "") }
    var rules by remember { mutableStateOf(campsite.rules) }
    var cancellationPolicy by remember { mutableStateOf(campsite.cancellationPolicy) }
    var priceError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        //back button at the top, where the mockup has its back arrow
        TextButton(onClick = onCancel) { Text("Back") }

        Text("Update my Campsite", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Campsite Name") },
            modifier = Modifier.fillMaxWidth().testTag("nameField")
        )
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth().testTag("locationField")
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description and amenities") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = maxOccupancy,
            onValueChange = { maxOccupancy = it },
            label = { Text("Max Occupancy") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().testTag("maxOccupancyField")
        )
        OutlinedTextField(
            value = tags,
            onValueChange = { tags = it },
            label = { Text("Enter Tags (Tag1, Tag2, ...)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Price per night (optional)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = rules,
            onValueChange = { rules = it },
            label = { Text("Rules") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = cancellationPolicy,
            onValueChange = { cancellationPolicy = it },
            label = { Text("Cancellation policy") },
            modifier = Modifier.fillMaxWidth()
        )

        //rule errors from the validator, plus the price check below
        val shownPriceError = priceError
        val allErrors = if (shownPriceError != null) errors + shownPriceError else errors
        for (error in allErrors) {
            Text(
                error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.testTag("errorMessage")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            enabled = !saving,
            modifier = Modifier.fillMaxWidth().testTag("saveButton"),
            onClick = {
                //price is optional, but if they typed something it has to be a number
                val priceNumber = price.trim().toDoubleOrNull()
                if (price.isNotBlank() && priceNumber == null) {
                    priceError = "Price must be a number"
                } else {
                    priceError = null
                    val updated = campsite.copy(
                        name = name.trim(),
                        location = location.trim(),
                        description = description.trim(),
                        //anything that isn't a number becomes 0, which MaxOccupancyRule catches
                        maxOccupancy = maxOccupancy.trim().toIntOrNull() ?: 0,
                        price = priceNumber,
                        tags = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                        rules = rules.trim(),
                        cancellationPolicy = cancellationPolicy.trim()
                    )
                    onSave(updated)
                }
            }
        ) {
            Text(if (saving) "Updating..." else "Update my Campsite!")
        }
    }
}