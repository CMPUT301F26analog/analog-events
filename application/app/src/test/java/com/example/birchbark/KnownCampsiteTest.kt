package com.example.birchbark

import com.example.birchbark.model.Lottery
import com.example.birchbark.model.Reservation
import com.example.birchbark.ui.entrant.knownCampsiteId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for [knownCampsiteId]. Covers US 01.04.02, when a lottery winner
 * has to pick a campsite and when they don't.
 */
class KnownCampsiteTest {

    @Test
    fun reservationWithCampsite_usesIt() {
        val lottery = Lottery(campsiteIds = listOf("C1", "C2"))
        assertEquals("C2", knownCampsiteId(Reservation(campsiteId = "C2"), lottery))
    }

    @Test
    fun oneCampsiteLottery_usesThatCampsite() {
        assertEquals("C1", knownCampsiteId(Reservation(), Lottery(campsiteIds = listOf("C1"))))
    }

    @Test
    fun multiCampsiteLottery_winnerHasToPick() {
        assertNull(knownCampsiteId(Reservation(), Lottery(campsiteIds = listOf("C1", "C2"))))
    }
}