package com.example.birchbark.model

import com.example.birchbark.util.Time

// LLM assistance: see doc/LLM.md, Prompt #002 (Akshat)

/**
 * US 02.04.04: the booking period must start after registration's last day
 * (the draw happens in between) and end on a later day than it starts.
 * Compares calendar days, the way the owner types them, not raw timestamps.
 */
object BookingPeriodRule : LotteryRule {
    /** Shown to the owner when this rule fails. */
    override val message = "Booking must start after registration's last day, and end on a later day than it starts"

    /**
     * Checks the rule above.
     *
     * @param lottery the lottery to check.
     * @return true if booking starts after registration's last day and ends on a later day, false otherwise.
     */
    override fun isValid(lottery: Lottery): Boolean {
        //compare calendar days the way the owner typed them, not raw timestamps
        val registrationLastDay = Time.lastDayOf(lottery.registrationEnd)
        val bookingFirstDay = Time.toLocalDate(lottery.bookingStart)
        val bookingLastDay = Time.lastDayOf(lottery.bookingEnd)
        return bookingFirstDay.isAfter(registrationLastDay) &&
                bookingLastDay.isAfter(bookingFirstDay)
    }
}
