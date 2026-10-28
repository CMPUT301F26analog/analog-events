package com.example.birchbark.model

/**
 * A lottery for one or more campsites. Plain data only, so Firestore
 * can save and load it; the rules live in the Rule and Checker classes.
 */

// LLM assistance: see doc/LLM.md, Prompt #001 (Akshat)
data class Lottery(
    val id: String = "",
    val campsiteIds: List<String> = listOf(),
    /** FID of the owner, i.e. their document ID in the "users" collection. */
    val ownerId: String = "",
    val registrationStart: Long = 0L,
    val registrationEnd: Long = 0L,
    val bookingStart: Long = 0L,
    val bookingEnd: Long = 0L,
    val numWinners: Int = 0,
    val waitlistCapacity: Int? = null,
    val drawn: Boolean = false,
)

