package com.example.birchbark.ui.entrant

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.example.birchbark.ui.theme.ChipKind
import com.example.birchbark.ui.theme.PhotoCard
import com.example.birchbark.ui.theme.ScreenTitle
import com.example.birchbark.ui.theme.StatusChip
import com.example.birchbark.ui.theme.BirchBarkColors

private enum class ReservationTab {
    ACTIVE, PAST
}

/**
 * The entrant's booking list (US 01.06.01, US 01.06.02), styled like the
 * mockup's Booking List: photo cards with a status marker, and a details view
 * when a card is tapped. System back closes the details first.
 *
 * US 01.07.01 (added by Yash): the details view of a reservation that isn't
 * over has a Cancel Reservation button, which asks "are you sure" first.
 *
 * Outstanding issues. The photo is a placeholder. Downloading a CSV from the
 * details view (US 01.05.02) isn't here yet.
 *
 * @param reservations the entrant's reservations.
 * @param today the date used to decide what's active and what's past.
 * @param onCancel called with the reservation once the entrant confirms cancelling it.
 */
@Composable
fun EntrantReservationsScreen(
    reservations: List<ReservationListItem>,
    today: LocalDate = LocalDate.now(),
    onCancel: (ReservationListItem) -> Unit = {}
) {
    var selectedTab by remember {
        mutableStateOf(ReservationTab.ACTIVE)
    }

    var selectedReservation by remember {
        mutableStateOf<ReservationListItem?>(null)
    }

    //US 01.07.01: true while the "are you sure" dialog is open
    var confirmingCancel by remember { mutableStateOf(false) }

    val formatter = remember {
        DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)
    }

    val details = selectedReservation

    //system back closes the details before it leaves the whole screen
    BackHandler(enabled = details != null) {
        selectedReservation = null
    }

    //US 01.07.01: make sure a reservation isn't cancelled by accident
    if (confirmingCancel && details != null) {
        AlertDialog(
            onDismissRequest = { confirmingCancel = false },
            title = { Text("Cancel this reservation?") },
            text = { Text("Your dates will be given up, and this can't be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmingCancel = false
                        selectedReservation = null
                        onCancel(details)
                    },
                    modifier = Modifier.testTag("confirmCancelReservationButton")
                ) {
                    Text("Cancel it")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { confirmingCancel = false },
                    modifier = Modifier.testTag("keepReservationButton")
                ) {
                    Text("Keep it")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (details != null) {
            TextButton(
                onClick = { selectedReservation = null }
            ) {
                Text("Back to reservations", color = BirchBarkColors.Gold)
            }

            ScreenTitle("Reservation Details")

            val (statusText, statusKind) = statusOf(details, today)
            PhotoCard(
                height = 220.dp,
                topRow = { StatusChip(statusText, statusKind) },
                bottomStart = {
                    Text(
                        details.campsiteName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = BirchBarkColors.Cream
                    )
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BirchBarkColors.TealCard)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Check-in: ${details.startDate.format(formatter)}",
                    color = BirchBarkColors.Cream
                )
                Text(
                    "Check-out: ${details.endDate.format(formatter)}",
                    color = BirchBarkColors.Cream
                )
            }

            //US 01.07.01: only reservations that aren't over can be cancelled
            if (canCancel(details, today)) {
                OutlinedButton(
                    onClick = { confirmingCancel = true },
                    modifier = Modifier.fillMaxWidth().testTag("cancelReservationButton")
                ) {
                    Text("Cancel Reservation", color = BirchBarkColors.Gold)
                }
            }
        } else {
            ScreenTitle("My Reservations")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                //the selected tab is the disabled one, and its disabled colours are the gold "selected" look
                TabButton(
                    text = "Active",
                    selected = selectedTab == ReservationTab.ACTIVE,
                    onClick = { selectedTab = ReservationTab.ACTIVE },
                    modifier = Modifier.testTag("activeTab")
                )
                TabButton(
                    text = "Past",
                    selected = selectedTab == ReservationTab.PAST,
                    onClick = { selectedTab = ReservationTab.PAST },
                    modifier = Modifier.testTag("pastTab")
                )
            }

            val visibleReservations = when (selectedTab) {
                ReservationTab.ACTIVE ->
                    activeReservations(reservations, today)

                ReservationTab.PAST ->
                    pastReservations(reservations, today)
            }

            if (visibleReservations.isEmpty()) {
                Text(
                    text = if (selectedTab == ReservationTab.ACTIVE) {
                        "No active reservations"
                    } else {
                        "No past reservations"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = BirchBarkColors.Cream,
                    modifier = Modifier.testTag("emptyReservations")
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = visibleReservations,
                        key = { it.id }
                    ) { reservation ->
                        val (statusText, statusKind) = statusOf(reservation, today)
                        PhotoCard(
                            modifier = Modifier.testTag("reservation-${reservation.id}"),
                            onClick = { selectedReservation = reservation },
                            topRow = { StatusChip(statusText, statusKind) },
                            bottomStart = {
                                Text(
                                    reservation.campsiteName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BirchBarkColors.Cream
                                )
                                Text(
                                    "Check-in: ${reservation.startDate.format(formatter)}",
                                    color = BirchBarkColors.Cream
                                )
                                Text(
                                    "Check-out: ${reservation.endDate.format(formatter)}",
                                    color = BirchBarkColors.MutedText
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

/** A rounded tab button. The selected one is disabled and drawn gold. */
@Composable
private fun TabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = !selected,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = BirchBarkColors.TealCard,
            contentColor = BirchBarkColors.Cream,
            disabledContainerColor = BirchBarkColors.Gold,
            disabledContentColor = BirchBarkColors.DarkGreen
        ),
        modifier = modifier
    ) {
        Text(text)
    }
}

// See [TBD 4] in LLM.md
/**
 * Works out the status marker for a reservation. The words are in capitals so
 * they don't clash with the "Active" and "Past" tab buttons.
 *
 * @param item the reservation.
 * @param today the date to compare with.
 * @return the marker text and which colours it gets.
 */
private fun statusOf(item: ReservationListItem, today: LocalDate): Pair<String, ChipKind> = when {
    item.cancelled -> "CANCELLED" to ChipKind.CANCELLED
    item.endDate.isBefore(today) -> "PAST" to ChipKind.PAST
    else -> "ACTIVE" to ChipKind.ACTIVE
}