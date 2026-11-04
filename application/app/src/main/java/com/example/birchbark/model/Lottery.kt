package com.example.birchbark.model

// LLM assistance: see doc/LLM.md, Prompt #001 (Akshat)

/**
 * A lottery for one or more campsites, as stored in the Firestore
 * "lotteries" collection. Plain data only, so Firestore can save and load it;
 * the rules live in the Rule and Checker objects.
 *
 * Every field has a default so Firestore can create an empty Lottery before
 * filling it in. All dates are epoch milliseconds (use [com.example.birchbark.util.Time]
 * to convert them to and from LocalDate).
 *
 * @property id Firestore document ID. Empty until the lottery is first saved.
 * @property campsiteIds IDs of the campsites this lottery is for.
 * @property ownerId FID of the owner, i.e. their document ID in the "users" collection.
 * @property registrationStart When entrants can start joining the waitlist (inclusive).
 * @property registrationEnd When joining closes (exclusive). US 02.04.03.
 * @property bookingStart When winners can start booking. Must be after [registrationEnd]. US 02.04.04.
 * @property bookingEnd When booking closes. US 02.04.04.
 * @property numWinners How many entrants the draw picks. Must be at least 1. US 02.04.05.
 * @property waitlistCapacity Max entrants on the waitlist, or null for no limit. US 02.04.07.
 * @property drawn True once winners have been drawn; the lottery can no longer be edited.
 */
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
)
