package com.example.birchbark

import android.os.Bundle
import android.content.Intent
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import com.example.birchbark.model.CampsiteQrResolver
import com.example.birchbark.model.RepositoryCampsiteQrLookup
import com.example.birchbark.ui.qr.CampsiteQrRoute
import com.example.birchbark.ui.entrant.EntrantCampsiteDetailsRoute
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
import androidx.compose.ui.unit.dp
import com.example.birchbark.ui.theme.BirchBarkTheme
import com.example.birchbark.ui.owner.OwnerCampsitesApp
import com.example.birchbark.ui.entrant.EditProfileScreen
import androidx.compose.foundation.layout.statusBarsPadding
import com.example.birchbark.ui.entrant.EntrantApp



// Set this flag to disable firestore access or enable temporary database functionality
private const val ENABLE_FIREBASE_TEST = false

/**
 * Main activity; US 01.12.01 receives external QR deep links without bypassing
 * authentication, registration or role selection. Internal scans use the same state.
 */
class MainActivity : ComponentActivity() {
    private var pendingQrPayload by mutableStateOf<String?>(null)

    /** @param savedInstanceState previously saved request, if the activity is recreated. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingQrPayload = if (savedInstanceState != null) {
            savedInstanceState.getString("pendingQrPayload")
        } else qrPayload(intent)
        enableEdgeToEdge()
        setContent {
            BirchBarkTheme {
                BirchBarkRoot(
                    pendingQrPayload = pendingQrPayload,
                    onQrPayload = { pendingQrPayload = it },
                    onQrClosed = { pendingQrPayload = null }
                )
            }
        }
    }

    /** @param intent new external URI delivered to the existing activity. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        qrPayload(intent)?.let { pendingQrPayload = it }
    }

    /** @param outState stores the unconsumed request across configuration changes. */
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("pendingQrPayload", pendingQrPayload)
        super.onSaveInstanceState(outState)
    }

    /**
     * @param intent potentially untrusted launch intent.
     * @return raw external QR payload for validation after login, or null for ordinary startup.
     */
    private fun qrPayload(intent: Intent?): String? =
        intent?.takeIf { it.action == Intent.ACTION_VIEW }?.dataString
}

private sealed interface Startup {
    data object Loading : Startup
    data class Register(val fid: String, val authUid: String) : Startup
    data class Ready(val profile: UserProfile) : Startup
    data class Error(val message: String) : Startup
}

/**
 * @param pendingQrPayload pending internal scan or external link, retained through startup.
 * @param onQrPayload receives a new internal scan.
 * @param onQrClosed clears the request after dismissal or navigation back.
 */
@Composable
private fun BirchBarkRoot(
    pendingQrPayload: String?,
    onQrPayload: (String) -> Unit,
    onQrClosed: () -> Unit
) {
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
            onProfileDeleted = { onQrClosed(); restart++ },
            pendingQrPayload = pendingQrPayload,
            onQrPayload = onQrPayload,
            onQrClosed = onQrClosed
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

/**
 * @param profile authenticated profile.
 * @param onProfileDeleted restarts authentication after deletion.
 * @param pendingQrPayload request to route only after selecting an assigned role.
 * @param onQrPayload receives internal scan data.
 * @param onQrClosed dismisses QR navigation.
 */
@Composable
private fun RoleHome(
    profile: UserProfile,
    onProfileDeleted: () -> Unit,
    pendingQrPayload: String?,
    onQrPayload: (String) -> Unit,
    onQrClosed: () -> Unit
) {
    val roles = profile.roles.map { it.lowercase() }.distinct()
    // If admin is a role, it is the default homescreen when entering the app
    val firstRole = when {
        "admin" in roles -> "admin"
        roles.size == 1 -> roles.first()
        else -> null
    }
    var activeRole by rememberSaveable(profile.fid, roles) { mutableStateOf(firstRole) }
    val qrResolver = remember(profile.fid) { CampsiteQrResolver(RepositoryCampsiteQrLookup()) }
    val entrantStateHolder = rememberSaveableStateHolder()
    var viewingProfile by remember(profile.fid) {
        mutableStateOf(false)
    }
    val selected = activeRole?.takeIf { it in roles }
    var adminRefresh by remember { mutableIntStateOf(0) }

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
    } else Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {

        if (selected == "owner") {  // selected == 'entrant'
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
                TextButton(onClick = { onQrClosed(); activeRole = null }) { Text("Switch role") }
            }
        }
        Box(modifier = Modifier.weight(1f)) {
            key(selected) {
                when (selected) {
                    "entrant" -> {
                        if (pendingQrPayload != null) {
                            CampsiteQrRoute(
                                payload = pendingQrPayload,
                                activeRole = selected,
                                resolver = qrResolver,
                                onClose = onQrClosed,
                                entrantDetails = { campsite, close ->
                                    EntrantCampsiteDetailsRoute(campsite, close)
                                },
                                adminDetails = { _, _ -> }
                            )
                        } else {
                            // Restore the previous entrant tab when the QR route closes.
                            entrantStateHolder.SaveableStateProvider("entrantHome") {
                                EntrantApp(
                                    profile = profile,
                                    onProfileDeleted = onProfileDeleted,
                                    onQrPayload = onQrPayload
                                )
                            }
                        }
                    }
                    "owner" -> OwnerCampsitesApp(ownerId = profile.fid)
                    "admin" -> key(adminRefresh) {
                        AdminBrowseScreen(
                            adminName = listOf(profile.firstName, profile.lastName)
                                .filter { it.isNotBlank() }.joinToString(" ").ifBlank { "Admin" }
                        )
                    }
                    else -> Text("Unrecognized role: $selected")
                }
            }
        }
    }
    // Admin and owner QR outcomes are dialogs; preserve the existing role UI underneath.
    if (pendingQrPayload != null && selected != "entrant") {
        CampsiteQrRoute(
            payload = pendingQrPayload,
            activeRole = selected,
            resolver = qrResolver,
            onClose = onQrClosed,
            entrantDetails = { _, _ -> },
            adminDetails = { campsite, close ->
                AdminScannedCampsiteDetails(campsite, close, onDeleted = {
                    adminRefresh++
                    close()
                })
            }
        )
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