package com.example.birchbark.model

// LLM assistance: see doc/LLM.md, Prompt #002 (Akshat)

/**
 * The stage a lottery is in. Computed by [LotteryStatusCalculator] from the
 * lottery's dates and [Lottery.drawn]; never stored in Firestore.
 */
enum class LotteryStatus {
    /** Registration has not started yet. */
    SCHEDULED,

    /** Registration is open; entrants can join the waitlist. */
    OPEN,

    /** Registration has ended, but winners have not been drawn yet. */
    CLOSED,

    /** Winners have been drawn. */
    DRAWN
}
