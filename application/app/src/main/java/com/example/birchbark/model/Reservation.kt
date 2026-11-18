package com.example.birchbark.model
// LLM assistance: see doc/LLM.md, Prompt #43 (Akshat)

/**
 * A lottery win, as stored in the top-level Firestore "reservations" collection.
 * Created by the winner draw (US 02.05.01). Plain data only, like [Lottery].
 *
 * Document ID is "${lotteryId}_${entrantId}", so the same entrant can't get
 * two reservations from one lottery even if the draw runs twice.
 *
 * @property id Firestore document ID.
 * @property lotteryId The lottery this reservation was drawn from.
 * @property entrantId FID of the winning entrant (same ID the waitlist uses).
 * @property ownerId FID of the campsite owner, copied from the lottery so owner
 *   queries don't need a second lookup. US 02.07.01–03.
 * @property campsiteId Chosen campsite, or null until the entrant picks one. US 01.04.02.
 * @property startDate First night (epoch ms), or null until dates are picked. US 01.04.01.
 * @property endDate Last night (epoch ms), or null until dates are picked. US 01.04.01.
 * @property status Where the reservation is in its lifecycle.
 * @property createdAt When the draw created it (epoch ms).
 */
data class Reservation (
    val id: String = "",
    val lotteryId: String = "",
    val campsiteId: String? = null,
    val entrantId: String = "",
    val ownerId: String = "",
    val startDate: Long? = null,
    val endDate: Long? = null,
    val status: ReservationStatus = ReservationStatus.PENDING,
    val createdAt: Long = 0L
)