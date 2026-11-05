package com.example.birchbark.ui.screens

// LLM assistance

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.birchbark.model.EditPermission
import com.example.birchbark.model.Lottery
import com.example.birchbark.ui.theme.BirchBarkColors.Gold
import com.example.birchbark.ui.theme.birchBarkButtonColors
import com.example.birchbark.ui.theme.birchBarkFieldColors

/**
 * The owner's "Update lottery" form (UI only: saving is done by Akshat's LotteryRepository through
 * EditLotteryRoute). Fields: registration period, booking period, number of winners, waiting list limit.
 * Everything is read-only once the draw has happened (Akshat's EditPermission).
 */
@Composable
fun EditLotteryScreen(
    campsiteName: String,
    lottery: Lottery?,
    loading: Boolean,
    messages: List<String>,
    onSave: (Lottery) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var form by remember(lottery) {
        mutableStateOf(lottery?.toForm() ?: LotteryForm())
    }

    var formErrors by remember { mutableStateOf<List<String>>(emptyList()) }
    val canEdit = lottery != null && EditPermission.canEdit(lottery)
    val fieldColors = birchBarkFieldColors()

    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        TextButton(onClick = onBack) { Text("Back", color = Gold) }
        Text("Lottery for $campsiteName", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        when {
            loading -> Text("Loading...", modifier = Modifier.padding(top = 16.dp))
            lottery == null -> Text("This campsite doesn't have a lottery yet.", modifier = Modifier.padding(top = 16.dp))
            else -> {
                if (!canEdit) {
                    Text("This lottery has already been drawn, so it can't be edited.",
                        color = Gold, modifier = Modifier.padding(vertical = 8.dp))
                }
                Text("Dates look like 2026-11-15", modifier = Modifier.padding(top = 8.dp))

                OutlinedTextField(value = form.registrationStart, enabled = canEdit, singleLine = true,
                    onValueChange = { form = form.copy(registrationStart = it) },
                    label = { Text("Registration start") }, colors = fieldColors, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = form.registrationEnd, enabled = canEdit, singleLine = true,
                    onValueChange = { form = form.copy(registrationEnd = it) },
                    label = { Text("Registration end") }, colors = fieldColors, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = form.bookingStart, enabled = canEdit, singleLine = true,
                    onValueChange = { form = form.copy(bookingStart = it) },
                    label = { Text("Booking start") }, colors = fieldColors, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = form.bookingEnd, enabled = canEdit, singleLine = true,
                    onValueChange = { form = form.copy(bookingEnd = it) },
                    label = { Text("Booking end") }, colors = fieldColors, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = form.numWinners, enabled = canEdit, singleLine = true,
                    onValueChange = { form = form.copy(numWinners = it) },
                    label = { Text("Number of winners") }, colors = fieldColors, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = form.waitlistCapacity, enabled = canEdit, singleLine = true,
                    onValueChange = { form = form.copy(waitlistCapacity = it) },
                    label = { Text("Waiting list limit (blank = no limit)") }, colors = fieldColors,
                    modifier = Modifier.fillMaxWidth())

                Button(
                    enabled = canEdit,
                    colors = birchBarkButtonColors(),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    onClick = {
                        val result = form.applyTo(lottery)
                        formErrors = result.errors
                        if (result.lottery != null) onSave(result.lottery)
                    }
                ) { Text("Save changes") }
            }
        }

        (formErrors + messages).forEach {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
