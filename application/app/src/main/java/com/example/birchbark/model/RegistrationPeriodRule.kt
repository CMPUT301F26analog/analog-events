package com.example.birchbark.model

// LLM assistance: see doc/LLM.md, Prompt #002 (Akshat)

/**
 * US 02.04.03: the registration period must end after it starts.
 * Equal start and end times are invalid (a zero-length period).
 */
object RegistrationPeriodRule : LotteryRule {
    /** Shown to the owner when this rule fails. */
    override val message = "Registration must end after it starts"

    /**
     * Checks the rule above.
     *
     * @param lottery the lottery to check.
     * @return true if [Lottery.registrationEnd] is after [Lottery.registrationStart], false otherwise.
     */
    override fun isValid(lottery: Lottery): Boolean {
        return lottery.registrationEnd > lottery.registrationStart
    }
}
