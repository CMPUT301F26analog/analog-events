package com.example.birchbark.ui.owner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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

/**
 * US 02.03.01 View my campsites. The owner's "Your Campsites" page from the
 * Owner Home mockup. Shows the campsites they registered as cards they can
 * swipe through, and tapping one opens its details.
 *
 * MyCampsitesScreen loads the campsites from Firestore, and MyCampsitesList
 * just draws whatever list it's given. That way the intent tests can give it
 * fake campsites without needing Firestore.
 *
 * Outstanding issues. The cards don't show a picture, rating or lottery status
 * yet like the mockup does, those need image upload, ratings and the lottery
 * link. The empty message doesn't have a register button yet since the
 * register form isn't built. The ownerId comes from the dev login for now
 * until device ID (US 01.09.01) is done.
 */

/**
 * Loads the owner's campsites and shows them.
 *
 * @param ownerId the owner's ID, used to only load the campsites they registered.
 * @param onCampsiteClick called with the campsite the owner tapped.
 */
@Composable
fun MyCampsitesScreen(ownerId: String, onCampsiteClick: (Campsite) -> Unit) {
    val repository = remember { CampsiteRepository() }
    var myCampsites by remember { mutableStateOf<List<Campsite>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    //load every time the screen opens
    LaunchedEffect(ownerId) {
        repository.getCampsitesByOwner(ownerId, object : CampsiteRepository.ListCallback {
            override fun onLoaded(campsites: List<Campsite>) {
                myCampsites = campsites
            }

            override fun onError(error: Exception) {
                errorMessage = "Couldn't load your campsites, check your connection and try again"
            }
        })
    }

    val list = myCampsites
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
        MyCampsitesList(campsites = list, onCampsiteClick = onCampsiteClick)
    }
}

/**
 * Draws the owner's campsites as cards that swipe left and right, like the
 * mockup. Each one shows at least the name and location. If the list is empty
 * it shows a message telling the owner to register one.
 *
 * @param campsites the campsites to show.
 * @param onCampsiteClick called with the campsite the owner tapped.
 */
@Composable
fun MyCampsitesList(campsites: List<Campsite>, onCampsiteClick: (Campsite) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp)) {
        Text("Your Campsites", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        if (campsites.isEmpty()) {
            Text(
                "You haven't registered any campsites yet. Register one to see it here.",
                modifier = Modifier.testTag("emptyMessage")
            )
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(campsites) { campsite ->
                    Card(
                        modifier = Modifier
                            .width(280.dp)
                            .clickable { onCampsiteClick(campsite) }
                            .testTag("campsiteCard")
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(campsite.name, style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(campsite.location, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(16.dp))
                            //button to view campsite details, like the arrow button in the mockup
                            Button(
                                onClick = { onCampsiteClick(campsite) },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("View details")
                            }
                        }
                    }
                }
            }
        }
    }
}