package com.example.birchbark.model

import kotlin.random.Random

// LLM assistance: see doc/LLM.md, Prompt #044 (Akshat)

/**
 * US 02.05.01: picks a lottery's winners and turns them into reservations.
 *
 * Pure logic with no Firestore, so it can be unit tested. Saving the results
 * is ReservationRepository's job.
 */
object LotteryDraw {

    /**
     * Picks winners at random from the waitlist, with no repeats.
     * If fewer people joined than there are winner slots, everyone wins.
     *
     * @param entrantIds FIDs of every entrant on the waitlist.
     * @param numWinners how many to pick ([Lottery.numWinners]).
     * @param random source of randomness. Tests pass a seeded Random so the
     *   result is repeatable; the app uses the default.
     * @return the winners' FIDs, or an empty list if there are no entrants
     *   or numWinners is 0 or less.
     */
    fun drawWinners(entrantIds: List<String>, numWinners: Int, random: Random = Random.Default): List<String> {
        if (numWinners <= 0) return emptyList()
        return entrantIds.shuffled(random).take(numWinners)
    }

    /**
     * Builds a reservation's document ID. One ID per lottery and entrant, so
     * running the draw twice overwrites instead of creating duplicates.
     *
     * @param lotteryId the lottery's document ID.
     * @param entrantId the winner's FID.
     * @return "<lotteryId>_<entrantId>".
     */
    fun buildReservationId(lotteryId: String, entrantId: String): String {
        return "${lotteryId}_$entrantId"
    }

    /**
     * Turns the winners into PENDING reservations, one per winner.
     * The campsite is filled in only for a single-campsite lottery; for a
     * multi-campsite lottery the winner picks one later (US 01.04.02).
     *
     * @param lottery the lottery that was drawn.
     * @param winnerIds the winners' FIDs, from [drawWinners].
     * @param now when the draw happened, in epoch ms.
     * @return one reservation per winner, in the same order as [winnerIds].
     */
    fun buildReservations(lottery: Lottery, winnerIds: List<String>, now: Long): List<Reservation> {
        val campsiteId = lottery.campsiteIds.singleOrNull()
        return winnerIds.map { entrantId ->
            Reservation(
                id = buildReservationId(lottery.id, entrantId),
                lotteryId = lottery.id,
                entrantId = entrantId,
                ownerId = lottery.ownerId,
                campsiteId = campsiteId,
                createdAt = now,
            )
        }
    }
}