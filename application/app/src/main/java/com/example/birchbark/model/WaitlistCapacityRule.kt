package com.example.birchbark.model

// LLM assistance: see doc/LLM.md, Prompt #002 (Akshat)

/**
 * US 02.04.07: the waitlist capacity is optional.
 * null means no limit; if set, it must be at least 1.
 */
object WaitlistCapacityRule : LotteryRule {
    /** Shown to the owner when this rule fails. */
    override val message = "Waitlist capacity must be blank (no limit) or at least 1"

    /**
     * Checks the rule above.
     *
     * @param lottery the lottery to check.
     * @return true if the capacity is null or at least 1, false otherwise.
     */
    override fun isValid(lottery: Lottery): Boolean {
        return lottery.waitlistCapacity == null || lottery.waitlistCapacity > 0
    }
}
