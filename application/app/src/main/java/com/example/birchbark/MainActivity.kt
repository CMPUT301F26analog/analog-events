package com.example.birchbark

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.birchbark.model.Campsite
import com.example.birchbark.ui.entrant.SearchCampsitesScreen
import com.example.birchbark.ui.theme.BirchBarkTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.example.birchbark.ui.notifications.NotificationSettingsScreen
import com.example.birchbark.ui.screens.CampsiteDetailsScreen
import com.example.birchbark.ui.owner.OwnerCampsitesApp
import com.example.birchbark.ui.entrant.EditProfileScreen
import androidx.compose.foundation.layout.statusBarsPadding


// Set this flag to disable firestore access or enable temporary database functionality
private const val ENABLE_FIREBASE_TEST = false

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { BirchBarkTheme { BirchBarkRoot() } }
    }
}

private sealed interface Startup {
    data object Loading : Startup
    data class Register(val fid: String, val authUid: String) : Startup
    data class Ready(val profile: UserProfile) : Startup
    data class Error(val message: String) : Startup
}

@Composable
private fun BirchBarkRoot() {
    val authenticator = remember { Authenticator() }
    var restart by remember { mutableIntStateOf(0) }
    var startup by remember { mutableStateOf<Startup>(Startup.Loading) }

    LaunchedEffect(restart) {
        startup = Startup.Loading
        authenticator.checkForAccount(object : Authenticator.Callback {
            override fun onAccountFound(profile: UserProfile) {
                startup = Startup.Ready(profile)
            }
            override fun onRegistrationRequired(fid: String, authUid: String) {
                startup = Startup.Register(fid, authUid)
            }
            override fun onError(error: Exception) {
                startup = Startup.Error(error.localizedMessage ?: "Unable to load account.")
            }
        })
    }
    when (val state = startup) {
        Startup.Loading -> CenterContent { CircularProgressIndicator() }
        is Startup.Error -> CenterContent {
            Text(state.message)
            Button(onClick = { restart++ }) { Text("Retry") }
        }
        is Startup.Register -> RegisterProfileScreen(
            fid = state.fid, authUid = state.authUid,
            onRegistered = { profile -> startup = Startup.Ready(profile) }
        )
        is Startup.Ready -> RoleHome(
            profile = state.profile,
            onProfileDeleted = { restart++ }
        )
    }
}

@Composable
private fun CenterContent(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) { content() }
}

@Composable
private fun RoleHome(
    profile: UserProfile,
    onProfileDeleted: () -> Unit
) {
    val roles = profile.roles.map { it.lowercase() }.distinct()
    // If admin is a role, it is the default homescreen when enterin the app
    val firstRole = when {
        "admin" in roles -> "admin"
        roles.size == 1 -> roles.first()
        else -> null
    }
    var activeRole by remember(profile.fid, roles) { mutableStateOf(firstRole) }
    var viewingProfile by remember(profile.fid) {
        mutableStateOf(false)
    }
    val selected = activeRole

    if (selected == null) {
        RoleSelectionScreen(
            roles = roles,
            onSelect = { activeRole = it }
        )
        return
    }

    if (viewingProfile) {
        BackHandler { viewingProfile = false }

        EditProfileScreen(
            onBack = { viewingProfile = false },
            onDeleted = onProfileDeleted
        )
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {

        if (selected == "entrant" || selected == "owner") {
            TextButton(
                onClick = { viewingProfile = true }
            ) {
                Text("My Profile")
            }
        }

        if (roles.size > 1) {

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${selected.replaceFirstChar { it.uppercase() }} mode",
                    style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { activeRole = null }) { Text("Switch role") }
            }
        }
        Box(modifier = Modifier.weight(1f)) {
            key(selected) {
                when (selected) {
                    "entrant" -> EntrantHome()
                    "owner" -> OwnerCampsitesApp(ownerId = profile.fid)
                    "admin" -> AdminBrowseScreen(
                        adminName = listOf(profile.firstName, profile.lastName)
                            .filter { it.isNotBlank() }.joinToString(" ").ifBlank { "Admin" }
                    )
                    else -> Text("Unrecognized role: $selected")
                }
            }
        }
    }
}

/**
 * Role selection screen, if a user profile is associated with multiple roles
 * this screen automatically is induced at startup. It is also accessed when a user
 * selects 'switch role.'
 *
 * @param roles string list of roles: [entrant], [owner], [admin]
 * @param onSelect which role is selected as parameter
 */
@Composable
private fun RoleSelectionScreen(roles: List<String>, onSelect: (String) -> Unit) {
    CenterContent {
        Text("Choose your role", style = MaterialTheme.typography.headlineMedium)
        roles.filter { it in setOf("entrant", "owner", "admin") }.forEach { role ->
            Button(onClick = { onSelect(role) }, modifier = Modifier.fillMaxWidth()) {
                Text(role.replaceFirstChar { it.uppercase() })
            }
        }
    }
}

/** Existing entrant browsing screen; tap a campsite to see its existing details screen. */
@Composable
private fun EntrantHome() {
    var selectedCampsite by remember { mutableStateOf<Campsite?>(null) }
    val campsite = selectedCampsite
    if (campsite == null) {
        SearchCampsitesScreen(onCampsiteClick = { selectedCampsite = it })
    } else {
        BackHandler { selectedCampsite = null }
        CampsiteDetailsScreen(
            campsite = campsite,
            lottery = null,
            joined = false,
            isFull = false,
            message = null,
            onBack = { selectedCampsite = null },
            onJoin = {}, // Lottery entry flow is not connected in this phase.
            onLeave = {}
        )
    }
}