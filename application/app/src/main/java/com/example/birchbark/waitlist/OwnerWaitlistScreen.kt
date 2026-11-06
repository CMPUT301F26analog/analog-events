package com.example.birchbark.waitlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.birchbark.model.OwnerWaitlistRepository

@Composable
fun OwnerWaitlistScreen(
    lotteryId: String,
    onBack: () -> Unit
) {
    val repository = remember { OwnerWaitlistRepository() }

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
            }
        }
    }
}
