package com.example.birchbark.ui.entrant

// import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
// import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
// import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
// import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
// import androidx.compose.material3.MaterialTheme
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
import com.example.birchbark.ui.theme.EmptyState
import com.example.birchbark.ui.theme.BirchBarkColors
import com.example.birchbark.ui.theme.CampsiteCard
import com.example.birchbark.ui.theme.birchBarkFieldColors
import androidx.compose.foundation.BorderStroke

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Surface

/**
 * US 01.01.01 Search for campsites. The entrant search page from the Entrant
 * Search mockup. There's a search bar where the entrant types a keyword, and
 * campsites with a matching name or location show up. If nothing matches it
 * shows a not found message.
 *
 * Also works with filters (US 01.01.02, US 01.01.03). The browse screen can pass
 * a filter, like CampsiteFilter. Matches with the entrant's filters, and only
 * campsites that match both the search and the filter show up. It can also pass
 * onFilterClick to show a Filters button that opens the filter screen.
 *
 * SearchCampsitesScreen loads every campsite from Firestore, and
 * SearchCampsitesContent does the searching and drawing so the intent tests
 * can give it fake campsites. Tapping a result calls onCampsiteClick, so the
 * entrant details screen (US 01.01.05) can be opened from here.
 *
 * Styled like the mockup: a rounded search bar and photo cards from
 * BirchBarkComponents. It no longer adds its own status bar padding, the
 *  entrant shell (EntrantApp) takes care of that.
 *
 * Outstanding issues. Recommended campsites and recommended searches from the
 * mockup aren't here yet. The result cards are plain for now and can be swapped
 * for the campsite summary card (US 01.01.04). The photo is a placeholder until
 * image upload is done.
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
        EmptyState(
            title = "Something went wrong",
            hint = error,
            modifier = Modifier.fillMaxSize()
        )
    } else if (list == null) {
        //still loading
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = BirchBarkColors.Gold)
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

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = keyword,
                onValueChange = { keyword = it },
                placeholder = { Text("Search your campsite") },
                singleLine = true,
                shape = RoundedCornerShape(50),
                colors = birchBarkFieldColors(),
                modifier = Modifier.weight(1f).testTag("searchField")
            )
            //clear button only shows once they've typed something
            if (keyword.isNotEmpty()) {
                TextButton(onClick = { keyword = "" }) { Text("Clear", color = BirchBarkColors.Gold )}
            }
        }
        //filters button only shows if the screen using this gave it somewhere to go
        if (onFilterClick != null) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onFilterClick,
                border = BorderStroke(1.dp, BirchBarkColors.Gold),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BirchBarkColors.Gold),
                modifier = Modifier.testTag("filterButton")
            ) {
                Text("Filters")
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (results.isEmpty()) {
            EmptyState(
                title = "Site not found",
                hint = "Try search with a different keyword",
                modifier = Modifier.fillMaxSize().testTag("notFound")
            )
        } else {
            val countText = if (results.size == 1) "Found 1 campsite" else "Found ${results.size} campsites"
            Text(countText, color = BirchBarkColors.MutedText)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(results) { campsite ->
                    CampsiteCard(
                        campsite = campsite,
                        onClick = { onCampsiteClick(campsite) },
                        modifier = Modifier.testTag("searchResult"))
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF141815) // Adjust hex to match your app's dark background
@Composable
fun SearchCampsitesContentPreview() {
    val sampleCampsites = listOf(
        Campsite(
            id = "1",
            name = "Pine Ridge Overlook",
            location = "Banff National Park, AB"
            // Add any other required fields from your Campsite model here
        ),
        Campsite(
            id = "2",
            name = "Whispering Pines",
            location = "Jasper, AB"
        ),
        Campsite(
            id = "3",
            name = "Crystal Lake Camp",
            location = "Kananaskis, AB"
        )
    )

    SearchCampsitesContent(
        campsites = sampleCampsites,
        onCampsiteClick = {},
        onFilterClick = {}
    )
}

