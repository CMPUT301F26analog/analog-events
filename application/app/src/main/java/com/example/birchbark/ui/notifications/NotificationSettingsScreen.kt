
package com.example.birchbark.ui.notifications

import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.birchbark.model.NotificationSettingsRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
/**
 * Notification Settings Screen — Issue #23 (US 01.10.01).
 *
 * Allows authenticated users to enable or disable notifications.
 * Loads saved preferences from Firebase Firestore and saves changes
 * through NotificationSettingsRepository.
 *
 * Handles loading, saving, and error states, and restores the
 * previous preference if saving fails.
 */
@Composable
fun NotificationSettingsScreen() {
    val uid = Firebase.auth.currentUser?.uid
    val repository = remember { NotificationSettingsRepository() }

    var notificationsEnabled by remember { mutableStateOf(true) }
    var loading by remember { mutableStateOf(true) }
    var loaded by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    LaunchedEffect(saving){
        if(saving){
            delay(3000)
            if(saving){
                message = "Still syncing. Check your internet connection."
            }
        }
    }

    LaunchedEffect(uid) {
        loading = true
        loaded = false

        if (uid == null) {
            loading = false
            message = "Please sign in first."
        } else {
            repository.loadPreference(
                uid = uid,
                onSuccess = { savedValue ->
                    notificationsEnabled = savedValue
                    loading = false
                    loaded = true
                },
                onFailure = { error ->
                    loading = false
                    message = "Loading failed: ${error.message}"
                }
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Notification Settings")

        if (loading) {
            Text("Loading preferences...")
        }

        NotificationSettingsSection(
            enabled = notificationsEnabled,
            interactive = loaded && !saving,
            onEnabledChange = { newValue ->
                if (uid != null && loaded && !saving) {
                    val oldValue = notificationsEnabled

                    notificationsEnabled = newValue
                    saving = true
                    message = "Saving..."

                    repository.savePreference(
                        uid = uid,
                        enabled = newValue,
                        onSuccess = {
                            saving = false
                            message = "Preference saved!"
                        },
                        onFailure = { error ->
                            notificationsEnabled = oldValue
                            saving = false
                            message = "Save failed: ${error.message}"
                        }
                    )
                }
            }
        )

        Text(message)
    }
}
