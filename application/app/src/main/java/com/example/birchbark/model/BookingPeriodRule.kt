package com.example.birchbark.model

/** US 02.04.04: booking must end after it starts, and start after registration ends. */
object BookingPeriodRule : LotteryRule {
    override val message = "Booking must end after it starts, and start after registration ends"

    override fun isValid(lottery: Lottery): Boolean {
        return lottery.bookingEnd > lottery.bookingStart &&
                lottery.bookingStart > lottery.registrationEnd
    }
}