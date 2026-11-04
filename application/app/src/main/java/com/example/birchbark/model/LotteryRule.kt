package com.example.birchbark.model

// LLM assistance: see doc/LLM.md, Prompt #002 (Akshat)

/**
 * One validation rule for a lottery's settings. Each rule is its own object
 * (e.g. [NumWinnersRule]); [LotteryValidator] runs all of them before saving.
 */
interface LotteryRule {
    /** Error message shown to the owner when this rule fails. */
    val message: String

    /**
     * Checks one lottery against this rule.
     *
     * @param lottery the lottery to check.
     * @return true if the lottery passes this rule, false otherwise.
     */
    fun isValid(lottery: Lottery): Boolean
}
