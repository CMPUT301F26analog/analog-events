
package com.example.birchbark.ui.entrant

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.birchbark.model.Campsite
import com.example.birchbark.model.Lottery
import com.example.birchbark.model.LotteryRepository
import com.example.birchbark.model.LotteryStatus
import com.example.birchbark.model.LotteryStatusCalculator
import com.example.birchbark.model.RegistrationChecker
// import com.example.birchbark.ui.screens.CampsiteDetailsScreen // TBD remove this in a bug pr
import com.example.birchbark.util.Time
import com.example.birchbark.waitlist.JoinWaitlistButton
import com.example.birchbark.model.EntrantFilters


fun selectOpenLottery(
    lotteries: List<Lottery>,
    now: Long
): Lottery? {
    return lotteries
        .filter {
            it.id.isNotBlank() &&
                    RegistrationChecker.isOpen(it, now)
        }
        .maxByOrNull { it.registrationStart }
}

/**
 * Whether a campsite passes the entrant's filters (US 01.01.02).
 *
 * See [TBD 7] in LLM.md
 *
 * Location is a case-insensitive "contains", guests means the campsite holds at
 * least that many, and rating means at least that rating. Dates are not checked
 * here, because that needs availability data a campsite doesn't carry.
 *
 * */
fun Campsite.passesFilters(filters: EntrantFilters): Boolean {
    if (filters.location.isNotBlank() &&
        !location.contains(filters.location.trim(), ignoreCase = true)
    ) return false
    filters.minOccupancy?.let { if (maxOccupancy < it) return false }
    filters.minRating?.let { min ->
        val avg = averageRating() ?: return false // Unrated campsites fail a minimum rating filter
        if (avg < min) return false
    }
    return true
}

/**
 * The Browse tab: search results, the filter screen, and campsite details.
 * System back closes the filter screen or the details first.
 */
@Composable
fun EntrantCampsitesApp() {
    var selectedCampsite by remember { mutableStateOf<Campsite?>(null) }
    var filters by remember { mutableStateOf(EntrantFilters()) }
    var showFilters by remember { mutableStateOf(false) }

    val campsite = selectedCampsite

    when {
        showFilters -> {
            BackHandler { showFilters = false }
            FilterScreen(
                currentFilters = filters,
                onApply = {
                    filters = it
                    showFilters = false
                },
                onCancel = { showFilters = false }
            )
        }

        campsite == null -> {
            SearchCampsitesScreen(
                onCampsiteClick = { selectedCampsite = it },
                filter = { it.passesFilters(filters) },
                onFilterClick = { showFilters = true }
            )
        }

        else -> {
            BackHandler { selectedCampsite = null }
            EntrantCampsiteDetailsRoute(
                campsite = campsite,
                onBack = { selectedCampsite = null }
            )
        }
    }
}

@Composable
private fun EntrantCampsiteDetailsRoute(
    campsite: Campsite,
    onBack: () -> Unit
) {
    val repository = remember { LotteryRepository() }

    var lotteries by remember(campsite.id) {
        mutableStateOf<List<Lottery>?>(null)
    }

    var error by remember(campsite.id) {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(campsite.id) {
        repository.getLotteriesForCampsite(
            campsite.id,
            object : LotteryRepository.ListCallback {
                override fun onLoaded(found: List<Lottery>) {
                    lotteries = found
                }

                override fun onError(exception: Exception) {
                    error = exception.message
                        ?: "Unable to load lottery"
                }
            }
        )
    }

    when {
        error != null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                TextButton(onClick = onBack) {
                    Text("Back")
                }

                Text("Error: $error")
            }
        }

        lotteries == null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        else -> {
            val list = lotteries.orEmpty()
            //the open lottery if there is one, otherwise the latest one so we can say why they can't join
            val lottery = selectOpenLottery(list, System.currentTimeMillis())
                ?: list.maxByOrNull { it.registrationStart }

            CampsiteDetailsScreen(
                campsite = campsite,
                lottery = lottery,
                joined = false,
                isFull = false,
                message = null,
                onBack = onBack,
                onJoin = {},
                onLeave = {}
            )
        }
    }
}

@Composable
fun EntrantWaitlistSection(
    lottery: Lottery?,
    now: Long = System.currentTimeMillis(),
    buttonContent: @Composable (String) -> Unit = { id ->
        JoinWaitlistButton(lotteryId = id)
    }
) {
    val openLottery = lottery?.takeIf {
        it.id.isNotBlank() &&
                RegistrationChecker.isOpen(it, now)
    }

    if (openLottery != null) {
        Spacer(modifier = Modifier.height(16.dp))
        buttonContent(openLottery.id)
    } else {
        //US 01.01.06: say why there's no join button instead of showing nothing
        lotteryMessage(lottery, now)?.let { message ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(message, modifier = Modifier.testTag("lotteryMessage"))
        }
    }
}

// See #052 in LLM.md
/**
 * US 01.01.06: what to show instead of the join button when the entrant can't join.
 *
 * @param lottery the campsite's latest lottery, or null if it has none.
 * @param now current time in epoch ms.
 * @return the message, or null if the lottery is open (the join button shows instead).
 */
fun lotteryMessage(lottery: Lottery?, now: Long): String? {
    if (lottery == null || lottery.id.isBlank()) return "No lottery for this campsite right now."
    return when (LotteryStatusCalculator.status(lottery, now)) {
        LotteryStatus.SCHEDULED ->
            "Registration opens ${Time.format(Time.toLocalDate(lottery.registrationStart))}."
        LotteryStatus.OPEN -> null
        LotteryStatus.CLOSED -> "Registration has closed. Winners will be drawn soon."
        LotteryStatus.DRAWN -> "Winners have been drawn for this lottery."
    }
}
