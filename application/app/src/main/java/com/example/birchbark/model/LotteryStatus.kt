package com.example.birchbark.model
// LLM assistance: see doc/LLM.md, Prompt #002 (Akshat)
/** The stage a lottery is in. Computed by LotteryStatusCalculator, never stored. */
enum class LotteryStatus { SCHEDULED, OPEN, CLOSED, DRAWN }