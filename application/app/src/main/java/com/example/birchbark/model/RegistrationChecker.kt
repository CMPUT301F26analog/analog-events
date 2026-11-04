package com.example.birchbark.model

// LLM assistance: see doc/LLM.md, Prompt #002 (Akshat)

/**
 * US 02.04.03: tells the join-waitlist code whether entrants can join a
 * lottery right now.
 */
object RegistrationChecker {
    /**
     * Checks whether registration is open at a given moment.
     *
     * @param lottery the lottery to check.
     * @param now the moment to check, in epoch millis.
     * @return true only if the lottery's status at [now] is [LotteryStatus.OPEN].
     */
    fun isOpen(lottery: Lottery, now: Long): Boolean {
        return LotteryStatusCalculator.status(lottery, now) == LotteryStatus.OPEN
    }
}
