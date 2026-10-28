package com.example.birchbark.model

/** US 02.04.07: is the waitlist full? (Krish uses this) */
object WaitlistChecker {
    fun isFull(lottery: Lottery, currentSize: Int): Boolean {
        if (lottery.waitlistCapacity == null) {
            return false
        } else if (currentSize >= lottery.waitlistCapacity) {
            return true
        } else {
            return false
        }
    }
}