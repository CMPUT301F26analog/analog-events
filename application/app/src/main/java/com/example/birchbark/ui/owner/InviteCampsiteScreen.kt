package com.example.birchbark.ui.owner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.birchbark.model.Campsite
import com.example.birchbark.model.CampsiteRepository
import com.example.birchbark.model.InviteChecker
import com.example.birchbark.model.Lottery
import com.example.birchbark.model.LotteryInviteRepository

/**
 * US 02.04.08 Multi-owner lottery. Lets the owner who made a lottery invite
 * campsites from other owners into it. Opens from the Invite Another Owner
 * button on the campsite details. The other owner has to accept before their
 * campsite joins (see [LotteryInvitesScreen]).
 *
 * InviteCampsiteScreen loads and sends, InviteCampsiteContent just draws so the
 * intent tests can use fake campsites.
 *
 * Outstanding issues. It loads every campsite in the app, which is fine for now
 * but would be slow with a lot of campsites.
 */

/**
 * Loads every campsite and lets the owner invite the ones that belong to other owners.
 *
 * @param lottery the owner's lottery to invite campsites into.
 * @param onBack called when the owner taps Back.
 */
@Composable
fun InviteCampsiteScreen(lottery: Lottery, onBack: () -> Unit) {
    val inviteRepository = remember { LotteryInviteRepository() }
    var allCampsites by remember { mutableStateOf<List<Campsite>?>(null) }
    var sentIds by remember { mutableStateOf(setOf<String>()) }
    var message by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(lottery.id) {
        CampsiteRepository().getAllCampsites(object : CampsiteRepository.ListCallback {
            override fun onLoaded(campsites: List<Campsite>) {
                allCampsites = campsites
            }

            override fun onError(error: Exception) {
                allCampsites = listOf()
                message = "Couldn't load campsites, check your connection and try again"
            }
        })
    }

    val list = allCampsites
    if (list == null) {
        //still loading
        Box(
            modifier = Modifier.fillMaxSize().safeDrawingPadding(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        InviteCampsiteContent(
            campsites = list,
            lottery = lottery,
            sentIds = sentIds,
            message = message,
            onInvite = { campsite ->
                inviteRepository.sendInvite(campsite, lottery, object : LotteryInviteRepository.DoneCallback {
                    override fun onDone() {
                        sentIds = sentIds + campsite.id
                        message = null
                    }

                    override fun onError(message: String) {
                        showMessage(message)
                    }

                    //the parameter is also called message, so set the screen's one through this
                    fun showMessage(text: String) {
                        message = text
                    }
                })
            },
            onBack = onBack
        )
    }
}

/**
 * Draws a search box and every campsite the owner can invite, each with an
 * Invite button. Once one is invited its button turns into "Invite sent".
 *
 * @param campsites every campsite in the app, [InviteChecker] picks which ones show.
 * @param lottery the lottery they're being invited to.
 * @param sentIds campsites already invited on this screen.
 * @param message an error to show, or null.
 * @param onInvite called with the campsite when the owner taps Invite.
 * @param onBack called when the owner taps Back.
 */
@Composable
fun InviteCampsiteContent(
    campsites: List<Campsite>,
    lottery: Lottery,
    sentIds: Set<String>,
    message: String?,
    onInvite: (Campsite) -> Unit,
    onBack: () -> Unit
) {
    var search by remember { mutableStateOf("") }
    val shown = InviteChecker.invitable(campsites, lottery, search)

    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp)) {
        TextButton(onClick = onBack) { Text("Back") }
        Text("Invite Another Owner", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Invite another owner's campsite into your lottery. It only joins once they accept.",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            label = { Text("Search by name or location") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("inviteSearch")
        )
        if (message != null) {
            Text(
                message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.testTag("inviteMessage")
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (shown.isEmpty()) {
            Text("No other owners' campsites to invite", modifier = Modifier.testTag("noInviteCampsites"))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(shown) { site ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(site.name, style = MaterialTheme.typography.titleMedium)
                                Text(site.location, style = MaterialTheme.typography.bodySmall)
                            }
                            val sent = site.id in sentIds
                            Button(
                                onClick = { onInvite(site) },
                                enabled = !sent,
                                modifier = Modifier.testTag("inviteButton_" + site.id)
                            ) {
                                Text(if (sent) "Invite sent" else "Invite")
                            }
                        }
                    }
                }
            }
        }
    }
}