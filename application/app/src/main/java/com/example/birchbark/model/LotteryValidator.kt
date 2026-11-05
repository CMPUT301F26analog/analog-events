package com.example.birchbark.model

// LLM assistance: see doc/LLM.md, Prompt #002 (Akshat)

/**
 * Runs every [LotteryRule] against a lottery. Call this before saving.
 * To add a rule, create a new LotteryRule object and add it to [rules].
 */
object LotteryValidator {
    /** All rules a lottery must pass before it can be saved. */
    private val rules = listOf(
        RegistrationPeriodRule,
        BookingPeriodRule,
        NumWinnersRule,
        WaitlistCapacityRule
    )

    /**
     * Checks a lottery against every rule.
     *
     * @param lottery the lottery to check.
     * @return the message of every rule that failed, so the owner sees all
     *   problems at once. An empty list means the lottery is valid and safe to save.
     */
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
