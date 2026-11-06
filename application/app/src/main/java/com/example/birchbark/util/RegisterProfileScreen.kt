package com.example.birchbark

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.birchbark.ui.theme.BirchBarkColors
import com.example.birchbark.ui.theme.birchBarkButtonColors
import com.example.birchbark.ui.theme.birchBarkFieldColors

/**
 * First-time profile registration.
 * Version 0.2
 *
 * No email/phone verification is performed yet.
 *
 * The registration screen will only appear until registration is complete
 * for the specific device. The device is linked to the database through:
 *
 * @param fid Firebase installation id
 * @param authUid Firebase anonymous authentication id
 * @param onRegistered Unit object that is used to transmit data
 *
 * File was created with the assistance of LLM, see LLM.md [014]
s */
@Composable
fun RegisterProfileScreen(
    fid: String,
    authUid: String,
    onRegistered: (UserProfile) -> Unit
) {
    val repo = remember { UserRepository() }
    var fullName by rememberSaveable(fid) { mutableStateOf("") }
    var email by rememberSaveable(fid) { mutableStateOf("") }
    var phoneNumber by rememberSaveable(fid) { mutableStateOf("") }
    var userName by rememberSaveable(fid) { mutableStateOf("") }
    var entrant by rememberSaveable(fid) { mutableStateOf(false) }
    var owner by rememberSaveable(fid) { mutableStateOf(false) }
    var termsVisible by rememberSaveable { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    BackHandler(enabled = termsVisible) { termsVisible = false }
    if (termsVisible) {
        TermsPlaceholder(onBack = { termsVisible = false })
        return
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(BirchBarkColors.DarkGreen)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(190.dp)
                .background(Brush.verticalGradient(listOf(
                    BirchBarkColors.PhotoTop, BirchBarkColors.DarkGreen
                ))),
            contentAlignment = Alignment.BottomStart
        ) {
            Text("Register", style = MaterialTheme.typography.headlineMedium,
                color = BirchBarkColors.Cream, modifier = Modifier.padding(24.dp))
        }

        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = fullName, onValueChange = { fullName = it; errorMessage = null },
                label = { Text("Full name (first and last)") }, singleLine = true,
                colors = birchBarkFieldColors(),
                modifier = Modifier.fillMaxWidth().testTag("registrationFullName")
            )
            OutlinedTextField(
                value = email, onValueChange = { email = it; errorMessage = null },
                label = { Text("Email address") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = birchBarkFieldColors(),
                modifier = Modifier.fillMaxWidth().testTag("registrationEmail")
            )
            OutlinedTextField(
                value = phoneNumber, onValueChange = { phoneNumber = it; errorMessage = null },
                label = { Text("Phone number") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = birchBarkFieldColors(),
                modifier = Modifier.fillMaxWidth().testTag("registrationPhone")
            )
            OutlinedTextField(
                value = userName, onValueChange = { userName = it },
                label = { Text("Screen name (optional)") }, singleLine = true,
                colors = birchBarkFieldColors(), modifier = Modifier.fillMaxWidth()
            )
            Text("Select role(s)", color = BirchBarkColors.Cream)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = entrant, onCheckedChange = { entrant = it })
                Text("Entrant", color = BirchBarkColors.Cream,
                    modifier = Modifier.testTag("registrationEntrant"))
                Spacer(Modifier.padding(horizontal = 16.dp))
                Checkbox(checked = owner, onCheckedChange = { owner = it })
                Text("Owner", color = BirchBarkColors.Cream,
                    modifier = Modifier.testTag("registrationOwner"))
            }
            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("registrationError"))
            }
            Button(
                enabled = !saving,
                onClick = {
                    val parts = fullName.trim().split(Regex("\\s+"))
                        .filter { it.isNotBlank() }
                    if (parts.size < 2) {
                        errorMessage = "Enter a first and last name."
                        return@Button
                    }
                    val roles = buildList {
                        if (entrant) add("entrant")
                        if (owner) add("owner")
                    }
                    val candidate = UserProfile(
                        fid = fid,
                        authUid = authUid,
                        firstName = parts.first(),
                        lastName = parts.drop(1).joinToString(" "),
                        email = email,
                        phoneNumber = phoneNumber,
                        userName = userName,
                        roles = roles,
                        registrationComplete = true
                    )
                    saving = true
                    errorMessage = null
                    repo.registerProfile(candidate,
                        onResult = { saved -> saving = false; onRegistered(saved) },
                        onFailure = { error ->
                            saving = false
                            errorMessage = error.localizedMessage ?: "Registration failed."
                        }
                    )
                },
                colors = birchBarkButtonColors(),
                modifier = Modifier.fillMaxWidth().testTag("registerProfileButton")
            ) {
                Text(if (saving) "Registering..." else "Register")
            }
            // Reading and accepting terms are optional. There is no acceptance checkbox.
            TextButton(onClick = { termsVisible = true },
                modifier = Modifier.fillMaxWidth()) {
                Text("Terms of Service (optional)", color = BirchBarkColors.Gold)
            }
        }
    }
}

@Composable
private fun TermsPlaceholder(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(BirchBarkColors.DarkGreen)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onBack) { Text("Back") }
        Text("Terms of Service", style = MaterialTheme.typography.headlineMedium)
        Text("The approved BirchBark policy will be added in a later user story.")
    }
}