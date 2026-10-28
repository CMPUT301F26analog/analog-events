package com.example.birchbark.model

/** Runs every LotteryRule. Empty list = valid, safe to save. */
object LotteryValidator {
    private val rules = listOf(
        RegistrationPeriodRule,
        BookingPeriodRule,
        NumWinnersRule,
        WaitlistCapacityRule
    )

    fun validate(lottery: Lottery): List<String> {
        val errors = mutableListOf<String>()
        for (rule in rules) {
            if (!rule.isValid(lottery)) {
                errors.add(rule.message)
            }
        }
        return errors
    }
}