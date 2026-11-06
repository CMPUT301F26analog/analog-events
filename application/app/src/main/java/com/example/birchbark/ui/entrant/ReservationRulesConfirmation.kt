package com.example.birchbark.ui.entrant

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.birchbark.model.Campsite
import com.example.birchbark.ui.theme.birchBarkButtonColors

@Composable
fun ReservationRulesConfirmation(
    campsite: Campsite,
    onConfirm: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    var agreed by remember(
        campsite.id,
        campsite.rules,
        campsite.cancellationPolicy
    ) {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Reservation Rules",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Campsite Rules",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = campsite.rules.ifBlank {
                "No campsite rules provided."
            },
            modifier = Modifier.testTag("campsiteRules")
        )

        Text(
            text = "Cancellation Policy",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = campsite.cancellationPolicy.ifBlank {
                "No cancellation policy provided."
            },
            modifier = Modifier.testTag("cancellationPolicy")
        )

        Row {
            Checkbox(
                checked = agreed,
                onCheckedChange = { agreed = it },
                enabled = enabled,
                modifier = Modifier.testTag("acceptRulesCheckbox")
            )

            Text(
                text = "I have read and agree to the campsite rules and cancellation policy.",
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Button(
            onClick = {
                if (agreed && enabled) {
                    onConfirm(true)
                }
            },
            enabled = agreed && enabled,
            colors = birchBarkButtonColors(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("confirmReservationButton")
        ) {
            Text("Confirm Reservation")
        }
    }
}
