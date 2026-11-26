package com.example.birchbark

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.birchbark.model.AppNotification
import com.example.birchbark.model.NotificationLogs
import com.example.birchbark.model.NotificationRepository
import com.example.birchbark.util.Time

/**
 * US 03.08.01 Review notification logs. The admin's Logs tab. Shows every
 * notification sent in the app, newest first, with who sent it, who got it,
 * the message and the date.
 *
 * AdminNotificationLogsScreen loads from Firestore, and
 * AdminNotificationLogsContent just draws so the intent tests can use fake
 * notifications.
 *
 * Outstanding issues. It loads every notification at once, which is fine for
 * now but would be slow with a lot of them.
 */

/**
 * Loads every notification and every user's name, then shows the log.
 */
@Composable
fun AdminNotificationLogsScreen() {
    val repository = remember { NotificationRepository() }
    //the same live list of profiles the admin profiles screen uses, for showing names
    val userRepository = remember { UserRepository() }
    var notifications by remember { mutableStateOf<List<AppNotification>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        repository.getAll(
            onResult = { found -> notifications = found },
            onFailure = { errorMessage = "Couldn't load the notification logs, check your connection and try again" }
        )
    }

    DisposableEffect(Unit) {
        userRepository.startListening()
        onDispose { userRepository.stopListening() }
    }

    //FID to "first last", rebuilt whenever the profiles list changes
    val names = userRepository.profiles.associate { profile ->
        profile.fid to listOf(profile.firstName, profile.lastName).joinToString(" ").trim()
    }

    val list = notifications
    val error = errorMessage
    if (error != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(error, color = LogCream, modifier = Modifier.padding(16.dp))
        }
    } else if (list == null) {
        //still loading
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = LogGold)
        }
    } else {
        AdminNotificationLogsContent(notifications = list, names = names)
    }
}

/**
 * Draws the notification log as cards, newest first.
 *
 * @param notifications every notification to show.
 * @param names every user's name by FID, so the log shows names instead of IDs.
 */
@Composable
fun AdminNotificationLogsContent(notifications: List<AppNotification>, names: Map<String, String>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LogBackground)
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "Notification Logs",
            color = LogCream,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.testTag("notificationLogsTitle")
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (notifications.isEmpty()) {
            Text(
                "No notifications have been sent yet",
                color = LogMuted,
                modifier = Modifier.testTag("noNotificationLogs")
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(NotificationLogs.newestFirst(notifications)) { notification ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LogCard, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                            .testTag("notificationLog")
                    ) {
                        Text(
                            NotificationLogs.typeLabel(notification.type) + " · " +
                                    Time.format(Time.toLocalDate(notification.createdAt)),
                            color = LogGold,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            "From " + NotificationLogs.nameFor(notification.senderId, names) +
                                    " to " + NotificationLogs.nameFor(notification.recipientId, names),
                            color = LogCream,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(notification.title, color = LogCream, style = MaterialTheme.typography.titleSmall)
                        Text(notification.body, color = LogMuted, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

//same colours as the other admin screens
private val LogBackground = Color(0xFF1B3232)
private val LogCard = Color(0xFF244746)
private val LogGold = Color(0xFFD6A266)
private val LogCream = Color(0xFFF5EADD)
private val LogMuted = Color(0xFFB8C3BE)