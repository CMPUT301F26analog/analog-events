package com.example.birchbark.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// LLM assistance: see doc/LLM.md, Prompt #001 (Akshat)
/**
 * Unit tests for the lottery rules and checkers.
 * Covers US 02.04.03, 02.04.04, 02.04.05, 02.04.06, 02.04.07.
 */
class LotteryTest {

    // US 02.04.03: registration period

    @Test
    fun registrationValid_whenEndAfterStart() {
        val lottery = Lottery(registrationStart = 100L, registrationEnd = 200L)
        assertTrue(RegistrationPeriodRule.isValid(lottery))
    }

    @Test
    fun registrationInvalid_whenEndBeforeStart() {
        val lottery = Lottery(registrationStart = 200L, registrationEnd = 100L)
        assertFalse(RegistrationPeriodRule.isValid(lottery))
    }

    @Test
    fun registrationInvalid_whenEndEqualsStart() {
        val lottery = Lottery(registrationStart = 100L, registrationEnd = 100L)
        assertFalse(RegistrationPeriodRule.isValid(lottery))
    }

    // US 02.04.04: booking period

    @Test
    fun bookingValid_whenAfterRegistration() {
        val lottery = Lottery(
            registrationStart = 100L, registrationEnd = 200L,
            bookingStart = 300L, bookingEnd = 400L
        )
        assertTrue(BookingPeriodRule.isValid(lottery))
    }

    @Test
    fun bookingInvalid_whenStartsDuringRegistration() {
        val lottery = Lottery(
            registrationStart = 100L, registrationEnd = 200L,
            bookingStart = 150L, bookingEnd = 400L
        )
        assertFalse(BookingPeriodRule.isValid(lottery))
    }

    @Test
    fun bookingInvalid_whenEndBeforeStart() {
        val lottery = Lottery(
            registrationStart = 100L, registrationEnd = 200L,
            bookingStart = 400L, bookingEnd = 300L
        )
        assertFalse(BookingPeriodRule.isValid(lottery))
    }

    @Test
    fun bookingInvalid_whenDatesNotSet() {
        val lottery = Lottery()
        assertFalse(BookingPeriodRule.isValid(lottery))
    }

    // US 02.04.05: number of winners

    @Test
    fun numWinnersValid_whenPositive() {
        val lottery = Lottery(numWinners = 1)
        assertTrue(NumWinnersRule.isValid(lottery))
    }

    @Test
    fun numWinnersInvalid_whenZero() {
        val lottery = Lottery(numWinners = 0)
        assertFalse(NumWinnersRule.isValid(lottery))
    }

    // US 02.04.07: waitlist capacity

    @Test
    fun waitlistCapacityValid_whenNull() {
        val lottery = Lottery(waitlistCapacity = null)
        assertTrue(WaitlistCapacityRule.isValid(lottery))
    }

    @Test
    fun waitlistCapacityValid_whenPositive() {
        val lottery = Lottery(waitlistCapacity = 1)
        assertTrue(WaitlistCapacityRule.isValid(lottery))
    }

    @Test
    fun waitlistCapacityInvalid_whenZero() {
        val lottery = Lottery(waitlistCapacity = 0)
        assertFalse(WaitlistCapacityRule.isValid(lottery))
    }

    @Test
    fun waitlistNeverFull_whenNoCapacity() {
        val lottery = Lottery(waitlistCapacity = null)
        assertFalse(WaitlistChecker.isFull(lottery, 1000))
    }

    @Test
    fun waitlistFull_whenSizeReachesCapacity() {
        val lottery = Lottery(waitlistCapacity = 20)
        assertTrue(WaitlistChecker.isFull(lottery, 20))
    }

    @Test
    fun waitlistNotFull_whenSizeBelowCapacity() {
        val lottery = Lottery(waitlistCapacity = 20)
        assertFalse(WaitlistChecker.isFull(lottery, 19))
    }

    // US 02.04.06: update lottery listing

    @Test
    fun canEdit_whenNotDrawn() {
        val lottery = Lottery(drawn = false)
        assertTrue(EditPermission.canEdit(lottery))
    }

    @Test
    fun cannotEdit_whenDrawn() {
        val lottery = Lottery(drawn = true)
        assertFalse(EditPermission.canEdit(lottery))
    }

    // Status (used by US 02.04.03 registration checks)

    @Test
    fun status_isScheduled_beforeRegistrationStarts() {
        val lottery = Lottery(registrationStart = 100L, registrationEnd = 200L)
        assertEquals(LotteryStatus.SCHEDULED, LotteryStatusCalculator.status(lottery, 50L))
    }

    @Test
    fun status_isOpen_atRegistrationStart() {
        val lottery = Lottery(registrationStart = 100L, registrationEnd = 200L)
        assertEquals(LotteryStatus.OPEN, LotteryStatusCalculator.status(lottery, 100L))
    }

    @Test
    fun status_isClosed_atRegistrationEnd() {
        val lottery = Lottery(registrationStart = 100L, registrationEnd = 200L)
        assertEquals(LotteryStatus.CLOSED, LotteryStatusCalculator.status(lottery, 200L))
    }

    @Test
    fun status_isDrawn_evenDuringRegistration() {
        val lottery = Lottery(registrationStart = 100L, registrationEnd = 200L, drawn = true)
        assertEquals(LotteryStatus.DRAWN, LotteryStatusCalculator.status(lottery, 150L))
    }
}