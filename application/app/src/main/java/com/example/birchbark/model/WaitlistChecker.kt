package com.example.birchbark.model

// LLM assistance: see doc/LLM.md, Prompt #002 (Akshat)

/**
 * US 02.04.07: tells the join-waitlist code whether a lottery's waitlist
 * has room for another entrant.
 */
object WaitlistChecker {
    /**
     * Checks whether the waitlist has reached its capacity.
     *
     * @param lottery the lottery whose capacity to use.
     * @param currentSize how many entrants are on the waitlist right now.
     * @return false if the lottery has no capacity limit (null); otherwise
     *   true when [currentSize] is at or above the capacity.
     */
    fun isFull(lottery: Lottery, currentSize: Int): Boolean {
        if (lottery.waitlistCapacity == null) {
            return false
        } else if (currentSize >= lottery.waitlistCapacity) {
            return true
        } else {
            return false
        }
    }
}
