package com.example.birchbark.model

/** US 02.04.03: registration must end after it starts. */
object RegistrationPeriodRule : LotteryRule {
    override val message = "Registration must end after it starts"

    override fun isValid(lottery: Lottery): Boolean {
        return lottery.registrationEnd > lottery.registrationStart
    }
}