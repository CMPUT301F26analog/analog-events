package com.example.birchbark.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import com.example.birchbark.util.Time
import java.time.LocalDate

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
    // Dates are built the same way the lottery form builds them.

    private val regLastDay = LocalDate.of(2026, 11, 18)

    private fun bookingLottery(firstDay: LocalDate, lastDay: LocalDate) = Lottery(
        registrationStart = Time.startOfDay(regLastDay.minusDays(3)),
        registrationEnd = Time.endOfDay(regLastDay),
        bookingStart = Time.startOfDay(firstDay),
        bookingEnd = Time.endOfDay(lastDay)
    )

    @Test
    fun bookingValid_whenStartsDayAfterRegistrationEnds() {
        assertTrue(BookingPeriodRule.isValid(bookingLottery(regLastDay.plusDays(1), regLastDay.plusDays(3))))
    }

    @Test
    fun bookingInvalid_whenStartsOnRegistrationLastDay() {
        assertFalse(BookingPeriodRule.isValid(bookingLottery(regLastDay, regLastDay.plusDays(3))))
    }

    @Test
    fun bookingInvalid_whenStartsDuringRegistration() {
        assertFalse(BookingPeriodRule.isValid(bookingLottery(regLastDay.minusDays(2), regLastDay.plusDays(3))))
    }

    @Test
    fun bookingInvalid_whenStartAndEndSameDay() {
        val day = regLastDay.plusDays(2)
        assertFalse(BookingPeriodRule.isValid(bookingLottery(day, day)))
    }

    @Test
    fun bookingInvalid_whenEndBeforeStart() {
        assertFalse(BookingPeriodRule.isValid(bookingLottery(regLastDay.plusDays(4), regLastDay.plusDays(2))))
    }

    @Test
    fun bookingInvalid_whenDatesNotSet() {
        assertFalse(BookingPeriodRule.isValid(Lottery()))
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