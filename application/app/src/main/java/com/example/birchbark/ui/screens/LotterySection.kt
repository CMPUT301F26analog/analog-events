package com.example.birchbark.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.birchbark.model.Lottery
import com.example.birchbark.model.LotteryStatus
import com.example.birchbark.model.LotteryStatusCalculator
import com.example.birchbark.ui.theme.birchBarkButtonColors
import com.example.birchbark.util.Time

// See [TBD] in LLM.md, which was used as a starter for how to write the below code.

/**
 * Renders the lottery details and entry controls within the campsite details screen.
 *
 * Shows the registration and booking dates, winner count, waitlist limit and
 * current status. Shows "Join waiting list" or, once joined, "Leave waiting list".
 * The caller must re-check that registration is still open (RegistrationChecker)
 * when the user taps join, because this screen only checks when it is drawn.
 *
 * @param lottery The active lottery, or null if none is scheduled.
 * @param joined True if the current user is already on the waiting list.
 * @param isFull True if the waiting list has reached its capacity (WaitlistChecker).
 * @param message An optional info or error string shown at the bottom.
 * @param onJoin Called when the user taps "Join waiting list".
 * @param onLeave Called when the user taps "Leave waiting list".
 */
@Composable
fun LotterySection(
    lottery: Lottery?,
    joined: Boolean,
    isFull: Boolean,
    message: String?,
    onJoin: () -> Unit,
    onLeave: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Lottery", style = MaterialTheme.typography.titleLarge)

        if (lottery == null) {
            Text("No open lottery.")
        } else {
            val status = LotteryStatusCalculator.status(lottery, Time.now())
            val regStart = Time.format(Time.toLocalDate(lottery.registrationStart))
            val regEnd = Time.format(Time.lastDayOf(lottery.registrationEnd))
            val bookStart = Time.format(Time.toLocalDate(lottery.bookingStart))
            val bookEnd = Time.format(Time.lastDayOf(lottery.bookingEnd))

            Text("Registration: $regStart to $regEnd")
            Text("Booking: $bookStart to $bookEnd")
            Text("Winners: ${lottery.numWinners}")
            Text("Waiting list limit: ${lottery.waitlistCapacity?.toString() ?: "No limit"}")
            Text("Status: ${statusText(status, regStart)}")

            if (joined) {
                OutlinedButton(
                    onClick = onLeave,
                    enabled = status != LotteryStatus.DRAWN,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Leave waiting list")
                }
            } else {
                Button(
                    onClick = onJoin,
                    enabled = status == LotteryStatus.OPEN && !isFull,
                    colors = birchBarkButtonColors(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isFull) "Waiting list full" else "Join waiting list")
                }
            }
        }

        if (message != null) {
            Text(message, color = MaterialTheme.colorScheme.primary)
        }
    }
}

/**
 * Maps a lottery stage to the text shown after "Status:".
 *
 * @param status The current stage of the lottery.
 * @param opensOn The formatted registration start date, shown while scheduled.
 * @return A label suitable for the screen.
 */
private fun statusText(status: LotteryStatus, opensOn: String): String = when (status) {
    LotteryStatus.SCHEDULED -> "Opens $opensOn"
    LotteryStatus.OPEN -> "Registration open"
    LotteryStatus.CLOSED -> "Registration closed"
    LotteryStatus.DRAWN -> "Draw complete"
}