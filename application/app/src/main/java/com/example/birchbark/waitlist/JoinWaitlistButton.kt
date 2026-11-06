
package com.example.birchbark.waitlist

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.example.birchbark.model.WaitlistRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

/**
 * Displays a join or leave button based on the user's waitlist membership.
 *
 * Checks membership in Firestore and allows authenticated users to
 * join or leave a lottery waitlist while displaying operation feedback.
 *
 * @param lotteryId The Firestore document ID of the lottery.
 */
@Composable
fun JoinWaitlistButton(lotteryId: String) {

    val repository = remember { WaitlistRepository() }
    val userId = Firebase.auth.currentUser?.uid

    var isJoined by remember(lotteryId, userId) {
        mutableStateOf<Boolean?>(null)
    }

    var working by remember(lotteryId, userId) {
        mutableStateOf(false)
    }

    var message by remember(lotteryId, userId) {
        mutableStateOf("")
    }

    // Check whether the user is already registered
    LaunchedEffect(lotteryId, userId) {
        val uid = userId ?: return@LaunchedEffect

        isJoined = null

        repository.isOnWaitlist(
            lotteryId = lotteryId,
            userId = uid,
            onResult = { joined ->
                isJoined = joined
            },
            onFailure = { error ->
                message = "Error checking waitlist: ${error.message}"
            }
        )
    }

    Column {

        if (userId == null) {
            Text("Sign in to join.")
        } else if (isJoined == null) {
            Text(
                if (message.isNotEmpty()) message
                else "Checking waitlist..."
            )
        } else {

            Button(
                enabled = !working,
                onClick = {
                    val uid = userId ?: return@Button

                    working = true
                    message = ""

                    if (isJoined == true) {

                        repository.leaveWaitlist(
                            lotteryId = lotteryId,
                            userId = uid,
                            onResult = { result ->
                                working = false

                                message = when (result) {
                                    WaitlistRepository.LeaveResult.LEFT -> {
                                        isJoined = false
                                        "Successfully left the waitlist!"
                                    }

                                    WaitlistRepository.LeaveResult.NOT_JOINED -> {
                                        isJoined = false
                                        "You are not on this waitlist."
                                    }

                                    WaitlistRepository.LeaveResult.LOTTERY_NOT_FOUND ->
                                        "Lottery not found."
                                }
                            },
                            onFailure = { error ->
                                working = false
                                message = "Error: ${error.message}"
                            }
                        )

                    } else {

                        repository.joinWaitlist(
                            lotteryId = lotteryId,
                            userId = uid,
                            onResult = { result ->
                                working = false

                                message = when (result) {
                                    WaitlistRepository.JoinResult.JOINED -> {
                                        isJoined = true
                                        "Successfully joined!"
                                    }

                                    WaitlistRepository.JoinResult.ALREADY_JOINED -> {
                                        isJoined = true
                                        "You are already registered."
                                    }

                                    WaitlistRepository.JoinResult.WAITLIST_FULL ->
                                        "The waitlist is full."

                                    WaitlistRepository.JoinResult.REGISTRATION_CLOSED ->
                                        "Registration is not currently open."

                                    WaitlistRepository.JoinResult.LOTTERY_NOT_FOUND ->
                                        "Lottery not found."
                                }
                            },
                            onFailure = { error ->
                                working = false
                                message = "Error: ${error.message}"
                            }
                        )
                    }
                }
            ) {
                Text(
                    when {
                        working -> "Please wait..."
                        isJoined == true -> "Leave Waitlist"
                        else -> "Join Waitlist"
                    }
                )
            }
        }

        if (message.isNotEmpty() && isJoined != null) {
            Text(message)
        }
    }
}
