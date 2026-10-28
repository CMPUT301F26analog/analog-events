package com.example.birchbark.model

/** US 02.04.07: no limit (null) is fine, otherwise must be positive. */
object WaitlistCapacityRule : LotteryRule {
    override val message = "Waitlist capacity must be blank (no limit) or at least 1"

    override fun isValid(lottery: Lottery): Boolean {
        return lottery.waitlistCapacity == null || lottery.waitlistCapacity > 0
    }
}