package com.example.birchbark.model

/** US 02.04.05: at least one winner. */
object NumWinnersRule : LotteryRule {
    override val message = "Number of winners must be at least 1"

    override fun isValid(lottery: Lottery): Boolean {
        return lottery.numWinners > 0
    }
}