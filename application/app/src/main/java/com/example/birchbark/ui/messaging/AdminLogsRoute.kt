package com.example.birchbark.ui.messaging

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.birchbark.AdminNotificationLogsScreen
import com.example.birchbark.UserRepository
import com.example.birchbark.model.*
import java.text.DateFormat
import java.util.Date

/**
 * US 03.12.01
 *
 * See #[RW-2-LLM] in LLM.md
 *
 * Switches between the existing notification log and the owner-message moderation log.
 *
 **/
@Composable
fun AdminLogsRoute() {
    var messages by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        Row {
            TextButton(onClick = { messages = false }, enabled = messages) { Text("Notifications") }
            TextButton(onClick = { messages = true }, enabled = !messages) { Text("Owner messages") }
        }
        if (messages) AdminMessageLogsScreen() else AdminNotificationLogsScreen()
    }
}

/**
 * Displays owner-authored messages newest first with sender, recipient, date and text.
 * Uses profile names when available and installation FIDs when a profile cannot supply a name.
 *
 * @param reader read-only source of owner-to-entrant messages.
 */
@Composable
fun AdminMessageLogsScreen(reader: OwnerMessageLogReader = remember { FirestoreMessageRepository() }) {
    val users = remember { UserRepository() }
    var messages by remember { mutableStateOf<List<ReservationMessage>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var retry by remember { mutableIntStateOf(0) }
    DisposableEffect(users) {
        users.startListening()
        onDispose { users.stopListening() }
    }
    DisposableEffect(reader, retry) {
        val stream = reader.ownerLog({ messages = it },
            { error = it.localizedMessage ?: "Unable to load messages." })
        onDispose { stream.cancel() }
    }
    val names = users.profiles.associate { it.fid to "${it.firstName} ${it.lastName}".trim() }
    val rows = messages
    when {
        error != null -> Column {
            Text(error.orEmpty())
            TextButton(onClick = { error = null; retry++ }) { Text("Retry") }
        }
        rows == null -> CircularProgressIndicator()
        rows.isEmpty() -> Text("No owner messages yet.")
        else -> LazyColumn(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(rows) { m ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("From " + (names[m.sender]?.takeIf(String::isNotBlank) ?: m.sender))
                        Text("To " + (names[m.recipient]?.takeIf(String::isNotBlank) ?: m.recipient))
                        Text(DateFormat.getDateTimeInstance().format(Date(m.sentAt)))
                        Text(m.body)
                    }
                }
            }
        }
    }
}