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
import androidx.compose.material3.AlertDialog
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
import com.example.birchbark.model.PickDatesForm
import com.example.birchbark.model.Reservation
import com.example.birchbark.model.ReservationRepository
import com.example.birchbark.model.ReservationStatus
import com.example.birchbark.ui.theme.BirchBarkColors
import com.example.birchbark.ui.theme.birchBarkButtonColors
import com.example.birchbark.ui.theme.birchBarkFieldColors
import com.example.birchbark.util.Time
import com.example.birchbark.util.datesLabel
import java.time.LocalDate

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
        //rejected ones show as cancelled so the entrant sees what happened (US 02.08.01)
        .filter {
            it.status == ReservationStatus.CONFIRMED ||
                it.status == ReservationStatus.CANCELLED ||
                it.status == ReservationStatus.REJECTED
        }
        .mapNotNull { reservation ->
            val start = reservation.startDate ?: return@mapNotNull null
            val end = reservation.endDate ?: return@mapNotNull null
            ReservationListItem(
                id = reservation.id,
                campsiteName = reservation.campsiteId?.let { campsites[it]?.name } ?: "Campsite",
                startDate = Time.toLocalDate(start),
                endDate = Time.lastDayOf(end),
                cancelled = reservation.status != ReservationStatus.CONFIRMED
            )
        }
}

/**
 * Everything the pick dates screen shows, in one place, so the screen only
 * draws it and the loading/checking/saving code updates it (Riley's PR feedback).
 *
 * @property lottery the lottery the entrant won, null until loaded.
 * @property campsite the campsite being booked, null until loaded.
 * @property bookings days already booked at the campsite, null until loaded.
 * @property firstDayText what the entrant typed for the first day.
 * @property lastDayText what the entrant typed for the last day.
 * @property pickedDays the checked days, set once Next passes; shows the rules step.
 * @property loadError why the screen couldn't load, if it couldn't.
 * @property formError why the typed days or saving didn't work, if they didn't.
 * @property isSaving true while confirming or declining, so buttons can't be tapped twice.
 * @property confirmingDecline true while the "are you sure" dialog for declining is open.
 * @property confirmed true once the reservation is saved; shows the confirmation (US 01.05.01).
 */
data class PickDatesUiState(
    val lottery: Lottery? = null,
    val campsite: Campsite? = null,
    val bookings: List<CampsiteBooking>? = null,
    val firstDayText: String = "",
    val lastDayText: String = "",
    val pickedDays: Pair<LocalDate, LocalDate>? = null,
    val loadError: String? = null,
    val formError: String? = null,
    val isSaving: Boolean = false,
    val confirmingDecline: Boolean = false,
    val confirmed: Boolean = false,
) {
    /** True until the lottery, campsite and bookings have all loaded. */
    val isLoading: Boolean
        get() = lottery == null || campsite == null || bookings == null
}

/**
 * US 01.04.01, 01.04.03, 02.11.01: answering one lottery win. Shows which days
 * are still free, lets the entrant type their first and last day (checked by
 * [PickDatesForm]), then asks them to accept the campsite rules
 * ([ReservationRulesConfirmation]) before confirming. They can decline
 * instead, after an "are you sure" dialog. Once saved, Eliza's
 * [ReservationConfirmationScreen] shows what was booked (US 01.05.01).
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
    var state by remember { mutableStateOf(PickDatesUiState()) }

    LaunchedEffect(reservation.id) {
        loadPickDates(reservation, reservationRepository) { update -> state = update(state) }
    }

    //Next: check the typed days, then move on to the rules step
    fun onNext() {
        val lottery = state.lottery ?: return
        val bookings = state.bookings ?: return
        val capacity = state.campsite?.maxOccupancy ?: 1
        state = when (val result = PickDatesForm.check(state.firstDayText, state.lastDayText, lottery, bookings, capacity)) {
            is PickDatesForm.Result.Picked ->
                state.copy(pickedDays = result.firstDay to result.lastDay, formError = null)
            is PickDatesForm.Result.Problem -> state.copy(formError = result.message)
        }
    }

    //Confirm Reservation, after the rules are accepted
    fun onConfirm() {
        val campsite = state.campsite ?: return
        val picked = state.pickedDays ?: return
        state = state.copy(isSaving = true)
        reservationRepository.confirm(
            reservation = reservation,
            campsiteId = campsite.id,
            startDate = Time.startOfDay(picked.first),
            endDate = Time.endOfDay(picked.second),
            name = "${profile.firstName} ${profile.lastName}".trim(),
            email = profile.email,
            phone = profile.phoneNumber,
            onSuccess = { state = state.copy(isSaving = false, confirmed = true) },
            onFailure = {
                state = state.copy(
                    isSaving = false,
                    pickedDays = null,
                    formError = "Couldn't confirm, those days may have just been taken. Try again."
                )
            }
        )
    }

    //Decline, after the "are you sure" dialog
    fun onDecline() {
        state = state.copy(isSaving = true, confirmingDecline = false)
        reservationRepository.decline(
            reservation.id,
            onSuccess = onDone,
            onFailure = {
                state = state.copy(isSaving = false, formError = "Couldn't decline, check your connection and try again")
            }
        )
    }

    //US 01.05.01: show what was booked, Done goes back to the reservations list
    val booked = state.pickedDays
    val bookedCampsite = state.campsite
    if (state.confirmed && booked != null && bookedCampsite != null) {
        ReservationConfirmationScreen(
            campsiteName = bookedCampsite.name,
            dates = datesLabel(Time.startOfDay(booked.first), Time.endOfDay(booked.second)),
            onDone = onDone
        )
        return
    }

    PickDatesContent(
        state = state,
        onBack = onBack,
        onFirstDayChange = { state = state.copy(firstDayText = it) },
        onLastDayChange = { state = state.copy(lastDayText = it) },
        onNext = ::onNext,
        onChangeDates = { state = state.copy(pickedDays = null) },
        onConfirm = ::onConfirm,
        onDeclineClick = { state = state.copy(confirmingDecline = true) },
        onDeclineDismiss = { state = state.copy(confirmingDecline = false) },
        onDeclineConfirm = ::onDecline
    )
}

/**
 * Loads the lottery, its campsite and the campsite's bookings for the pick
 * dates screen, reporting each piece through [update].
 *
 * @param reservation the reservation being answered.
 * @param repository used to load the bookings.
 * @param update called with a change to apply to the screen's state.
 */
private fun loadPickDates(
    reservation: Reservation,
    repository: ReservationRepository,
    update: ((PickDatesUiState) -> PickDatesUiState) -> Unit
) {
    fun fail(message: String) = update { it.copy(loadError = message) }

    LotteryRepository().getLottery(reservation.lotteryId, object : LotteryRepository.LoadCallback {
        override fun onLoaded(lottery: Lottery) {
            update { it.copy(lottery = lottery) }
            //single-campsite lotteries already know the campsite; otherwise use the lottery's first one
            val campsiteId = reservation.campsiteId ?: lottery.campsiteIds.firstOrNull()
            if (campsiteId == null) {
                fail("This lottery has no campsite")
                return
            }
            CampsiteRepository().getCampsite(campsiteId, object : CampsiteRepository.LoadCallback {
                override fun onLoaded(campsite: Campsite) {
                    update { it.copy(campsite = campsite) }
                }

                override fun onNotFound() = fail("This campsite doesn't exist anymore")

                override fun onError(error: Exception) =
                    fail("Couldn't load the campsite, check your connection and try again")
            })
            repository.getBookingsForCampsite(
                campsiteId,
                onResult = { found -> update { it.copy(bookings = found) } },
                onFailure = { fail("Couldn't load the booked dates, check your connection and try again") }
            )
        }

        override fun onNotFound() = fail("This lottery doesn't exist anymore")

        override fun onError(error: Exception) = fail("Couldn't load the lottery, check your connection and try again")
    })
}

/**
 * Draws the pick dates screen from [state]. Has no logic of its own, so UI
 * tests can pass in any state without Firestore.
 */
@Composable
fun PickDatesContent(
    state: PickDatesUiState,
    onBack: () -> Unit,
    onFirstDayChange: (String) -> Unit,
    onLastDayChange: (String) -> Unit,
    onNext: () -> Unit,
    onChangeDates: () -> Unit,
    onConfirm: () -> Unit,
    onDeclineClick: () -> Unit,
    onDeclineDismiss: () -> Unit,
    onDeclineConfirm: () -> Unit
) {
    val lottery = state.lottery
    val campsite = state.campsite
    val bookings = state.bookings
    val picked = state.pickedDays

    //US 01.04.03: make sure a win isn't declined by accident
    if (state.confirmingDecline) {
        AlertDialog(
            onDismissRequest = onDeclineDismiss,
            title = { Text("Decline this reservation?") },
            text = { Text("You'll give up your win, and this can't be undone.") },
            confirmButton = {
                TextButton(onClick = onDeclineConfirm, modifier = Modifier.testTag("confirmDeclineButton")) {
                    Text("Decline")
                }
            },
            dismissButton = {
                TextButton(onClick = onDeclineDismiss) { Text("Keep it") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) { Text("Back", color = BirchBarkColors.Gold) }

        when {
            state.loadError != null -> Text(state.loadError, color = BirchBarkColors.Cream)

            lottery == null || campsite == null || bookings == null -> CircularProgressIndicator()

            //step 2 (US 02.11.01): agree to the rules, then save
            picked != null -> {
                Text(
                    "${campsite.name}: ${Time.format(picked.first)} to ${Time.format(picked.second)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = BirchBarkColors.Cream
                )
                ReservationRulesConfirmation(
                    campsite = campsite,
                    enabled = !state.isSaving,
                    onConfirm = { onConfirm() }
                )
                OutlinedButton(onClick = onChangeDates, enabled = !state.isSaving) {
                    Text("Change dates", color = BirchBarkColors.Gold)
                }
            }

            //step 1 (US 01.04.01): see the free days and pick some
            else -> {
                Text(campsite.name, style = MaterialTheme.typography.headlineSmall, color = BirchBarkColors.Cream)
                Text(
                    "Booking period: ${Time.format(AvailableDates.firstBookableDay(lottery))} to " +
                        Time.format(AvailableDates.lastBookableDay(lottery)),
                    color = BirchBarkColors.Cream
                )
                Text("Free dates", style = MaterialTheme.typography.titleMedium, color = BirchBarkColors.Cream)
                Text(
                    AvailableDates.label(AvailableDates.freeRanges(lottery, bookings, campsite.maxOccupancy)),
                    color = BirchBarkColors.Cream,
                    modifier = Modifier.testTag("freeDates")
                )

                Text("Type dates like 2026-11-20.", color = BirchBarkColors.MutedText)
                OutlinedTextField(
                    value = state.firstDayText,
                    onValueChange = onFirstDayChange,
                    label = { Text("First day") },
                    singleLine = true,
                    colors = birchBarkFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("firstDayField")
                )
                OutlinedTextField(
                    value = state.lastDayText,
                    onValueChange = onLastDayChange,
                    label = { Text("Last day") },
                    singleLine = true,
                    colors = birchBarkFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("lastDayField")
                )
                state.formError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("formError"))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onNext,
                        colors = birchBarkButtonColors(),
                        modifier = Modifier.testTag("nextButton")
                    ) {
                        Text("Next")
                    }
                    OutlinedButton(
                        onClick = onDeclineClick,
                        enabled = !state.isSaving,
                        modifier = Modifier.testTag("declineButton")
                    ) {
                        Text("Decline", color = BirchBarkColors.Gold)
                    }
                }
            }
        }
    }
}
