package com.example.birchbark.ui.entrant

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.birchbark.ui.theme.birchBarkButtonColors

import androidx.compose.ui.tooling.preview.Preview

/**
 * US 01.05.01: the confirmation the entrant sees after confirming a reservation.
 * Shows the campsite name and the dates that were booked.
 *
 * See [TBD 1] in LLM.md
 * v v v
 * It takes plain text instead of a reservation, because the reservation saved by
 * the draw only has the campsite's ID and may not have dates, while the reservation
 * flow already has the campsite and the dates the entrant picked.
 *
 * @param campsiteName the reserved campsite's name.
 * @param dates the booked dates as text, e.g. from [com.example.birchbark.util.datesLabel].
 * @param onDone called when the entrant taps Done, to leave the confirmation.
 * @param modifier modifier from the parent layout.
 */
@Composable
fun ReservationConfirmationScreen(
    campsiteName: String,
    dates: String,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Reservation confirmed", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        // Campsite name
        Text(campsiteName, style = MaterialTheme.typography.titleLarge)
        // Dates
        Text(dates)

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onDone,
            colors = birchBarkButtonColors(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Done")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReservationConfirmationScreenPreview() {
    ReservationConfirmationScreen(
        campsiteName = "Pine Ridge Overlook",
        dates = "Jul 20 – Jul 23, 2026",
        onDone = {}
    )
}
