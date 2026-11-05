package com.example.birchbark

import com.example.birchbark.model.Lottery
import com.example.birchbark.ui.owner.currentLottery
import com.example.birchbark.ui.owner.lotteryStatusText
import com.example.birchbark.util.Time
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests for the lottery line on the campsite details screen.
 * Covers US 02.04.01, the campsite details show that a lottery is open.
 */
class LotteryStatusTextTest {

    //pretend today is Nov 5, 2026
    private val now = Time.startOfDay(LocalDate.of(2026, 11, 5))

    private fun lottery(regStart: LocalDate, regLastDay: LocalDate, drawn: Boolean = false) = Lottery(
        id = "l1",
        campsiteIds = listOf("c1"),
        registrationStart = Time.startOfDay(regStart),
        registrationEnd = Time.endOfDay(regLastDay),
        bookingStart = Time.startOfDay(regLastDay.plusDays(2)),
        bookingEnd = Time.endOfDay(regLastDay.plusDays(30)),
        numWinners = 5,
        drawn = drawn
    )

    @Test
    fun noLottery_whenNone() {
        assertEquals("No lottery yet", lotteryStatusText(listOf(), now))
    }

    @Test
    fun open_showsLastDay() {
        val open = lottery(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 10))
        assertEquals("Lottery open until Nov 10, 2026", lotteryStatusText(listOf(open), now))
    }

    @Test
    fun scheduled_showsStartDay() {
        val later = lottery(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 10))
        assertEquals("Lottery opens Dec 1, 2026", lotteryStatusText(listOf(later), now))
    }

    @Test
    fun closed_whenRegistrationOverButNotDrawn() {
        val closed = lottery(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 10))
        assertEquals("Registration closed, waiting for the draw", lotteryStatusText(listOf(closed), now))
    }

    @Test
    fun drawn_whenOnlyDrawnLotteries() {
        val drawn = lottery(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 10), drawn = true)
        assertEquals("Winners drawn", lotteryStatusText(listOf(drawn), now))
    }

    @Test
    fun currentLottery_isNull_whenAllDrawn() {
        val drawn = lottery(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 10), drawn = true)
        assertNull(currentLottery(listOf(drawn), now))
    }

    @Test
    fun currentLottery_ignoresDrawnOnes() {
        val drawn = lottery(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 10), drawn = true)
        val open = lottery(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 10)).copy(id = "l2")
        assertEquals("l2", currentLottery(listOf(drawn, open), now)?.id)
    }
}