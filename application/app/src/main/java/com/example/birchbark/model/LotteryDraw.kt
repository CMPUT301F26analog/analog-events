package com.example.birchbark.model

import kotlin.random.Random

// LLM assistance: see doc/LLM.md, Prompt #044 (Akshat)

object LotteryDraw {

    fun drawWinners(entrantIds: List<String>, numWinners: Int, random: Random = Random.Default): List<String> {
        if (numWinners <= 0) return emptyList()
        return entrantIds.shuffled(random).take(numWinners)
    }

    fun buildReservationId(lotteryId: String, entrantId: String): String {
        return "${lotteryId}_$entrantId"
    }

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