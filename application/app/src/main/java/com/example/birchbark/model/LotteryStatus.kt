package com.example.birchbark.model

/** The stage a lottery is in. Computed by LotteryStatusCalculator, never stored. */
enum class LotteryStatus { SCHEDULED, OPEN, CLOSED, DRAWN }