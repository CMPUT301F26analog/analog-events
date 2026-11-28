package com.example.birchbark.ui.owner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.birchbark.model.AppNotification
import com.example.birchbark.model.Campsite
import com.example.birchbark.model.CampsiteRepository
import com.example.birchbark.model.Lottery
import com.example.birchbark.model.LotteryRepository
import com.example.birchbark.model.NotificationRepository
import com.example.birchbark.model.SentNotifications
import com.example.birchbark.util.Time

/**
 * US 02.10.02 View sent notifications. Shows the owner every notification they
 * sent, newest first, with the message, which lottery it went to and the date.
 * Opens from the Sent Notifications button on Your Campsites.
 *
 * SentNotificationsScreen loads from Firestore, and SentNotificationsContent
 * just draws so the intent tests can use fake messages.
 *
 * Outstanding issues. None.
 */

/**
 * Loads everything the owner sent, plus the lotteries and campsite names to
 * say which lottery each message went to.
 *
 * @param ownerId the owner whose sent notifications to show.
 * @param onBack called when the owner taps Back.
 */
@Composable
fun SentNotificationsScreen(ownerId: String, onBack: () -> Unit) {
    var sent by remember { mutableStateOf<List<AppNotification>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var campsiteNames by remember { mutableStateOf(mapOf<String, String>()) }
    //lottery ID to its label, filled in as each lottery loads
    var lotteryLabels by remember { mutableStateOf(mapOf<String, String>()) }

    LaunchedEffect(ownerId) {
        //all campsites, since a shared lottery (US 02.04.08) can have other owners' campsites
        CampsiteRepository().getAllCampsites(object : CampsiteRepository.ListCallback {
            override fun onLoaded(campsites: List<Campsite>) {
                campsiteNames = campsites.associate { it.id to it.name }

                NotificationRepository().getSentBy(
                    ownerId,
                    onResult = { found ->
                        sent = found
                        loadLotteryLabels(found.mapNotNull { it.lotteryId }.distinct())
                    },
                    onFailure = { errorMessage = "Couldn't load your sent notifications, check your connection and try again" }
                )
            }

            override fun onError(error: Exception) {
                errorMessage = "Couldn't load your sent notifications, check your connection and try again"
            }

            //loads each lottery once and saves its label
            fun loadLotteryLabels(lotteryIds: List<String>) {
                val lotteryRepository = LotteryRepository()
                for (id in lotteryIds) {
                    lotteryRepository.getLottery(id, object : LotteryRepository.LoadCallback {
                        override fun onLoaded(lottery: Lottery) {
                            lotteryLabels = lotteryLabels + (id to SentNotifications.lotteryLabel(lottery, campsiteNames))
                        }

                        override fun onNotFound() {
                            lotteryLabels = lotteryLabels + (id to SentNotifications.lotteryLabel(null, campsiteNames))
                        }

                        override fun onError(error: Exception) {
                            lotteryLabels = lotteryLabels + (id to "Couldn't load the lottery")
                        }
                    })
                }
            }
        })
    }

    val list = sent
    val error = errorMessage
    if (error != null) {
        Box(
            modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(error)
        }
    } else if (list == null) {
        //still loading
        Box(
            modifier = Modifier.fillMaxSize().safeDrawingPadding(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        SentNotificationsContent(
            messages = SentNotifications.group(list),
            lotteryLabels = lotteryLabels,
            onBack = onBack
        )
    }
}

/**
 * Draws each sent message as a card, newest first.
 *
 * @param messages the owner's sent messages, from [SentNotifications.group].
 * @param lotteryLabels which lottery each one went to, by lottery ID.
 * @param onBack called when the owner taps Back.
 */
@Composable
fun SentNotificationsContent(
    messages: List<SentNotifications.SentMessage>,
    lotteryLabels: Map<String, String>,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp)) {
        TextButton(onClick = onBack) { Text("Back") }
        Text("Sent Notifications", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))

        if (messages.isEmpty()) {
            Text("You haven't sent any notifications yet", modifier = Modifier.testTag("noSentNotifications"))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(messages) { message ->
                    Card(modifier = Modifier.fillMaxWidth().testTag("sentNotification")) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                            Text(
                                "Sent " + Time.format(Time.toLocalDate(message.createdAt)),
                                style = MaterialTheme.typography.labelMedium
                            )
                            val lotteryId = message.lotteryId
                            Text(
                                if (lotteryId == null) "Not about a lottery" else lotteryLabels[lotteryId] ?: "Loading...",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(message.title, style = MaterialTheme.typography.titleMedium)
                            Text(message.body, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                SentNotifications.recipientsText(message.recipientCount),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}