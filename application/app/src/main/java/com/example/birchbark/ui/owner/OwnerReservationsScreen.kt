package com.example.birchbark.ui.owner

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.birchbark.model.Campsite
import com.example.birchbark.model.CampsiteRepository
import com.example.birchbark.model.NotificationRepository
import com.example.birchbark.model.OwnerReservationTab
import com.example.birchbark.model.OwnerReservationTabs
import com.example.birchbark.model.RejectionNotice
import com.example.birchbark.model.Reservation
import com.example.birchbark.model.ReservationRepository
import com.example.birchbark.model.ReservationStatus
import com.example.birchbark.util.Time

// See #049 in LLM.md

/** Preserves the existing navigation callbacks for callers outside the Message Center. */
@Composable
fun OwnerReservationsScreen(
    ownerId: String,
    onBack: () -> Unit
) {
    OwnerReservationsScreen(
        ownerId = ownerId,
        onBack = onBack,
        onOpenMessage = null
    )
}

/**
 * US 02.07.01, 02.07.02, 02.07.03, 02.08.01: the owner's reservations across all their
 * campsites, split into Active, Past and Cancelled. Loads the reservations and
 * the owner's campsites (for their names), then shows [OwnerReservationsList].
 *
 * @param ownerId the owner's FID.
 * @param onOpenMessage opens the active confirmed booking’s thread by reservation ID (US 02.12.01).
 * @param onBack called when the owner taps Back.
 */
@Composable
fun OwnerReservationsScreen(
    onOpenMessage: ((String) -> Unit)?,
    ownerId: String,
    onBack: () -> Unit
) {
    val reservationRepository = remember { ReservationRepository() }
    val campsiteRepository = remember { CampsiteRepository() }
    var reservations by remember { mutableStateOf<List<Reservation>?>(null) }
    var campsiteNames by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    //load every time the screen opens
    LaunchedEffect(ownerId) {
        reservationRepository.getReservationsForOwner(
            ownerId,
            onResult = { found -> reservations = found },
            onFailure = { errorMessage = "Couldn't load your reservations, check your connection and try again" }
        )
        campsiteRepository.getCampsitesByOwner(ownerId, object : CampsiteRepository.ListCallback {
            override fun onLoaded(campsites: List<Campsite>) {
                campsiteNames = campsites.associate { it.id to it.name }
            }

            //names are a nice-to-have, the list still works without them
            override fun onError(error: Exception) {}
        })
    }

    val list = reservations
    val error = errorMessage

    when {
        error != null -> Column(modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp)) {
            TextButton(onClick = onBack) { Text("Back") }
            Text(error)
        }

        list == null -> Box(
            modifier = Modifier.fillMaxSize().safeDrawingPadding(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        else -> OwnerReservationsList(
            reservations = list,
            campsiteNames = campsiteNames,
            now = System.currentTimeMillis(),
            onBack = onBack,
            onMessage = onOpenMessage?.let { open -> { r -> open(r.id) } },
            onReject = { reservation ->
                reservationRepository.reject(
                    reservation,
                    onSuccess = {
                        //move it to the Cancelled tab without reloading
                        reservations = reservations?.map {
                            if (it.id == reservation.id) it.copy(status = ReservationStatus.REJECTED) else it
                        }
                        val name = reservation.campsiteId?.let { campsiteNames[it] }
                        NotificationRepository().send(
                            listOf(RejectionNotice.build(reservation, name, System.currentTimeMillis(), ownerId)),
                            onResult = {},
                            onFailure = { error -> Log.e("OwnerReservations", "Couldn't notify entrant", error) }
                        )
                    },
                    onFailure = {
                        Toast.makeText(context, "Couldn't reject, check your connection and try again", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }
}

/** Preserves the existing navigation callbacks for callers outside the Message Center. */
@Composable
fun OwnerReservationsList(
    reservations: List<Reservation>,
    campsiteNames: Map<String, String>,
    now: Long,
    onBack: () -> Unit,
    onReject: (Reservation) -> Unit = {}
) {
    OwnerReservationsList(
        reservations = reservations,
        campsiteNames = campsiteNames,
        now = now,
        onBack = onBack,
        onReject = onReject,
        onMessage = null
    )
}

/**
 * Draws the owner's reservations with Active / Past / Cancelled tabs. Kept
 * separate from [OwnerReservationsScreen] so UI tests can pass in a list
 * without Firestore.
 *
 * @param reservations every reservation for the owner's campsites.
 * @param campsiteNames campsite ID to name, for showing which campsite was booked.
 * @param now current time in epoch ms, used to split active from past.
 * @param onBack called when the owner taps Back.
 * @param onReject called when the owner confirms rejecting a reservation. US 02.08.01.
 * @param onMessage opens the selected confirmed booking for a reply (US 02.12.01).
 */
@Composable
fun OwnerReservationsList(
    onMessage: ((Reservation) -> Unit)?,
    reservations: List<Reservation>,
    campsiteNames: Map<String, String>,
    now: Long,
    onBack: () -> Unit,
    onReject: (Reservation) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(OwnerReservationTab.ACTIVE) }
    //the reservation waiting on the "are you sure" dialog, null when it's closed
    var toReject by remember { mutableStateOf<Reservation?>(null) }
    val shown = OwnerReservationTabs.forTab(reservations, selectedTab, now)

    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) { Text("Back") }
        Text("Reservations", style = MaterialTheme.typography.headlineMedium)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TabButton("Active", OwnerReservationTab.ACTIVE, selectedTab, "ownerActiveTab") { selectedTab = it }
            TabButton("Past", OwnerReservationTab.PAST, selectedTab, "ownerPastTab") { selectedTab = it }
            TabButton("Cancelled", OwnerReservationTab.CANCELLED, selectedTab, "ownerCancelledTab") { selectedTab = it }
        }

        if (shown.isEmpty()) {
            Text(
                when (selectedTab) {
                    OwnerReservationTab.ACTIVE -> "No active reservations"
                    OwnerReservationTab.PAST -> "No past reservations"
                    OwnerReservationTab.CANCELLED -> "No cancelled reservations"
                },
                modifier = Modifier.testTag("ownerEmptyReservations")
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items = shown, key = { it.id }) { reservation ->
                    ReservationCard(
                        reservation,
                        campsiteNames,
                        onMessage = if (onMessage != null && reservation.status == ReservationStatus.CONFIRMED &&
                            selectedTab == OwnerReservationTab.ACTIVE) ({ onMessage(reservation) }) else null,
                        onReject = if (OwnerReservationTabs.canReject(reservation, now)) {
                            { toReject = reservation }
                        } else {
                            null
                        }
                    )
                }
            }
        }
    }

    toReject?.let { reservation ->
        AlertDialog(
            onDismissRequest = { toReject = null },
            title = { Text("Reject this reservation?") },
            text = { Text("The entrant will be told and the dates will open up again. This can't be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onReject(reservation)
                        toReject = null
                    },
                    modifier = Modifier.testTag("confirmRejectButton")
                ) { Text("Reject") }
            },
            dismissButton = {
                TextButton(onClick = { toReject = null }) { Text("Keep it") }
            }
        )
    }
}

/** One tab button; the selected tab is disabled so it looks pressed. */
@Composable
private fun TabButton(
    label: String,
    tab: OwnerReservationTab,
    selected: OwnerReservationTab,
    tag: String,
    onSelect: (OwnerReservationTab) -> Unit
) {
    Button(
        onClick = { onSelect(tab) },
        enabled = selected != tab,
        modifier = Modifier.testTag(tag)
    ) {
        Text(label)
    }
}

/**
 * One reservation: campsite, dates, status and, once confirmed, the entrant's
 * contact details (TA feedback: owners should see who booked).
 * Shows a Reject button when [onReject] isn't null (US 02.08.01).
 *
 * @param onMessage opens the entrant conversation when the booking is eligible (US 02.12.01).
 */
@Composable
private fun ReservationCard(
    reservation: Reservation,
    campsiteNames: Map<String, String>,
    onMessage: (() -> Unit)?,
    onReject: (() -> Unit)?
) {
    Card(modifier = Modifier.fillMaxWidth().testTag("ownerReservation-${reservation.id}")) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                reservation.campsiteId?.let { campsiteNames[it] } ?: "Campsite not chosen yet",
                style = MaterialTheme.typography.titleMedium
            )
            Text(datesLabel(reservation))
            Text("Status: ${statusLabel(reservation.status)}")

            val name = reservation.entrantName
            if (name != null) {
                Text("Booked by: $name")
                reservation.entrantEmail?.let { Text("Email: $it") }
                reservation.entrantPhone?.let { Text("Phone: $it") }
            } else {
                Text("Waiting for the entrant to confirm")
            }

            if (onMessage != null) {
                TextButton(onClick = onMessage) { Text("Message entrant") }
            }
            if (onReject != null) {
                TextButton(
                    onClick = onReject,
                    modifier = Modifier.testTag("rejectButton-${reservation.id}")
                ) { Text("Reject") }
            }
        }
    }
}

/**
 * The reservation's dates as text, or a note that they haven't been picked.
 * The end is stored as the moment the last night ends, so it is shown with
 * [Time.lastDayOf] (same as lottery end dates).
 */
internal fun datesLabel(reservation: Reservation): String {
    val start = reservation.startDate
    val end = reservation.endDate
    return if (start == null || end == null) {
        "Dates not picked yet"
    } else {
        "${Time.format(Time.toLocalDate(start))} to ${Time.format(Time.lastDayOf(end))}"
    }
}

/**
 * A status as owners read it.
 **/
internal fun statusLabel(status: ReservationStatus): String {
    return when (status) {
        ReservationStatus.PENDING -> "Waiting for entrant"
        ReservationStatus.CONFIRMED -> "Confirmed"
        ReservationStatus.DECLINED -> "Declined by entrant"
        ReservationStatus.CANCELLED -> "Cancelled by entrant"
        ReservationStatus.REJECTED -> "Rejected"
    }
}