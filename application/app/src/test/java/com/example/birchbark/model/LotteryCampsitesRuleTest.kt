package com.example.birchbark.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [LotteryCampsitesRule] and that [LotteryValidator] uses it.
 * Covers US 02.04.02 (multi-campsite lottery), a lottery needs at least one
 * campsite but can have more than one.
 */
class LotteryCampsitesRuleTest {

    @Test
    fun oneCampsite_isValid() {
        assertTrue(LotteryCampsitesRule.isValid(Lottery(campsiteIds = listOf("whistlers"))))
    }

    @Test
    fun severalCampsites_isValid() {
        val lottery = Lottery(campsiteIds = listOf("whistlers", "wapiti", "pocahontas"))
        assertTrue(LotteryCampsitesRule.isValid(lottery))
    }

    @Test
    fun noCampsites_isNotValid() {
        assertFalse(LotteryCampsitesRule.isValid(Lottery(campsiteIds = listOf())))
    }

    @Test
    fun validator_showsMessage_whenNoCampsites() {
        val errors = LotteryValidator.validate(Lottery(campsiteIds = listOf()))
        assertTrue(errors.contains("Pick at least one campsite for the lottery"))
    }

    @Test
    fun validator_noCampsiteMessage_whenCampsitePicked() {
        val errors = LotteryValidator.validate(Lottery(campsiteIds = listOf("whistlers")))
        assertFalse(errors.contains("Pick at least one campsite for the lottery"))
    }
}