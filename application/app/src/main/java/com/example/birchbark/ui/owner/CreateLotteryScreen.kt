package com.example.birchbark.ui.owner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.birchbark.model.Campsite
import com.example.birchbark.model.Lottery
import com.example.birchbark.model.LotteryRepository
import com.example.birchbark.util.Time
import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * US 02.04.01 Create lottery. The Campsite Lottery screen from the My Campgrounds
 * mockup. Opens from the campsite details screen, and saves a lottery for that
 * campsite with Akshat's [LotteryRepository.createLottery], which checks all the
 * lottery rules first.
 *
 * CreateLotteryScreen does the saving, and CreateLotteryForm is just the form so
 * the intent tests can check it without Firestore.
 *
 * Outstanding issues. Dates are typed in as year-month-day for now instead of a
 * date picker. Only one campsite per lottery, multi campsite lotteries are
 * US 02.04.02 (Part 4).
 */

/**
 * Shows the lottery form and saves the lottery to Firestore.
 *
 * @param campsite the campsite the lottery is for.
 * @param onCreated called once the lottery is saved.
 * @param onCancel called if the owner goes back without saving.
 */
@Composable
fun CreateLotteryScreen(campsite: Campsite, onCreated: () -> Unit, onCancel: () -> Unit) {
    val repository = remember { LotteryRepository() }
    var saveErrors by remember { mutableStateOf<List<String>>(listOf()) }
    var saving by remember { mutableStateOf(false) }

    CreateLotteryForm(
        campsite = campsite,
        errors = saveErrors,
        saving = saving,
        onSave = { lottery ->
            saving = true
            repository.createLottery(lottery, object : LotteryRepository.SaveCallback {
                override fun onSuccess(lotteryId: String) {
                    saving = false
                    onCreated()
                }

                override fun onInvalid(errors: List<String>) {
                    //broke a lottery rule, stay on the form and show what's wrong
                    saving = false
                    saveErrors = errors
                }

                override fun onError(error: Exception) {
                    saving = false
                    saveErrors = listOf("Couldn't save, check your connection and try again")
                }
            })
        },
        onCancel = onCancel
    )
}

/**
 * The lottery form. Turns what the owner typed into a [Lottery] for this campsite.
 *
 * @param campsite the campsite the lottery is for.
 * @param errors rule messages to show above the button, empty if there are none.
 * @param saving true while it's saving, so the button can't be tapped twice.
 * @param onSave called with the new lottery when the owner taps Set Lottery.
 * @param onCancel called when the owner taps back.
 */
@Composable
fun CreateLotteryForm(
    campsite: Campsite,
    errors: List<String>,
    saving: Boolean,
    onSave: (Lottery) -> Unit,
    onCancel: () -> Unit
) {
    var registrationStart by remember { mutableStateOf("") }
    var registrationEnd by remember { mutableStateOf("") }
    var bookingStart by remember { mutableStateOf("") }
    var bookingEnd by remember { mutableStateOf("") }
    var numWinners by remember { mutableStateOf("") }
    var waitlistCapacity by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextButton(onClick = onCancel) { Text("Back") }
        Text("Campsite Lottery", style = MaterialTheme.typography.headlineSmall)
        Text(campsite.name, style = MaterialTheme.typography.titleMedium)
        Text(
            "Type dates like 2026-11-20. Booking has to start at least a day after " +
                    "registration's last day, that's when the winners get drawn.",
            style = MaterialTheme.typography.bodySmall
        )

        OutlinedTextField(
            value = registrationStart,
            onValueChange = { registrationStart = it },
            label = { Text("Registration start") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("registrationStartField")
        )
        OutlinedTextField(
            value = registrationEnd,
            onValueChange = { registrationEnd = it },
            label = { Text("Registration last day") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("registrationEndField")
        )
        OutlinedTextField(
            value = bookingStart,
            onValueChange = { bookingStart = it },
            label = { Text("Booking start") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("bookingStartField")
        )
        OutlinedTextField(
            value = bookingEnd,
            onValueChange = { bookingEnd = it },
            label = { Text("Booking last day") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("bookingEndField")
        )
        OutlinedTextField(
            value = numWinners,
            onValueChange = { numWinners = it },
            label = { Text("Number of winners") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().testTag("numWinnersField")
        )
        OutlinedTextField(
            value = waitlistCapacity,
            onValueChange = { waitlistCapacity = it },
            label = { Text("Waitlist capacity (optional)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().testTag("waitlistField")
        )

        //form problems first, then the lottery rule messages
        val shownFormError = formError
        val allErrors = if (shownFormError != null) listOf(shownFormError) + errors else errors
        for (error in allErrors) {
            Text(
                error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.testTag("errorMessage")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            enabled = !saving,
            modifier = Modifier.fillMaxWidth().testTag("setLotteryButton"),
            onClick = {
                val regStart = parseDate(registrationStart)
                val regEnd = parseDate(registrationEnd)
                val bookStart = parseDate(bookingStart)
                val bookEnd = parseDate(bookingEnd)

                if (regStart == null || regEnd == null || bookStart == null || bookEnd == null) {
                    formError = "Enter every date like 2026-11-20"
                } else if (waitlistCapacity.isNotBlank() && waitlistCapacity.trim().toIntOrNull() == null) {
                    formError = "Waitlist capacity must be a number"
                } else {
                    formError = null
                    val lottery = Lottery(
                        campsiteIds = listOf(campsite.id),
                        ownerId = campsite.ownerId,
                        registrationStart = Time.startOfDay(regStart),
                        //end of day so registration stays open all of its last day
                        registrationEnd = Time.endOfDay(regEnd),
                        bookingStart = Time.startOfDay(bookStart),
                        bookingEnd = Time.endOfDay(bookEnd),
                        //anything that isn't a number becomes 0, which NumWinnersRule catches
                        numWinners = numWinners.trim().toIntOrNull() ?: 0,
                        //blank means no limit
                        waitlistCapacity = waitlistCapacity.trim().toIntOrNull()
                    )
                    onSave(lottery)
                }
            }
        ) {
            Text(if (saving) "Saving..." else "Set Lottery")
        }
    }
}

/**
 * Reads a typed date like 2026-11-20.
 *
 * @param text what the owner typed.
 * @return the date, or null if it isn't a real date in that format.
 */
private fun parseDate(text: String): LocalDate? {
    return try {
        LocalDate.parse(text.trim())
    } catch (e: DateTimeParseException) {
        null
    }
}