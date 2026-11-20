package com.example.birchbark.model

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.random.Random
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull

// LLM assistance: see doc/LLM.md, Prompt #045 (Akshat)
/**
 * Unit tests for the lottery winner draw.
 * Covers US 02.05.01.
 */
class LotteryDrawTest {
    @Test
    fun drawPicksNumWinners_whenEnoughEntrants() {
        val entrants = listOf("a", "b", "c", "d", "e")

        // US 02.05.01: drawing winners
        val winners = LotteryDraw.drawWinners(entrants, 3, Random(42))
        assertEquals(3, winners.size)
    }

    @Test
    fun drawPicksNoWinners_whenNoEntrants() {
        val entrants = emptyList<String>()
        val winners = LotteryDraw.drawWinners(entrants, 3, Random(42))
        assertEquals(0, winners.size)
    }

    // winners are all different and all come from the waitlist
    @Test
    fun drawPicksDistinctWaitlistEntrants_whenEnoughEntrants() {
        val entrants = listOf("a", "b", "c", "d", "e")
        val winners = LotteryDraw.drawWinners(entrants, 3, Random(42))
        assertEquals(winners.size, winners.toSet().size)
        assertTrue(entrants.containsAll(winners))
    }

    //fewer entrants than slots: everyone wins
    @Test
    fun drawPicksAllEntrants_whenNotEnoughWinners() {
        val entrants = listOf("a", "b", "c")
        val winners = LotteryDraw.drawWinners(entrants, 5, Random(42))
        assertEquals(entrants.toSet(), winners.toSet())
    }

    //numWinners of 0, and a negative numWinners: empty result, no crash.
    @Test
    fun drawPicksNoWinners_whenZeroWinners() {
        val entrants = listOf("a", "b", "c")
        val winners = LotteryDraw.drawWinners(entrants, 0, Random(42))
        assertEquals(0, winners.size)
    }

    @Test
    fun drawPicksNoWinners_whenNegativeWinners() {
        val entrants = listOf("a", "b", "c")
        val winners = LotteryDraw.drawWinners(entrants, -1, Random(42))
        assertEquals(0, winners.size)
    }

    @Test
    fun drawIsRepeatable_whenSameSeed() {
        val entrants = listOf("a", "b", "c", "d", "e")
        val first = LotteryDraw.drawWinners(entrants, 3, Random(42))
        val second = LotteryDraw.drawWinners(entrants, 3, Random(42))
        assertEquals(first, second)
    }

// buildReservations

    @Test
    fun buildMakesOneReservationPerWinner_withCombinedId() {
        val lottery = Lottery(id = "L1", ownerId = "O1", campsiteIds = listOf("C1"))

        // US 02.05.01: building reservations
        val reservations = LotteryDraw.buildReservations(lottery, listOf("E1", "E2"), 1000L)
        assertEquals(listOf("L1_E1", "L1_E2"), reservations.map { it.id })
    }

    @Test
    fun buildSetsCampsite_whenSingleCampsiteLottery() {
        val lottery = Lottery(id = "L1", ownerId = "O1", campsiteIds = listOf("C1"))
        val reservation = LotteryDraw.buildReservations(lottery, listOf("E1"), 1000L).single()
        assertEquals("C1", reservation.campsiteId)
    }

    @Test
    fun buildLeavesCampsiteNull_whenMultiCampsiteLottery() {
        val lottery = Lottery(id = "L1", ownerId = "O1", campsiteIds = listOf("C1", "C2"))
        val reservation = LotteryDraw.buildReservations(lottery, listOf("E1"), 1000L).single()
        assertNull(reservation.campsiteId)
    }

    @Test
    fun buildCopiesLotteryFields_andStartsPending() {
        val lottery = Lottery(id = "L1", ownerId = "O1", campsiteIds = listOf("C1"))
        val reservation = LotteryDraw.buildReservations(lottery, listOf("E1"), 1000L).single()
        assertEquals("L1", reservation.lotteryId)
        assertEquals("E1", reservation.entrantId)
        assertEquals("O1", reservation.ownerId)
        assertEquals(ReservationStatus.PENDING, reservation.status)
        assertEquals(1000L, reservation.createdAt)
        assertNull(reservation.startDate)
    }
}