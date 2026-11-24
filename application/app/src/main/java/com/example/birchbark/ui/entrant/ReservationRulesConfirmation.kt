package com.example.birchbark.ui.entrant

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.birchbark.model.Campsite
import com.example.birchbark.ui.theme.birchBarkButtonColors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.birchbark.ui.theme.BirchBarkColors

import androidx.compose.ui.tooling.preview.Preview

/**
 * US 02.11.01: the campsite rules and cancellation policy the entrant has to
 * agree to before confirming a reservation. Styled like the mockup's
 * Campsite Policy page: the policy sits in a scrollable rounded panel, then
 * the agree checkbox and the gold confirm button.
 *
 * @param campsite the campsite whose rules and cancellation policy are shown.
 * @param onConfirm called with true when the entrant has agreed and taps Confirm.
 * @param enabled false to lock the checkbox and button, e.g. while saving.
 */
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
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = BirchBarkColors.Gold
        )

        //long policies scroll inside the panel so the checkbox and button stay on screen
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 360.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(BirchBarkColors.TealCard)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Campsite Rules",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BirchBarkColors.Gold
            )

            Text(
                text = campsite.rules.ifBlank {
                    "No campsite rules provided."
                },
                color = BirchBarkColors.Cream,
                modifier = Modifier.testTag("campsiteRules")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Cancellation Policy",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BirchBarkColors.Gold
            )

            Text(
                text = campsite.cancellationPolicy.ifBlank {
                    "No cancellation policy provided."
                },
                color = BirchBarkColors.Cream,
                modifier = Modifier.testTag("cancellationPolicy")
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = agreed,
                onCheckedChange = { agreed = it },
                enabled = enabled,
                colors = CheckboxDefaults.colors(
                    checkedColor = BirchBarkColors.Gold,
                    checkmarkColor = BirchBarkColors.DarkGreen,
                    uncheckedColor = BirchBarkColors.Cream
                ),
                modifier = Modifier.testTag("acceptRulesCheckbox")
            )

            Text(
                text = "I have read and agree to the campsite rules and cancellation policy.",
                color = BirchBarkColors.Cream,
                modifier = Modifier.padding(start = 4.dp)
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

@Preview(showBackground = true)
@Composable
fun ReservationRulesConfirmationPreview() {
    val sampleCampsite = Campsite(
        id = "1",
        name = "Pine Ridge Overlook",
        location = "Banff National Park, AB",
        rules = "• Quiet hours are from 10:00 PM to 7:00 AM.\n" +
                "• Keep all food and scented items stored in bear-proof lockers.\n" +
                "• Campfires are permitted in designated fire rings only.\n" +
                "• Pack out all garbage and leave the site clean.",
        cancellationPolicy = "Free cancellation up to 48 hours before check-in. " +
                "Cancellations made within 48 hours of check-in will forfeit the first night's fee."
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BirchBarkColors.DarkGreen)
            .safeDrawingPadding()
    ) {
        ReservationRulesConfirmation(
            campsite = sampleCampsite,
            onConfirm = {}
        )
    }
}
