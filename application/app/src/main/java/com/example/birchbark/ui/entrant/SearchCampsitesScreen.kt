package com.example.birchbark.ui.entrant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.font.FontWeight
import com.example.birchbark.ui.theme.birchBarkButtonColors

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
 * entrant shell (EntrantApp) takes care of that.
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
    val results = campsites.filter { it.matchesSearch(keyword) && filter(it) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = keyword,
                onValueChange = { keyword = it },
                placeholder = { Text("Search your campsite") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = birchBarkFieldColors(),
                trailingIcon = {
                    if (keyword.isNotEmpty()) {
                        IconButton(onClick = { keyword = "" }) {
                            Text("✕", color = BirchBarkColors.Gold)
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("searchField")
            )

            Button(
                onClick = { },
                shape = RoundedCornerShape(14.dp),
                colors = birchBarkButtonColors(),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(52.dp)
            ) {
                Text("⌕", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val countText = if (results.size == 1) "Found 1 campsite" else "Found ${results.size} campsites"
            Text(
                text = countText,
                fontWeight = FontWeight.Bold,
                color = BirchBarkColors.Cream
            )

            if (onFilterClick != null) {
                OutlinedButton(
                    onClick = onFilterClick,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BirchBarkColors.Gold),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("filterButton")
                ) {
                    Text("☰", color = BirchBarkColors.Gold)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Filters", color = BirchBarkColors.Gold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (results.isEmpty()) {
            EmptyState(
                title = "Site not found",
                hint = "Try search with a different keyword",
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("notFound")
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(results) { campsite ->
                    CampsiteSummary(
                        campsite = campsite,
                        onClick = { onCampsiteClick(campsite) },
                        modifier = Modifier.testTag("searchResult")
                    )
                }
            }
        }
    }
}