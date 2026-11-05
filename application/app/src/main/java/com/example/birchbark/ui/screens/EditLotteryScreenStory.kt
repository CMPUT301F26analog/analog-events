package com.example.birchbark.ui.screens

// LLM assistance

import com.example.birchbark.model.Campsite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.birchbark.model.Lottery
import com.example.birchbark.model.LotteryRepository

/**
 * Connects the Edit Lottery screen to Akshat's LotteryRepository: loads the campsite's lottery and saves
 * changes. Entrants see the saved changes the next time they open the campsite.
 * NEEDS LotteryRepository.getLotteryForCampsite (see README).
 */
@Composable
fun EditLotteryRoute(
    campsite: Campsite,
    onBack: () -> Unit
) {
    val repo = remember { LotteryRepository() }
    var lottery by remember { mutableStateOf<Lottery?>(null) }
    var loading by remember { mutableStateOf(true) }
    var messages by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(campsite.id) {
        repo.getLotteryForCampsite(campsite.id, object : LotteryRepository.LoadCallback {
            override fun onLoaded(loaded: Lottery) {
                lottery = loaded
                loading = false
            }

            override fun onNotFound() {
                loading = false
            }

            override fun onError(error: Exception) {
                loading = false
                messages = listOf("Couldn't load the lottery.")
            }
        })
    }

    EditLotteryScreen(
        campsiteName = campsite.name,
        lottery = lottery,
        loading = loading,
        messages = messages,
        onSave = { updated ->
            messages = emptyList()
            repo.updateLottery(updated, object : LotteryRepository.SaveCallback {
                override fun onSuccess(lotteryId: String) {
                    lottery = updated
                    messages = listOf("Saved. Entrants will see the changes.")
                }

                override fun onInvalid(errors: List<String>) {
                    messages = errors
                }

                override fun onError(error: Exception) {
                    messages = listOf("Couldn't save. Check your connection and try again.")
                }
            })
        },
        onBack = onBack
    )
}
