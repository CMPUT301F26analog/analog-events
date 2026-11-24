package com.example.birchbark

import com.example.birchbark.model.Lottery
import com.example.birchbark.ui.entrant.lotteryMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// See #052 in LLM.md
/**
 * Unit tests for the message shown when an entrant can't join a lottery.
 * Covers US 01.01.06.
 */
class LotteryMessageTest {

    private val lottery = Lottery(id = "L1", registrationStart = 100L, registrationEnd = 200L)

    // US 01.01.06: why there's no join button

    @Test
    fun noLottery_saysNoLottery() {
        assertEquals("No lottery for this campsite right now.", lotteryMessage(null, 150L))
    }

    @Test
    fun scheduledLottery_saysWhenRegistrationOpens() {
        assertTrue(lotteryMessage(lottery, 50L)!!.startsWith("Registration opens"))
    }

    @Test
    fun openLottery_hasNoMessage() {
        assertNull(lotteryMessage(lottery, 150L))
    }

    @Test
    fun closedLottery_saysWaitingForDraw() {
        assertEquals("Registration has closed. Winners will be drawn soon.", lotteryMessage(lottery, 250L))
    }

    @Test
    fun drawnLottery_saysWinnersDrawn() {
        assertEquals("Winners have been drawn for this lottery.", lotteryMessage(lottery.copy(drawn = true), 250L))
    }
}
