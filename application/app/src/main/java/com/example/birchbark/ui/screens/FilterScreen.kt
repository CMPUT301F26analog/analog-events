package com.example.birchbark.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.birchbark.model.EntrantFilters
import com.example.birchbark.ui.theme.BirchBarkColors.Gold
import com.example.birchbark.ui.theme.birchBarkButtonColors
import com.example.birchbark.ui.theme.birchBarkFieldColors
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.ui.text.input.KeyboardType
import com.example.birchbark.util.Time
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldDefaults.colors
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

/**
 * Renders the filter form for the browse screen (US 01.01.02).
 *
 * Location is free text, dates come from a range picker, and occupancy and
 *  rating are checked as the user types. Apply is disabled while a number is invalid.
 *
 * @param currentFilters The filters currently applied, used to fill the fields.
 * @param onApply Called with the new filters when the user taps Apply or Clear filters.
 * @param onCancel Called to leave the screen without changing the filters.
 * @param modifier Modifier from the parent layout.
 */

@OptIn(ExperimentalMaterial3Api::class) // See [7] in LLM.md, used to help fix the code
@Composable
fun FilterScreen(
    currentFilters: EntrantFilters,
    onApply: (EntrantFilters) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var location by remember { mutableStateOf(currentFilters.location) }
    var startDate by remember { mutableStateOf(currentFilters.startDate) }
    var endDate by remember { mutableStateOf(currentFilters.endDate) }
    var occupancy by remember { mutableStateOf(currentFilters.minOccupancy?.toString() ?: "") }
    var rating by remember { mutableStateOf(currentFilters.minRating?.toString() ?: "") }
    val fieldColors = birchBarkFieldColors()
    var showPicker by remember { mutableStateOf(false) }

    // Parse once; the same values are used for the error checks and for Apply. See [3] in LLM.md, used for helper code
    val occupancyValue = occupancy.trim().toIntOrNull()
    val ratingValue = rating.trim().replace(',', '.').toDoubleOrNull()
    val occupancyError = occupancy.isNotBlank() && (occupancyValue == null || occupancyValue < 1)
    val ratingError = rating.isNotBlank() && (ratingValue == null || ratingValue !in 1.0..5.0)

    Column(modifier = modifier.fillMaxSize().background(Color(0xFF004D40)).verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Filters", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = location, onValueChange = { location = it },
            label = { Text("Location") }, colors = fieldColors, modifier = Modifier.fillMaxWidth())

        // One button replaces the two date text fields. See [4] in LLM.md, used to understand and learn about showPicker
        OutlinedButton(
            onClick = {
                showPicker = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            val start = startDate?.let { Time.format(it) }
            val end = endDate?.let { Time.format(it) }
            Text(if (start == null) "Choose dates" else "$start to ${end ?: "?"}", color = Color.White)
        }

        // See [5] in LLM.md, used for helper code for error-catching
        OutlinedTextField(
            value = occupancy,
            onValueChange = { occupancy = it },
            label = { Text("Guests") },
            isError = occupancyError,
            supportingText = { if (occupancyError) Text("Enter a whole number, 1 or more") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = rating,
            onValueChange = { rating = it },
            label = { Text("Minimum rating") },
            isError = ratingError,
            supportingText = { if (ratingError) Text("Enter a number from 1 to 5") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                onApply(
                    EntrantFilters(
                        location = location,
                        startDate = startDate,
                        endDate = endDate,
                        minOccupancy = occupancyValue,
                        minRating = ratingValue
                    )
                )
            },
            colors = birchBarkButtonColors(),
            enabled = !occupancyError && !ratingError,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Apply") }

        OutlinedButton(
            onClick = {
                onApply(EntrantFilters())
            },
            modifier = Modifier.fillMaxWidth())
        {
            Text("Clear filters", color = Color.White)
        }

        TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Cancel", color = Gold) }
    }

    // See [4] in LLM.md, used for helper code --> Matches from the above code
    if (showPicker) {
        val pickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = startDate?.let { dateToUtcMillis(it) },
            initialSelectedEndDateMillis = endDate?.let { dateToUtcMillis(it) }
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDate = pickerState.selectedStartDateMillis?.let { utcMillisToDate(it) }
                    endDate = pickerState.selectedEndDateMillis?.let { utcMillisToDate(it) }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            }
        ) {
            DateRangePicker(state = pickerState)
        }
    }
}

// See [6] in LLM.md, used to help sort out date-time issues with helper code
private fun utcMillisToDate(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

private fun dateToUtcMillis(date: LocalDate): Long =
    date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()