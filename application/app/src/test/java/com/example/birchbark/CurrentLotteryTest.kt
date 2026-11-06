package com.example.birchbark.model

// LLM assistance: see doc/LLM.md, Prompt #009 (Akshat)

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Unit tests for CurrentLottery. Covers US 02.04.06. */
class CurrentLotteryTest {

    @Test
    fun pick_isNull_whenNoLotteries() {
        assertNull(CurrentLottery.pick(emptyList()))
    }

    @Test
    fun pick_isNull_whenAllDrawn() {
        val drawn = Lottery(id = "a", registrationStart = 100L, drawn = true)
        assertNull(CurrentLottery.pick(listOf(drawn)))
    }

    @Test
    fun pick_ignoresDrawnLotteries() {
        val drawn = Lottery(id = "old", registrationStart = 500L, drawn = true)
        val open = Lottery(id = "new", registrationStart = 100L)
        assertEquals("new", CurrentLottery.pick(listOf(drawn, open))?.id)
    }

    @Test
    fun pick_choosesLatestStart_whenSeveralUndrawn() {
        val earlier = Lottery(id = "earlier", registrationStart = 100L)
        val later = Lottery(id = "later", registrationStart = 200L)
        assertEquals("later", CurrentLottery.pick(listOf(earlier, later))?.id)
    }
}