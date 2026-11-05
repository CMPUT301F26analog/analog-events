package com.example.birchbark.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.birchbark.model.EntrantFilters
import com.example.birchbark.ui.theme.BirchBarkColors.Gold
import com.example.birchbark.ui.theme.birchBarkButtonColors
import com.example.birchbark.ui.theme.birchBarkFieldColors

/**
 * Renders a configurable entry form for updating search and sorting criteria.
 *
 * This screen manages localized input states for campsite attributes, handles type conversions
 * for numerical parameters, and exposes action callbacks to confirm, flush, or discard
 * the updated criteria layout.
 *
 * @param currentFilters The baseline filtering criteria used to populate initial field values.
 * @param onApply Callback triggered with updated criteria when confirming or clearing selections.
 * @param onCancel Callback triggered to exit the screen without persisting state changes.
 * @param modifier Structural styling configurations to inject from the parent layout scope.
 */
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

    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Filters", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = location, onValueChange = { location = it },
            label = { Text("Location") }, colors = fieldColors, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = startDate, onValueChange = { startDate = it },
            label = { Text("Start date") }, colors = fieldColors, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = endDate, onValueChange = { endDate = it },
            label = { Text("End date") }, colors = fieldColors, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = occupancy, onValueChange = { occupancy = it },
            label = { Text("Minimum occupancy") }, colors = fieldColors, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = rating, onValueChange = { rating = it },
            label = { Text("Minimum rating") }, colors = fieldColors, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onApply(
                    EntrantFilters(
                        location = location,
                        startDate = startDate,
                        endDate = endDate,
                        minOccupancy = occupancy.trim().toIntOrNull(),
                        minRating = rating.trim().toDoubleOrNull()
                    )
                )
            },
            colors = birchBarkButtonColors(),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Apply") }
        OutlinedButton(
            onClick = {
                onApply(EntrantFilters())
            },
            modifier = Modifier.fillMaxWidth())
        {
            Text("Clear filters")
        }
        TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Cancel", color = Gold) }
    }
}