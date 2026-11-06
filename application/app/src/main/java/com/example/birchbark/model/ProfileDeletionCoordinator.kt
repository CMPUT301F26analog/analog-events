package com.example.birchbark.model

import com.example.birchbark.UserProfile
import com.example.birchbark.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import android.util.Log


class ProfileDeletionCoordinator(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val users: UserRepository = UserRepository(),
    private val waitlists: WaitlistRepository = WaitlistRepository()
) {

    fun deleteProfile(
        profile: UserProfile,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val fid = profile.fid.trim()
        val authUid = profile.authUid.trim()

        if (fid.isBlank() || authUid.isBlank()) {
            onFailure(
                IllegalArgumentException("Profile identity is incomplete.")
            )
            return
        }

        if (FirebaseAuth.getInstance().currentUser?.uid != authUid) {
            onFailure(
                IllegalArgumentException(
                    "You can only delete your own profile."
                )
            )
            return
        }

        db.collection("lotteries")
            .get(Source.SERVER)
            .addOnSuccessListener { result ->
                val lotteryIds = result.documents.map { it.id }

                removeMemberships(
                    lotteryIds = lotteryIds,
                    index = 0,
                    fid = fid,
                    onComplete = {
                        users.removeProfile(
                            fid = fid,
                            authUid = authUid,
                            onSuccess = onSuccess,
                            onFailure = { error ->
                                onFailure(
                                    IllegalStateException(
                                        "STAGE 4 FAILED - PROFILE DELETION: ${error.message}",
                                        error
                                    )
                                )
                            }
                        )

                    },
                    onFailure = onFailure
                )
            }
            .addOnFailureListener { error ->
                Log.e(
                    "ProfileDeletion",
                    "STAGE 1 FAILED: Reading lotteries",
                    error
                )

                onFailure(
                    IllegalStateException(
                        "STAGE 1 FAILED: ${error.message}",
                        error
                    )
                )
            }
    }

    private fun removeMemberships(
        lotteryIds: List<String>,
        index: Int,
        fid: String,
        onComplete: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (index >= lotteryIds.size) {
            onComplete()
            return
        }

        val lotteryId = lotteryIds[index]

        fun next() {
            removeMemberships(
                lotteryIds = lotteryIds,
                index = index + 1,
                fid = fid,
                onComplete = onComplete,
                onFailure = onFailure
            )
        }

        waitlists.isOnWaitlist(
            lotteryId = lotteryId,
            userId = fid,
            onResult = { joined ->

                if (!joined) {
                    next()
                } else {
                    waitlists.leaveWaitlist(
                        lotteryId = lotteryId,
                        userId = fid,
                        onResult = { result ->
                            when (result) {
                                WaitlistRepository.LeaveResult.LEFT,
                                WaitlistRepository.LeaveResult.NOT_JOINED -> {
                                    next()
                                }

                                WaitlistRepository.LeaveResult.LOTTERY_NOT_FOUND -> {
                                    onFailure(
                                        IllegalStateException(
                                            "Lottery disappeared during cleanup."
                                        )
                                    )
                                }
                            }
                        },

                        onFailure = { error ->
                            onFailure(
                                IllegalStateException(
                                    "STAGE 3 FAILED - WAITLIST REMOVAL ($lotteryId): ${error.message}",
                                    error
                                )
                            )
                        }

                    )
                }
            },
            onFailure = { error ->
                onFailure(
                    IllegalStateException(
                        "STAGE 2 FAILED - WAITLIST LOOKUP ($lotteryId): ${error.message}",
                        error
                    )
                )
            }

        )
    }
}