package com.example.birchbark.model

/** Works out a lottery's current stage from its dates, so it never goes stale. */
object LotteryStatusCalculator {
    fun status(lottery: Lottery, now: Long): LotteryStatus {
        return when {
            lottery.drawn -> LotteryStatus.DRAWN
            now < lottery.registrationStart -> LotteryStatus.SCHEDULED
            now < lottery.registrationEnd -> LotteryStatus.OPEN
            else -> LotteryStatus.CLOSED
        }
    }
}