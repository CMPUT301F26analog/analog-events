package com.example.birchbark.model
// LLM assistance: see doc/LLM.md, Prompt #002 (Akshat)
interface LotteryRule {
    /** Shown to the owner when the rule fails. */
    val message: String

    /** True if the lottery passes this rule. */
    fun isValid(lottery: Lottery): Boolean
}