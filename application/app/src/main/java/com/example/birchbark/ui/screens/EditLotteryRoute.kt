package com.example.birchbark.ui.screens

// LLM assistance: see doc/LLM.md, Prompt #009 (Akshat)

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.birchbark.model.Campsite
import com.example.birchbark.model.Lottery
import com.example.birchbark.model.LotteryRepository

/**
 * US 02.04.06 Update lottery listing. Loads the campsite's current lottery,
 * shows it in [EditLotteryScreen], and saves changes with
 * [LotteryRepository.updateLottery], which checks the lottery rules and
 * refuses edits after the draw.
 *
 * @param campsite the campsite whose lottery is being edited.
 * @param onBack called when the owner taps back.
 */
@Composable
fun EditLotteryRoute(campsite: Campsite, onBack: () -> Unit) {
    val repository = remember { LotteryRepository() }
    var lottery by remember { mutableStateOf<Lottery?>(null) }
    var loading by remember { mutableStateOf(true) }
    var saveErrors by remember { mutableStateOf<List<String>>(emptyList()) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    //load the current lottery when the screen opens
    LaunchedEffect(campsite.id) {
        repository.getLotteryForCampsite(campsite.id, object : LotteryRepository.LoadCallback {
            override fun onLoaded(loaded: Lottery) {
                lottery = loaded
                loading = false
            }

            override fun onNotFound() {
                loading = false
            }

            override fun onError(error: Exception) {
                loading = false
                saveErrors = listOf("Couldn't load the lottery, check your connection and try again")
            }
        })
    }

    EditLotteryScreen(
        campsiteName = campsite.name,
        lottery = lottery,
        loading = loading,
        errors = saveErrors,
        successMessage = successMessage,
        onSave = { updated ->
            saveErrors = emptyList()
            successMessage = null
            repository.updateLottery(updated, object : LotteryRepository.SaveCallback {
                override fun onSuccess(lotteryId: String) {
                    lottery = updated
                    successMessage = "Lottery updated"
                }

                override fun onInvalid(errors: List<String>) {
                    saveErrors = errors
                }

                override fun onError(error: Exception) {
                    saveErrors = listOf("Couldn't save, check your connection and try again")
                }
            })
        },
        onBack = onBack
    )
}