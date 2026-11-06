package com.example.birchbark.ui.owner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.birchbark.model.Campsite
import com.example.birchbark.model.CampsiteRepository

/**
 * US 02.01.01 Register campsite and US 02.01.02 Provide campsite details. The
 * "Register my Campsite" screens from the Register Campsite mockup. Owner fills
 * in the form, saving stores the campsite in Firebase under that owner, and then
 * it shows Registration Complete or Registration Failed.
 *
 * Uses the same form as the update screen ([EditCampsiteForm]) with a blank campsite.
 *
 * Outstanding issues. The mockup has a separate campsite policies page before
 * registering, for now rules and cancellation policy are just fields on the form.
 * Place your Camp Pin and Upload images aren't here yet, they need the map and
 * image upload.
 */

/**
 * The register form, then the complete or failed screen after saving.
 *
 * @param ownerId the owner's ID, saved on the new campsite so it shows in their list.
 * @param onDone called when the owner taps Back to Home after registering.
 * @param onCancel called if the owner goes back without registering.
 */
@Composable
fun RegisterCampsiteScreen(ownerId: String, onDone: () -> Unit, onCancel: () -> Unit) {
    val repository = remember { CampsiteRepository() }
    var saveErrors by remember { mutableStateOf<List<String>>(listOf()) }
    var saving by remember { mutableStateOf(false) }
    //name of the campsite once it's registered, null until then
    var registeredName by remember { mutableStateOf<String?>(null) }
    var failed by remember { mutableStateOf(false) }

    val name = registeredName

    if (name != null) {
        RegistrationResult(
            title = "Registration Complete for your Campsite!",
            message = "Welcome, $name!",
            onBackToHome = onDone
        )
    } else if (failed) {
        RegistrationResult(
            title = "Registration Failed",
            message = "Please try registering at a later time.",
            onBackToHome = onDone
        )
    } else {
        EditCampsiteForm(
            campsite = Campsite(ownerId = ownerId),
            errors = saveErrors,
            saving = saving,
            title = "Register my Campsite",
            buttonText = "Register my Campsite!",
            savingText = "Registering...",
            onSave = { newCampsite ->
                saving = true
                repository.createCampsite(newCampsite, object : CampsiteRepository.SaveCallback {
                    override fun onSuccess(campsiteId: String) {
                        saving = false
                        registeredName = newCampsite.name
                    }

                    override fun onInvalid(errors: List<String>) {
                        //missing required fields, stay on the form and show what's wrong
                        saving = false
                        saveErrors = errors
                    }

                    override fun onError(error: Exception) {
                        saving = false
                        failed = true
                    }
                })
            },
            onCancel = onCancel
        )
    }
}

/**
 * The screen shown after registering, either complete or failed.
 *
 * @param title the big heading, like "Registration Failed".
 * @param message the line under it.
 * @param onBackToHome called when the owner taps Back to Home.
 */
@Composable
fun RegistrationResult(title: String, message: String, onBackToHome: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("registrationResult")
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(message, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBackToHome, modifier = Modifier.fillMaxWidth()) {
            Text("Back to Home")
        }
    }
}