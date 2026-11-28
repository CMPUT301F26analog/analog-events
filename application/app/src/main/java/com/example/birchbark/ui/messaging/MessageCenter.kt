package com.example.birchbark.ui.messaging

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.text.format.DateUtils
import com.example.birchbark.model.*
import com.example.birchbark.ui.theme.BirchBarkColors
import com.example.birchbark.util.datesLabelOrNull
import kotlinx.coroutines.delay
import java.text.DateFormat
import java.util.Date

/**
 * Destinations within the participant Message Center. SENT follows a confirmed write.
 *
 * We try not to mix data classes with composoable or model files. However, setting this as
 * a private ENUM class only makes this accessable to THIS messagecenter.
 *
 **/
private enum class Page { INBOX, COMPOSE, THREAD, SENT }

/**
 * US 01.14.01 and US 02.12.01.
 *
 * Shared Compose route for the inbox, recipient picker and replies.
 * Listens to the selected booking so an open conversation becomes read-only when sending closes.
 *
 * @Note See #[RW-2-LLM] in LLM.md
 *
 * @param fid installation FID of the registered participant.
 * @param side role selected in navigation.
 * @param initialReservationId booking to open directly, or null to start in the inbox.
 * @param onBack returns to the calling role screen.
 * @param onNotifications opens the owner's notification destination when supplied.
 * @param repository provides bookings, live history, sends and read receipts.
 */
@Composable
fun MessageCenter(
    fid: String, side: MessageSide, initialReservationId: String? = null,
    onBack: () -> Unit, onNotifications: (() -> Unit)? = null,
    repository: ReservationMessaging = remember { FirestoreMessageRepository() }
) {
    var page by rememberSaveable { mutableStateOf(if (initialReservationId == null) Page.INBOX else Page.THREAD) }
    var id by rememberSaveable { mutableStateOf(initialReservationId) }
    var siteId by rememberSaveable { mutableStateOf<String?>(null) }
    var draft by rememberSaveable { mutableStateOf("") }
    var inbox by remember { mutableStateOf<List<ConversationSummary>?>(null) }
    var choices by remember { mutableStateOf<List<MessageBooking>?>(null) }
    var booking by remember(id) { mutableStateOf<Reservation?>(null) }
    var bookingLoaded by remember(id) { mutableStateOf(false) }
    var history by remember(id) { mutableStateOf<List<ReservationMessage>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var attempt by rememberSaveable { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var retry by remember { mutableIntStateOf(0) }
    val selected = id
    val summary = inbox?.find { it.id == selected }
    val choice = choices?.find { it.id == selected }
    val failed: MessageFailure = { error = it.localizedMessage ?: "Unable to load messages." }

    DisposableEffect(fid, side, retry) {
        val sub = repository.inbox(fid, side, { inbox = it }, failed)
        onDispose { sub.cancel() }
    }
    DisposableEffect(fid, side, page, retry) {
        var current = true
        if (page == Page.COMPOSE || page == Page.THREAD) {
            repository.bookings(fid, side, { if (current) choices = it }, { if (current) failed(it) })
        }
        onDispose { current = false }
    }
    DisposableEffect(selected, retry) {
        val sub = selected?.let { key ->
            repository.booking(key, { booking = it; bookingLoaded = true }, {
                // A deleted booking can no longer authorize sending. The archived thread remains readable.
                booking = null
                bookingLoaded = true
                failed(it)
            })
        }
        onDispose { sub?.cancel() }
    }
    DisposableEffect(selected, summary != null, page, retry) {
        val sub = if (selected != null && summary != null && page == Page.THREAD)
            repository.history(selected, { history = it }, failed) else null
        onDispose { sub?.cancel() }
    }
    LaunchedEffect(selected, summary?.lastId, history.lastOrNull()?.id, page, retry) {
        if (page == Page.THREAD && summary != null && summary.unread(fid) &&
            summary.lastId == history.lastOrNull()?.id) {
            repository.markSeen(summary.id, side, summary.lastId, failed)
        }
    }
    // Refresh eligibility at the exclusive checkout instant, including in an already-open composer.
    val now by produceState(System.currentTimeMillis(), booking, choices) {
        while (true) {
            value = System.currentTimeMillis()
            val next = (choices.orEmpty().mapNotNull { it.reservation.endDate } + listOfNotNull(booking?.endDate))
                .filter { it > value }.minOrNull() ?: break
            delay((next - value).coerceAtLeast(1L))
        }
    }
    fun back() {
        if (busy) return
        if (page == Page.INBOX || initialReservationId != null) onBack()
        else {
            page = Page.INBOX
            id = null
            siteId = null
            draft = ""
            error = null
        }
    }
    BackHandler { back() }

    Column(Modifier.fillMaxSize().background(BirchBarkColors.DarkGreen).padding(12.dp)) {
        TextButton(onClick = { back() }, enabled = !busy) { Text("← Back") }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Message Center", Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall,
                color = BirchBarkColors.Cream)
            TextButton(enabled = page != Page.COMPOSE && !busy, onClick = {
                id = null; siteId = null; draft = ""; error = null; page = Page.COMPOSE
            }) { Text("✎ New") }
        }
        Row(Modifier.weight(1f)) {
            if (side == MessageSide.OWNER) {
                Column(Modifier.width(82.dp)) {
                    TextButton(onClick = { page = Page.INBOX; id = null; error = null; draft = "" },
                        enabled = !busy) { Text("Messages", style = MaterialTheme.typography.labelSmall) }
                    TextButton(enabled = onNotifications != null && !busy, onClick = { onNotifications?.invoke() }) {
                        Text("Notifications", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Column(Modifier.fillMaxSize().then(if (page == Page.COMPOSE) Modifier.verticalScroll(rememberScrollState()) else Modifier), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                when (page) {
                    Page.INBOX -> Inbox(inbox, side, fid) {
                        id = it; page = Page.THREAD; draft = ""; error = null
                    }
                    Page.SENT -> {
                        Text("MESSAGE SENT!", color = BirchBarkColors.Teal)
                        TextButton(onClick = { page = Page.THREAD }) { Text("View conversation") }
                    }
                    Page.COMPOSE, Page.THREAD -> {
                        val composing = page == Page.COMPOSE
                        if (composing) {
                            val active = choices.orEmpty().filter { it.reservation.acceptsMessages(now) }
                            if (choices == null && error == null) LinearProgressIndicator()
                            val sites = active.distinctBy { it.reservation.campsiteId }
                            Picker(sites.find { it.reservation.campsiteId == siteId }?.siteName ?: "Campsite",
                                sites, { it.siteName }, !busy) { siteId = it.reservation.campsiteId; id = null }
                            Picker(choice?.label(side) ?: if (side == MessageSide.OWNER) "Find entrant" else "Choose booking",
                                active.filter { it.reservation.campsiteId == siteId }, {
                                    "${it.label(side)} · ${it.reservation.datesLabelOrNull().orEmpty()}"
                                }, siteId != null && !busy) { id = it.id }
                            Text("Recipient: " + (choice?.label(side) ?: "Select a booking"))
                            if (choices != null && active.isEmpty()) Text("No active confirmed bookings.")
                        } else {
                            Text(summary?.label(side) ?: choice?.label(side) ?: "Reservation conversation",
                                style = MaterialTheme.typography.titleMedium)
                            History(history, fid, summary?.entrantName ?: booking?.entrantName.orEmpty(),
                                summary?.siteName ?: choice?.siteName.orEmpty(), Modifier.weight(1f))
                        }
                        val canSend = booking?.acceptsMessages(now) == true
                        if ((selected == null && composing) || canSend) {
                            OutlinedTextField(draft, { draft = it; attempt = null }, enabled = !busy, maxLines = 4,
                                label = { Text(if (composing) "Message" else "Reply to message") },
                                modifier = Modifier.fillMaxWidth())
                            Button(enabled = !busy, modifier = Modifier.fillMaxWidth(), onClick = {
                                if (selected == null || draft.isBlank()) {
                                    error = "Select a valid recipient and enter a non-empty message."
                                } else {
                                    busy = true
                                    error = null
                                    val messageId = attempt ?: java.util.UUID.randomUUID().toString()
                                    attempt = messageId
                                    repository.send(selected, side, fid, messageId, draft, {
                                        busy = false; attempt = null; draft = ""; page = Page.SENT
                                    }, { busy = false; failed(it) })
                                }
                            }) { Text(if (busy) "Sending…" else if (composing) "Send Message" else "Send Reply") }
                        } else if (!bookingLoaded) {
                            LinearProgressIndicator()
                        } else {
                            Text(when (booking?.status) {
                                ReservationStatus.CANCELLED -> "Read-only · booking cancelled"
                                ReservationStatus.REJECTED -> "Read-only · booking rejected"
                                ReservationStatus.CONFIRMED -> "Read-only · checkout has passed"
                                else -> "Read-only · booking is no longer active"
                            })
                        }
                    }
                }
            }
        }
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            TextButton(onClick = { error = null; retry++ }) { Text("Refresh") }
        }
    }
}

/**
 * Displays [options] using [label] and passes the selected item to [pick].
 **/
@Composable
private fun <T> Picker(title: String, options: List<T>, label: (T) -> String, enabled: Boolean, pick: (T) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(enabled = enabled && options.isNotEmpty(), onClick = { expanded = true }) { Text(title) }
        DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            options.forEach { item ->
                DropdownMenuItem(text = { Text(label(item)) }, onClick = { expanded = false; pick(item) })
            }
        }
    }
}

/**
 * Displays each counterpart, latest preview and receipt for [fid]. Selecting a row passes its
 * reservation ID to [open].
 **/
@Composable
private fun Inbox(rows: List<ConversationSummary>?, side: MessageSide, fid: String, open: (String) -> Unit) {
    when {
        rows == null -> CircularProgressIndicator()
        rows.isEmpty() -> Text("No conversations yet. Choose New to message a confirmed booking.")
        else -> LazyColumn {
            items(rows, key = { it.id }) { row ->
                val name = row.label(side)
                Row(Modifier.fillMaxWidth().clickable { open(row.id) }.padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(32.dp).clip(CircleShape).background(BirchBarkColors.AvatarGreen),
                        contentAlignment = Alignment.Center) { Text(name.take(1).uppercase()) }
                    Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                        Text(name)
                        Text(row.siteName + " · " + DateUtils.getRelativeTimeSpanString(row.sentAt))
                        Text(row.preview, maxLines = 1, overflow = TextOverflow.Ellipsis, color = BirchBarkColors.MutedText)
                    }
                    Text(if (row.unread(fid)) "✉ Unread" else "✉ Read", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

/**
 * Displays messages in chronological order, aligns each entry by sender and scrolls to the latest entry.
 **/
@Composable
private fun History(messages: List<ReservationMessage>, fid: String, entrant: String, site: String, modifier: Modifier) {
    val list = rememberLazyListState()
    LaunchedEffect(messages.lastOrNull()?.id) { if (messages.isNotEmpty()) list.scrollToItem(messages.lastIndex) }
    LazyColumn(modifier, state = list, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(messages, key = { it.id }) { m ->
            Column(Modifier.fillMaxWidth(), horizontalAlignment = if (m.sender == fid) Alignment.End else Alignment.Start) {
                Text(if (m.ownerSent) "$site management" else entrant, style = MaterialTheme.typography.labelLarge)
                Text(DateFormat.getDateTimeInstance().format(Date(m.sentAt)), style = MaterialTheme.typography.labelSmall)
                Text(m.body)
            }
        }
    }
}