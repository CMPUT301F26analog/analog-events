package com.example.birchbark.model

// LLM assistance: see doc/LLM.md, Prompt #002 (Akshat)

/**
 * US 02.04.04: the booking period must end after it starts, and start
 * after registration ends (the draw happens in between).
 */
object BookingPeriodRule : LotteryRule {
    /** Shown to the owner when this rule fails. */
    override val message = "Booking must end after it starts, and start after registration ends"

    /**
     * Checks the rule above.
     *
     * @param lottery the lottery to check.
     * @return true if booking ends after it starts and starts after registration ends, false otherwise.
     */
    override fun isValid(lottery: Lottery): Boolean {
        return lottery.bookingEnd > lottery.bookingStart &&
                lottery.bookingStart > lottery.registrationEnd
    }
}
