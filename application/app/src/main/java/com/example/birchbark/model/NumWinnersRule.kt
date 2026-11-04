package com.example.birchbark.model

// LLM assistance: see doc/LLM.md, Prompt #002 (Akshat)

/**
 * US 02.04.05: the lottery must have at least one winner.
 */
object NumWinnersRule : LotteryRule {
    /** Shown to the owner when this rule fails. */
    override val message = "Number of winners must be at least 1"

    /**
     * Checks the rule above.
     *
     * @param lottery the lottery to check.
     * @return true if [Lottery.numWinners] is 1 or more, false otherwise.
     */
    override fun isValid(lottery: Lottery): Boolean {
        return lottery.numWinners > 0
    }
}
