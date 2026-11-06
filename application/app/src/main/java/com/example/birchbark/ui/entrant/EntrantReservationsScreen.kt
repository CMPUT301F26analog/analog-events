package com.example.birchbark.ui.entrant

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class ReservationTab {
    ACTIVE, PAST
}

@Composable
fun EntrantReservationsScreen(
    reservations: List<ReservationListItem>,
    today: LocalDate = LocalDate.now()
) {
    var selectedTab by remember {
        mutableStateOf(ReservationTab.ACTIVE)
    }

    var selectedReservation by remember {
        mutableStateOf<ReservationListItem?>(null)
    }

    val formatter = remember {
        DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)
    }

    val details = selectedReservation

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
                Text("Back to reservations")
            }

            Text(
                "Reservation Details",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(details.campsiteName)
            Text("Check-in: ${details.startDate.format(formatter)}")
            Text("Check-out: ${details.endDate.format(formatter)}")
        } else {
            Text(
                "My Reservations",
                style = MaterialTheme.typography.headlineMedium
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { selectedTab = ReservationTab.ACTIVE },
                    enabled = selectedTab != ReservationTab.ACTIVE,
                    modifier = Modifier.testTag("activeTab")
                ) {
                    Text("Active")
                }

                Button(
                    onClick = { selectedTab = ReservationTab.PAST },
                    enabled = selectedTab != ReservationTab.PAST,
                    modifier = Modifier.testTag("pastTab")
                ) {
                    Text("Past")
                }
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
                    modifier = Modifier.testTag("emptyReservations")
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = visibleReservations,
                        key = { it.id }
                    ) { reservation ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedReservation = reservation
                                }
                                .testTag("reservation-${reservation.id}")
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    reservation.campsiteName,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    "Check-in: ${
                                        reservation.startDate.format(formatter)
                                    }"
                                )

                                Text(
                                    "Check-out: ${
                                        reservation.endDate.format(formatter)
                                    }"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
