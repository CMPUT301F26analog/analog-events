package com.example.birchbark.model

// LLM assistance: see doc/LLM.md, Prompt #002 (Akshat)

/**
 * Works out a lottery's current stage from its dates, so the status is never
 * stored and can never go stale.
 */
object LotteryStatusCalculator {
    /**
     * Gives the lottery's stage at a given moment. A drawn lottery is always
     * DRAWN, whatever its dates.
     *
     * @param lottery the lottery to check.
     * @param now the moment to check, in epoch millis (usually [com.example.birchbark.util.Time.now]).
     * @return [LotteryStatus.DRAWN] if drawn; otherwise SCHEDULED before
     *   registration starts, OPEN from registrationStart up to (not including)
     *   registrationEnd, and CLOSED after that.
     */
    fun status(lottery: Lottery, now: Long): LotteryStatus {
        return when {
            lottery.drawn -> LotteryStatus.DRAWN
            now < lottery.registrationStart -> LotteryStatus.SCHEDULED
            now < lottery.registrationEnd -> LotteryStatus.OPEN
            else -> LotteryStatus.CLOSED
        }
    }
}
