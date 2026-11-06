package com.example.birchbark.model

// LLM assistance: see doc/LLM.md, Prompt #009 (Akshat)

/**
 * US 02.04.06: decides which of a campsite's lotteries is the current one.
 */
object CurrentLottery {
    /**
     * Picks the lottery that hasn't been drawn yet. If there are several, the one
     * that starts latest wins (same rule as the owner details screen).
     *
     * @param lotteries every lottery that includes the campsite.
     * @return the current lottery, or null if there are none or all are drawn.
     */
    fun pick(lotteries: List<Lottery>): Lottery? {
        return lotteries.filter { !it.drawn }.maxByOrNull { it.registrationStart }
    }
}