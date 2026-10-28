package com.example.birchbark.model

interface LotteryRule {
    /** Shown to the owner when the rule fails. */
    val message: String

    /** True if the lottery passes this rule. */
    fun isValid(lottery: Lottery): Boolean
}