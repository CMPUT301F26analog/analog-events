
package com.example.birchbark.ui.entrant

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.birchbark.model.Campsite
import com.example.birchbark.model.Lottery
import com.example.birchbark.model.LotteryRepository
import com.example.birchbark.model.RegistrationChecker
import com.example.birchbark.ui.screens.CampsiteDetailsScreen
import com.example.birchbark.waitlist.JoinWaitlistButton

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

@Composable
fun EntrantCampsitesApp() {
    var selectedCampsite by remember {
        mutableStateOf<Campsite?>(null)
    }

    val campsite = selectedCampsite

    if (campsite == null) {
        SearchCampsitesScreen(
            onCampsiteClick = {
                selectedCampsite = it
            }
        )
    } else {
        BackHandler {
            selectedCampsite = null
        }

        EntrantCampsiteDetailsRoute(
            campsite = campsite,
            onBack = {
                selectedCampsite = null
            }
        )
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
            val lottery = selectOpenLottery(
                lotteries = lotteries.orEmpty(),
                now = System.currentTimeMillis()
            )

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
    }
}
