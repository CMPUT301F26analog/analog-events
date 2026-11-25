package com.example.birchbark.ui.entrant

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.birchbark.UserProfile
import com.example.birchbark.model.AvailableDates
import com.example.birchbark.model.Campsite
import com.example.birchbark.model.CampsiteBooking
import com.example.birchbark.model.CampsiteRepository
import com.example.birchbark.model.Lottery
import com.example.birchbark.model.LotteryRepository
import com.example.birchbark.model.Reservation
import com.example.birchbark.model.ReservationRepository
import com.example.birchbark.model.ReservationStatus
import com.example.birchbark.ui.theme.BirchBarkColors
import com.example.birchbark.ui.theme.birchBarkButtonColors
import com.example.birchbark.ui.theme.birchBarkFieldColors
import com.example.birchbark.util.Time
import java.time.LocalDate
import java.time.format.DateTimeParseException

// See #056 in LLM.md

/**
 * US 01.04.01, 01.04.03, 01.06.01, 01.06.02: the entrant's reservations with
 * real data, opened from Profile > Reservations History. Lottery wins still
 * waiting for an answer are listed first with a Pick your dates button;
 * confirmed and cancelled stays are shown in [EntrantReservationsScreen]
 * (Active / Past tabs).
 *
 * @param profile the signed-in entrant. Their FID loads the reservations, and
 *   their contact details are shared with the owner when they confirm.
 * @param onBack called when the entrant taps Back.
 */
@Composable
fun EntrantReservationsRoute(
    profile: UserProfile,
    onBack: () -> Unit
) {
    val reservationRepository = remember { ReservationRepository() }
    val campsiteRepository = remember { CampsiteRepository() }
    var reservations by remember { mutableStateOf<List<Reservation>?>(null) }
    var campsites by remember { mutableStateOf<Map<String, Campsite>>(emptyMap()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var answering by remember { mutableStateOf<Reservation?>(null) }
    //bumped after accepting or declining so the list reloads
    var reloadKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(profile.fid, reloadKey) {
        reservationRepository.getReservationsForEntrant(
            profile.fid,
            onResult = { found ->
                reservations = found
                //load each campsite once, for its name
                found.mapNotNull { it.campsiteId }.distinct().forEach { id ->
                    campsiteRepository.getCampsite(id, object : CampsiteRepository.LoadCallback {
                        override fun onLoaded(campsite: Campsite) {
                            campsites = campsites + (campsite.id to campsite)
                        }

                        //the card falls back to a generic name
                        override fun onNotFound() {}

                        override fun onError(error: Exception) {}
                    })
                }
            },
            onFailure = { errorMessage = "Couldn't load your reservations, check your connection and try again" }
        )
    }

    val chosen = answering
    if (chosen != null) {
        BackHandler { answering = null }
        PickDatesScreen(
            reservation = chosen,
            profile = profile,
            onDone = {
                answering = null
                reloadKey++
            },
            onBack = { answering = null }
        )
        return
    }

    val list = reservations
    val error = errorMessage
    Column(modifier = Modifier.fillMaxSize()) {
        TextButton(onClick = onBack) { Text("Back", color = BirchBarkColors.Gold) }

        when {
            error != null -> Text(error, modifier = Modifier.padding(16.dp))

            list == null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            else -> {
                //US 01.04.01: wins that still need an answer
                val waiting = list.filter { it.status == ReservationStatus.PENDING }
                if (waiting.isNotEmpty()) {
                    WonReservations(waiting, campsites, onPick = { answering = it })
                }

                //US 01.06.01, 01.06.02: confirmed and cancelled stays
                Box(modifier = Modifier.weight(1f)) {
                    EntrantReservationsScreen(reservations = toListItems(list, campsites))
                }
            }
        }
    }
}

/** The "You won!" cards, one per reservation waiting for an answer. */
@Composable
private fun WonReservations(
    waiting: List<Reservation>,
    campsites: Map<String, Campsite>,
    onPick: (Reservation) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("You won!", style = MaterialTheme.typography.headlineSmall, color = BirchBarkColors.Cream)
        for (reservation in waiting) {
            Card(modifier = Modifier.fillMaxWidth().testTag("wonReservation-${reservation.id}")) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        reservation.campsiteId?.let { campsites[it]?.name } ?: "Campsite lottery win",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text("Pick your dates to accept, or decline if you've changed your mind.")
                    Button(
                        onClick = { onPick(reservation) },
                        colors = birchBarkButtonColors(),
                        modifier = Modifier.padding(top = 8.dp).testTag("pickDatesButton")
                    ) {
                        Text("Pick your dates")
                    }
                }
            }
        }
    }
}

/**
 * Turns confirmed and cancelled reservations into the rows
 * [EntrantReservationsScreen] shows. Pending ones have no dates yet, so they
 * are left out (they're shown under "You won!" instead), and so are declined
 * and rejected ones, which never became a stay.
 *
 * @param reservations the entrant's reservations.
 * @param campsites campsite ID to campsite, for the names.
 */
fun toListItems(reservations: List<Reservation>, campsites: Map<String, Campsite>): List<ReservationListItem> {
    return reservations
        .filter { it.status == ReservationStatus.CONFIRMED || it.status == ReservationStatus.CANCELLED }
        .mapNotNull { reservation ->
            val start = reservation.startDate ?: return@mapNotNull null
            val end = reservation.endDate ?: return@mapNotNull null
            ReservationListItem(
                id = reservation.id,
                campsiteName = reservation.campsiteId?.let { campsites[it]?.name } ?: "Campsite",
                startDate = Time.toLocalDate(start),
                endDate = Time.lastDayOf(end),
                cancelled = reservation.status == ReservationStatus.CANCELLED
            )
        }
}

/**
 * US 01.04.01, 01.04.03, 02.11.01: answering one lottery win. Shows which days
 * are still free, lets the entrant type their first and last day, then asks
 * them to accept the campsite rules ([ReservationRulesConfirmation]) before
 * confirming. They can decline instead.
 *
 * Multi-campsite lotteries (US 01.04.02) aren't handled yet: the winner books
 * the lottery's first campsite.
 *
 * @param reservation the PENDING reservation being answered.
 * @param profile the signed-in entrant, whose contact details go to the owner.
 * @param onDone called after accepting or declining succeeds.
 * @param onBack called when the entrant taps Back.
 */
@Composable
fun PickDatesScreen(
    reservation: Reservation,
    profile: UserProfile,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val reservationRepository = remember { ReservationRepository() }
    var lottery by remember { mutableStateOf<Lottery?>(null) }
    var campsite by remember { mutableStateOf<Campsite?>(null) }
    var bookings by remember { mutableStateOf<List<CampsiteBooking>?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }

    var firstDayText by remember { mutableStateOf("") }
    var lastDayText by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }
    //set once the days check out, which moves on to the rules step
    var pickedDays by remember { mutableStateOf<Pair<LocalDate, LocalDate>?>(null) }
    var saving by remember { mutableStateOf(false) }

    LaunchedEffect(reservation.id) {
        LotteryRepository().getLottery(reservation.lotteryId, object : LotteryRepository.LoadCallback {
            override fun onLoaded(lottery: Lottery) {
                setLottery(lottery)
                val campsiteId = reservation.campsiteId ?: lottery.campsiteIds.firstOrNull()
                if (campsiteId == null) {
                    loadError = "This lottery has no campsite"
                    return
                }
                CampsiteRepository().getCampsite(campsiteId, object : CampsiteRepository.LoadCallback {
                    override fun onLoaded(campsite: Campsite) {
                        setCampsite(campsite)
                    }

                    override fun onNotFound() {
                        loadError = "This campsite doesn't exist anymore"
                    }

                    override fun onError(error: Exception) {
                        loadError = "Couldn't load the campsite, check your connection and try again"
                    }
                })
                reservationRepository.getBookingsForCampsite(
                    campsiteId,
                    onResult = { found -> bookings = found },
                    onFailure = { loadError = "Couldn't load the booked dates, check your connection and try again" }
                )
            }

            override fun onNotFound() {
                loadError = "This lottery doesn't exist anymore"
            }

            override fun onError(error: Exception) {
                loadError = "Couldn't load the lottery, check your connection and try again"
            }

            //the parameters share names with the screen's variables, so set them through these
            fun setLottery(found: Lottery) {
                lottery = found
            }

            fun setCampsite(found: Campsite) {
                campsite = found
            }
        })
    }

    val shownLottery = lottery
    val shownCampsite = campsite
    val shownBookings = bookings
    val error = loadError
    val picked = pickedDays

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) { Text("Back", color = BirchBarkColors.Gold) }

        when {
            error != null -> Text(error, color = BirchBarkColors.Cream)

            shownLottery == null || shownCampsite == null || shownBookings == null -> CircularProgressIndicator()

            //step 2 (US 02.11.01): agree to the rules, then save
            picked != null -> {
                Text(
                    "${shownCampsite.name}: ${Time.format(picked.first)} to ${Time.format(picked.second)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = BirchBarkColors.Cream
                )
                ReservationRulesConfirmation(
                    campsite = shownCampsite,
                    enabled = !saving,
                    onConfirm = {
                        saving = true
                        reservationRepository.confirm(
                            reservation = reservation,
                            campsiteId = shownCampsite.id,
                            startDate = Time.startOfDay(picked.first),
                            endDate = Time.endOfDay(picked.second),
                            name = "${profile.firstName} ${profile.lastName}".trim(),
                            email = profile.email,
                            phone = profile.phoneNumber,
                            onSuccess = onDone,
                            onFailure = {
                                saving = false
                                pickedDays = null
                                formError = "Couldn't confirm, those days may have just been taken. Try again."
                            }
                        )
                    }
                )
                OutlinedButton(onClick = { pickedDays = null }, enabled = !saving) {
                    Text("Change dates", color = BirchBarkColors.Gold)
                }
            }

            //step 1 (US 01.04.01): see the free days and pick some
            else -> {
                Text(shownCampsite.name, style = MaterialTheme.typography.headlineSmall, color = BirchBarkColors.Cream)
                Text(
                    "Booking period: ${Time.format(AvailableDates.firstBookableDay(shownLottery))} to " +
                        Time.format(AvailableDates.lastBookableDay(shownLottery)),
                    color = BirchBarkColors.Cream
                )
                Text("Free dates", style = MaterialTheme.typography.titleMedium, color = BirchBarkColors.Cream)
                Text(
                    AvailableDates.label(AvailableDates.freeRanges(shownLottery, shownBookings)),
                    color = BirchBarkColors.Cream,
                    modifier = Modifier.testTag("freeDates")
                )

                Text("Type dates like 2026-11-20.", color = BirchBarkColors.MutedText)
                OutlinedTextField(
                    value = firstDayText,
                    onValueChange = { firstDayText = it },
                    label = { Text("First day") },
                    singleLine = true,
                    colors = birchBarkFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("firstDayField")
                )
                OutlinedTextField(
                    value = lastDayText,
                    onValueChange = { lastDayText = it },
                    label = { Text("Last day") },
                    singleLine = true,
                    colors = birchBarkFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("lastDayField")
                )
                formError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val first = parseDay(firstDayText)
                            val last = parseDay(lastDayText)
                            formError = if (first == null || last == null) {
                                "Enter both days like 2026-11-20"
                            } else {
                                AvailableDates.problemWith(shownLottery, shownBookings, first, last)
                            }
                            if (formError == null && first != null && last != null) {
                                pickedDays = first to last
                            }
                        },
                        colors = birchBarkButtonColors(),
                        modifier = Modifier.testTag("nextButton")
                    ) {
                        Text("Next")
                    }

                    //US 01.04.03: decline the win
                    OutlinedButton(
                        onClick = {
                            saving = true
                            reservationRepository.decline(
                                reservation.id,
                                onSuccess = onDone,
                                onFailure = {
                                    saving = false
                                    formError = "Couldn't decline, check your connection and try again"
                                }
                            )
                        },
                        enabled = !saving,
                        modifier = Modifier.testTag("declineButton")
                    ) {
                        Text("Decline", color = BirchBarkColors.Gold)
                    }
                }
            }
        }
    }
}

/** Reads a typed day like 2026-11-20, or null if it isn't one. */
private fun parseDay(text: String): LocalDate? {
    return try {
        LocalDate.parse(text.trim())
    } catch (e: DateTimeParseException) {
        null
    }
}
