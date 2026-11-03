package com.example.birchbark.model

// LLM assistance: see doc/LLM.md, Prompt #002 (Akshat)

/** US 02.04.03: can entrants join right now? (Krish uses this) */
object RegistrationChecker {
    fun isOpen(lottery: Lottery, now: Long): Boolean {
        return LotteryStatusCalculator.status(lottery, now) == LotteryStatus.OPEN
    }
}