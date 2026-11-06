package com.example.birchbark.ui.entrant

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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

/**
 * US 01.01.01 Search for campsites. The entrant search page from the Entrant
 * Search mockup. There's a search bar where the entrant types a keyword, and
 * campsites with a matching name or location show up. If nothing matches it
 * shows a not found message.
 *
 * Also works with filters (US 01.01.02, US 01.01.03). The browse screen can pass
 * a filter, like CampsiteFilter.matches with the entrant's filters, and only
 * campsites that match both the search and the filter show up. It can also pass
 * onFilterClick to show a Filters button that opens the filter screen.
 *
 * SearchCampsitesScreen loads every campsite from Firestore, and
 * SearchCampsitesContent does the searching and drawing so the intent tests
 * can give it fake campsites. Tapping a result calls onCampsiteClick, so the
 * entrant details screen (US 01.01.05) can be opened from here.
 *
 * Outstanding issues. Recommended campsites and recommended searches from the
 * mockup aren't here yet. The result cards are plain for now and can be swapped
 * for the campsite summary card (US 01.01.04).
 */

/**
 * Loads every campsite and shows the search page.
 *
 * @param onCampsiteClick called with the campsite the entrant tapped.
 * @param filter extra check a campsite has to pass to show up, everything passes by default.
 * @param onFilterClick called when the entrant taps Filters, or null to hide the button.
 */
@Composable
fun SearchCampsitesScreen(
    onCampsiteClick: (Campsite) -> Unit,
    filter: (Campsite) -> Boolean = { true },
    onFilterClick: (() -> Unit)? = null
) {
    val repository = remember { CampsiteRepository() }
    var allCampsites by remember { mutableStateOf<List<Campsite>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    //load once when the screen opens
    LaunchedEffect(Unit) {
        repository.getAllCampsites(object : CampsiteRepository.ListCallback {
            override fun onLoaded(campsites: List<Campsite>) {
                allCampsites = campsites
            }

            override fun onError(error: Exception) {
                errorMessage = "Couldn't load campsites, check your connection and try again"
            }
        })
    }

    val list = allCampsites
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
        SearchCampsitesContent(
            campsites = list,
            onCampsiteClick = onCampsiteClick,
            filter = filter,
            onFilterClick = onFilterClick
        )
    }
}

/**
 * The search bar and results. The results update right away as the entrant
 * types, using [Campsite.matchesSearch] and the filter.
 *
 * @param campsites every campsite to search through.
 * @param onCampsiteClick called with the campsite the entrant tapped.
 * @param filter extra check a campsite has to pass to show up, everything passes by default.
 * @param onFilterClick called when the entrant taps Filters, or null to hide the button.
 */
@Composable
fun SearchCampsitesContent(
    campsites: List<Campsite>,
    onCampsiteClick: (Campsite) -> Unit,
    filter: (Campsite) -> Boolean = { true },
    onFilterClick: (() -> Unit)? = null
) {
    var keyword by remember { mutableStateOf("") }
    //has to match the search and pass the filter
    val results = campsites.filter { it.matchesSearch(keyword) && filter(it) }

    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = keyword,
                onValueChange = { keyword = it },
                placeholder = { Text("Search your campsite") },
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("searchField")
            )
            //clear button only shows once they've typed something
            if (keyword.isNotEmpty()) {
                TextButton(onClick = { keyword = "" }) { Text("Clear") }
            }
        }
        //filters button only shows if the screen using this gave it somewhere to go
        if (onFilterClick != null) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onFilterClick,
                modifier = Modifier.testTag("filterButton")
            ) {
                Text("Filters")
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (results.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().testTag("notFound"),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Site not found", style = MaterialTheme.typography.headlineSmall)
                Text("Try search with a different keyword")
            }
        } else {
            val countText = if (results.size == 1) "Found 1 campsite" else "Found ${results.size} campsites"
            Text(countText, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(results) { campsite ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCampsiteClick(campsite) }
                            .testTag("searchResult")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(campsite.name, style = MaterialTheme.typography.titleMedium)
                            Text(campsite.location, style = MaterialTheme.typography.bodyMedium)
                            Text("Up to ${campsite.maxOccupancy} guests", style = MaterialTheme.typography.bodySmall)
                            val rating = campsite.averageRating()
                            Text(
                                if (rating == null) "No ratings" else "Rating " + String.format("%.1f", rating),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}