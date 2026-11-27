package com.example.birchbark.ui.entrant

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.birchbark.UserProfile
import com.example.birchbark.UserRepository
import com.example.birchbark.model.ProfileDeletionCoordinator
import com.example.birchbark.ui.theme.birchBarkButtonColors
import com.example.birchbark.ui.theme.birchBarkFieldColors
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.firestore.Blob

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onDeleted: () -> Unit = {},
    onProfileUpdated: (UserProfile) -> Unit = {}
) {
    val repository = remember { UserRepository() }
    val deletionCoordinator = remember {
        ProfileDeletionCoordinator()
    }

    var profile by remember {
        mutableStateOf<UserProfile?>(null)
    }
    var loading by remember {
        mutableStateOf(true)
    }
    var saving by remember {
        mutableStateOf(false)
    }
    var deleting by remember {
        mutableStateOf(false)
    }
    var message by remember {
        mutableStateOf("")
    }

    LaunchedEffect(Unit) {
        FirebaseInstallations.getInstance().id
            .addOnSuccessListener { fid ->
                repository.getProfile(
                    fid = fid,
                    onResult = { result ->
                        profile = result?.copy(fid = fid)
                        loading = false

                        if (result == null) {
                            message = "No profile found for this device."
                        }
                    },
                    onFailure = { error ->
                        loading = false
                        message = error.message
                            ?: "Unable to load profile."
                    }
                )
            }
            .addOnFailureListener { error ->
                loading = false
                message = error.message
                    ?: "Unable to identify device."
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(
            onClick = onBack,
            enabled = !saving && !deleting
        ) {
            Text("Back")
        }

        Text(
            text = "Edit Profile",
            style = MaterialTheme.typography.headlineMedium
        )

        when {
            loading -> {
                CircularProgressIndicator()
            }

            profile == null -> {
                Text(message)
            }

            else -> {
                val currentProfile = profile!!

                val photo = remember(currentProfile.profileImage) { currentProfile.profileImage?.toBytes() }

                ProfilePhotoEditor(
                    fid = currentProfile.fid,
                    name = "${currentProfile.firstName} ${currentProfile.lastName}".trim(),
                    imageBytes = photo,
                    onImageChanged = { bytes ->
                        //keep the profile in step with the saved photo, or Save Changes could write the old one back
                        profile?.copy(profileImage = bytes?.let { Blob.fromBytes(it) })?.let {
                            profile = it
                            onProfileUpdated(it)       // the header and menu update from this
                        }
                    }
                )

                EditProfileForm(
                    profile = currentProfile,
                    saving = saving || deleting,
                    message = message,
                    onSave = { edited ->
                        val cleaned = repository.cleanProfile(edited)
                        val error = repository.validateProfile(cleaned)

                        if (error != null) {
                            message = error
                        } else {
                            saving = true
                            message = ""

                            repository.updateProfile(
                                profile = cleaned,
                                onResult = {
                                    profile = cleaned
                                    saving = false
                                    message = "Profile updated successfully."
                                    onProfileUpdated(cleaned)
                                },
                                onFailure = { exception ->
                                    saving = false
                                    message = exception.message
                                        ?: "Unable to save profile."
                                }
                            )
                        }
                    }
                )

                HorizontalDivider()

                Text(
                    text = "Delete Account",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Deleting your profile also removes you " +
                            "from your lottery waitlists."
                )

                DeleteProfileSection(
                    working = saving || deleting,
                    onConfirmDelete = {
                        deleting = true
                        message = "Deleting profile..."

                        deletionCoordinator.deleteProfile(
                            profile = currentProfile,
                            onSuccess = {
                                deleting = false
                                profile = null
                                onDeleted()
                            },
                            onFailure = { error ->
                                deleting = false
                                message = "Deletion failed: " +
                                        (error.localizedMessage
                                            ?: "Unknown error") +
                                        ". You can retry. Some waitlist " +
                                        "memberships may already have " +
                                        "been removed."
                            }
                        )
                    }
                )

                if (deleting) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun EditProfileForm(
    profile: UserProfile,
    saving: Boolean,
    message: String,
    onSave: (UserProfile) -> Unit
) {
    var firstName by remember(profile.firstName) {
        mutableStateOf(profile.firstName)
    }

    var lastName by remember(profile.lastName) {
        mutableStateOf(profile.lastName)
    }

    var email by remember(profile.email) {
        mutableStateOf(profile.email)
    }

    var phoneNumber by remember(profile.phoneNumber) {
        mutableStateOf(profile.phoneNumber)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") },
            enabled = !saving,
            singleLine = true,
            colors = birchBarkFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("firstNameField")
        )

        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name" ) },
            enabled = !saving,
            singleLine = true,
            colors = birchBarkFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("lastNameField")
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            enabled = !saving,
            singleLine = true,
            colors = birchBarkFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("emailField")
        )

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number") },
            enabled = !saving,
            singleLine = true,
            colors = birchBarkFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("phoneField")
        )

        Button(
            onClick = {
                onSave(
                    profile.copy(
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        phoneNumber = phoneNumber
                    )
                )
            },
            enabled = !saving,
            colors = birchBarkButtonColors(),
            modifier = Modifier.testTag("saveProfileButton")
        ) {
            Text(
                if (saving) "Saving..." else "Save Changes"
            )
        }
        if (message.isNotBlank()) {
            Text(
                text = message,
                modifier = Modifier.testTag("profileMessage")
            )
        }
    }
}
