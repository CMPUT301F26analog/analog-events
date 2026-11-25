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
import androidx.compose.material3.OutlinedButton
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
import com.example.birchbark.model.LotteryInvite
import com.example.birchbark.model.LotteryInviteRepository
import com.example.birchbark.util.Time

/**
 * US 02.04.08 Multi-owner lottery. The owner's lottery invites, from other
 * owners who want one of their campsites in their lottery. Opens from the
 * Lottery Invites button on Your Campsites. Accept puts the campsite in the
 * lottery, so entrants see it there, decline doesn't.
 *
 * LotteryInvitesScreen loads and answers, LotteryInvitesContent just draws so
 * the intent tests can use fake invites.
 *
 * Outstanding issues. A reservation from a shared lottery is listed under the
 * owner who made the lottery until the winner picks a campsite (US 01.04.02).
 */

/**
 * Loads this owner's pending invites and lets them accept or decline each one.
 *
 * @param ownerId the owner whose campsites were invited.
 * @param onBack called when the owner taps Back.
 */
@Composable
fun LotteryInvitesScreen(ownerId: String, onBack: () -> Unit) {
    val repository = remember { LotteryInviteRepository() }
    var invites by remember { mutableStateOf<List<LotteryInvite>?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(ownerId) {
        repository.getPendingInvites(ownerId, object : LotteryInviteRepository.ListCallback {
            override fun onLoaded(invites: List<LotteryInvite>) {
                setInvites(invites)
            }

            override fun onError(error: Exception) {
                setInvites(listOf())
                message = "Couldn't load your invites, check your connection and try again"
            }

            //the parameter is also called invites, so set the screen's one through this
            fun setInvites(found: List<LotteryInvite>) {
                invites = found
            }
        })
    }

    //once an invite is answered it leaves the list
    fun answered(invite: LotteryInvite): LotteryInviteRepository.DoneCallback {
        return object : LotteryInviteRepository.DoneCallback {
            override fun onDone() {
                invites = invites?.filter { it.id != invite.id }
                message = null
            }

            override fun onError(message: String) {
                showMessage(message)
            }

            fun showMessage(text: String) {
                message = text
            }
        }
    }

    val list = invites
    if (list == null) {
        //still loading
        Box(
            modifier = Modifier.fillMaxSize().safeDrawingPadding(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LotteryInvitesContent(
            invites = list,
            message = message,
            onAccept = { invite -> repository.acceptInvite(invite, answered(invite)) },
            onDecline = { invite -> repository.declineInvite(invite, answered(invite)) },
            onBack = onBack
        )
    }
}

/**
 * Draws each pending invite with the campsite name, the lottery's registration
 * dates, and Accept and Decline buttons.
 *
 * @param invites the invites to show.
 * @param message an error to show, or null.
 * @param onAccept called with the invite when the owner taps Accept.
 * @param onDecline called with the invite when the owner taps Decline.
 * @param onBack called when the owner taps Back.
 */
@Composable
fun LotteryInvitesContent(
    invites: List<LotteryInvite>,
    message: String?,
    onAccept: (LotteryInvite) -> Unit,
    onDecline: (LotteryInvite) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp)) {
        TextButton(onClick = onBack) { Text("Back") }
        Text("Lottery Invites", style = MaterialTheme.typography.headlineSmall)
        if (message != null) {
            Text(
                message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.testTag("invitesMessage")
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (invites.isEmpty()) {
            Text("No lottery invites right now", modifier = Modifier.testTag("noInvites"))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(invites) { invite ->
                    Card(modifier = Modifier.fillMaxWidth().testTag("inviteCard")) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                            Text(
                                invite.campsiteName + " was invited to a lottery",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Registration " + Time.format(Time.toLocalDate(invite.registrationStart)) +
                                        " to " + Time.format(Time.lastDayOf(invite.registrationEnd)),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onAccept(invite) },
                                    modifier = Modifier.testTag("acceptInvite_" + invite.id)
                                ) {
                                    Text("Accept")
                                }
                                OutlinedButton(
                                    onClick = { onDecline(invite) },
                                    modifier = Modifier.testTag("declineInvite_" + invite.id)
                                ) {
                                    Text("Decline")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}