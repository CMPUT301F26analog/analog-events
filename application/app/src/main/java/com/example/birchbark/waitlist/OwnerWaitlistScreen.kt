package com.example.birchbark.waitlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.birchbark.model.Lottery
import com.example.birchbark.model.LotteryRepository
import com.example.birchbark.model.NotificationRepository
import com.example.birchbark.model.OwnerWaitlistRepository
import com.example.birchbark.model.WaitlistMessage
import com.example.birchbark.util.Time

/**
 * US 02.09.01: the owner's live view of who joined their lottery's waitlist.
 *
 * US 02.10.01 (added by Yash): under the list, the owner can write a message
 * and send it to everyone on the waitlist. Entrants who turned notifications
 * off (US 01.10.01) are skipped, and the messages are saved in Firestore.
 *
 * @param lotteryId the lottery whose waitlist is shown.
 * @param onBack called when the owner taps Back.
 */
@Composable
fun OwnerWaitlistScreen(
    lotteryId: String,
    onBack: () -> Unit
) {
    val repository = remember { OwnerWaitlistRepository() }
    val notificationRepository = remember { NotificationRepository() }

    var entrantIds by remember(lotteryId) {
        mutableStateOf<List<String>>(emptyList())
    }
    var names by remember(lotteryId) {
        mutableStateOf<Map<String, String>>(emptyMap())
    }
    var loading by remember(lotteryId) {
        mutableStateOf(true)
    }
    var error by remember(lotteryId) {
        mutableStateOf<String?>(null)
    }

    //US 02.10.01: the lottery (for its owner), and the message being written
    var waitlistLottery by remember(lotteryId) { mutableStateOf<Lottery?>(null) }
    var message by remember(lotteryId) { mutableStateOf("") }
    var sending by remember(lotteryId) { mutableStateOf(false) }
    var status by remember(lotteryId) { mutableStateOf<String?>(null) }

    LaunchedEffect(lotteryId) {
        LotteryRepository().getLottery(lotteryId, object : LotteryRepository.LoadCallback {
            override fun onLoaded(lottery: Lottery) {
                waitlistLottery = lottery
            }

            override fun onNotFound() {
                status = "This lottery doesn't exist anymore"
            }

            override fun onError(error: Exception) {
                status = "Couldn't load the lottery, check your connection and try again"
            }
        })
    }

    //US 02.10.01: check the message, then send one notification per entrant
    fun sendMessage() {
        val lottery = waitlistLottery ?: return
        val problem = WaitlistMessage.check(message, entrantIds)
        if (problem != null) {
            status = problem
            return
        }
        val total = entrantIds.distinct().size
        sending = true
        notificationRepository.send(
            WaitlistMessage.build(lottery, entrantIds, message, Time.now()),
            onResult = { sent ->
                sending = false
                message = ""
                status = WaitlistMessage.sentLabel(sent, total)
            },
            onFailure = {
                sending = false
                status = "Couldn't send the message, check your connection and try again"
            }
        )
    }

    DisposableEffect(lotteryId) {
        var active = true
        var generation = 0

        val listener = repository.observeWaitlist(
            lotteryId = lotteryId,
            onResult = { ids ->
                if (active) {
                    generation++
                    val currentGeneration = generation

                    entrantIds = ids
                    names = emptyMap()
                    loading = false
                    error = null

                    ids.forEach { id ->
                        repository.loadEntrantName(
                            userId = id,
                            onResult = { name ->
                                if (active && currentGeneration == generation) {
                                    names = names + (
                                            id to (name ?: "Name unavailable")
                                            )
                                }
                            },
                            onFailure = {
                                if (active && currentGeneration == generation) {
                                    names = names + (id to "Name unavailable")
                                }
                            }
                        )
                    }
                }
            },
            onFailure = { exception ->
                if (active) {
                    loading = false
                    error = exception.message ?: "Unable to load waitlist"
                }
            }
        )

        onDispose {
            active = false
            generation++
            listener.remove()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("Back")
        }

        Text(
            text = "Lottery Waitlist",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.testTag("waitlistTitle")
        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            loading -> CircularProgressIndicator()

            error != null -> Text(
                text = "Error: $error",
                color = MaterialTheme.colorScheme.error
            )

            entrantIds.isEmpty() -> Text(
                text = "No entrants have joined this lottery.",
                modifier = Modifier.testTag("emptyWaitlist")
            )

            else -> {
                Text(
                    text = "${entrantIds.size} entrants",
                    modifier = Modifier.testTag("entrantCount")
                )

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(entrantIds, key = { it }) { id ->
                        Text(
                            text = names[id] ?: "Loading name...",
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                        HorizontalDivider()
                    }
                }

                WaitlistMessageSection(
                    message = message,
                    onMessageChange = {
                        message = it
                        status = null
                    },
                    canSend = !sending && waitlistLottery != null,
                    status = status,
                    onSend = ::sendMessage
                )
            }
        }
    }
}

/**
 * US 02.10.01: the box for writing a message to the waitlist, with its Send
 * button. Has no logic of its own, so UI tests can drive it without Firestore.
 *
 * @param message what the owner has typed so far.
 * @param onMessageChange called with the new text as the owner types.
 * @param canSend false while sending, or before the lottery has loaded.
 * @param status what happened after the last Send, if anything.
 * @param onSend called when the owner taps Send to Waitlist.
 */
@Composable
fun WaitlistMessageSection(
    message: String,
    onMessageChange: (String) -> Unit,
    canSend: Boolean,
    status: String?,
    onSend: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Message the waitlist", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = message,
            onValueChange = onMessageChange,
            label = { Text("Message") },
            modifier = Modifier.fillMaxWidth().testTag("waitlistMessageField")
        )
        Button(
            onClick = onSend,
            enabled = canSend && message.isNotBlank(),
            modifier = Modifier.fillMaxWidth().testTag("sendWaitlistMessageButton")
        ) {
            Text("Send to Waitlist")
        }
        status?.let {
            Text(it, modifier = Modifier.testTag("waitlistMessageStatus"))
        }
    }
}