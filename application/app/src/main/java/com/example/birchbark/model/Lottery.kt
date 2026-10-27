package com.example.birchbark.model

// LLM assistance: see doc/LLM.md, Prompt #001 (Akshat)
data class Lottery(
    val id: String = "",
    val campsiteIds: List<String> = listOf(),
    val ownerId: String = "",
    val registrationStart: Long = 0L,
    val registrationEnd: Long = 0L,
    val bookingStart: Long = 0L,
    val bookingEnd: Long = 0L,
    val numWinners: Int = 0,
    val waitlistCapacity: Int? = null,
    val drawn: Boolean = false,
){

    // ---------- Validation: one per story ----------

    /** US 02.04.03: registration must end after it starts. */
    fun isRegistrationPeriodValid(): Boolean {
        if (registrationEnd <= registrationStart) {
            return false
        } else {
            return true
        }
    }

    /** US 02.04.04: booking must end after it starts, and start after registration ends. */
    fun isBookingPeriodValid(): Boolean {
        if (bookingEnd <= bookingStart) {
            return false
        } else if (bookingStart <= registrationEnd) {
            return false
        } else {
            return true
        }
    }

    /** US 02.04.05: at least one winner. */
    fun isNumWinnersValid(): Boolean {
        if (numWinners <= 0) {
            return false
        } else {
            return true
        }
    }

    /** US 02.04.07: no limit (null) is fine, otherwise must be positive. */
    fun isWaitlistCapacityValid(): Boolean {
        if (waitlistCapacity == null) {
            return true
        } else if (waitlistCapacity <= 0) {
            return false
        } else {
            return true
        }
    }

    /** Runs every check. Empty list = valid, safe to save. */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()

        if (!isRegistrationPeriodValid()) {
            errors.add("Registration must end after it starts")
        }
        if (!isBookingPeriodValid()) {
            errors.add("Booking must end after it starts, and start after registration ends")
        }
        if (!isNumWinnersValid()) {
            errors.add("Number of winners must be at least 1")
        }
        if (!isWaitlistCapacityValid()) {
            errors.add("Waitlist capacity must be blank (no limit) or at least 1")
        }

        return errors
    }

    // ---------- State: used by teammates ----------

    /** Current state, computed from dates so it never goes stale. */
    fun status(now: Long): LotteryStatus {
        return when {
            drawn -> LotteryStatus.DRAWN
            now < registrationStart -> LotteryStatus.SCHEDULED
            now < registrationEnd -> LotteryStatus.OPEN
            else -> LotteryStatus.CLOSED
        }
    }

    /** US 02.04.03: can entrants join right now? (Krish uses this) */
    fun isRegistrationOpen(now: Long): Boolean {
        return status(now) == LotteryStatus.OPEN
    }

    /** US 02.04.07: is the waitlist full? (Krish uses this) */
    fun isWaitlistFull(currentSize: Int): Boolean {
        if (waitlistCapacity == null) {
            return false
        } else if (currentSize >= waitlistCapacity) {
            return true
        } else {
            return false
        }
    }

    /** US 02.04.06: settings can't change after the draw. */
    fun canEdit(): Boolean {
        return !drawn
    }
}


enum class LotteryStatus { SCHEDULED, OPEN, CLOSED, DRAWN }