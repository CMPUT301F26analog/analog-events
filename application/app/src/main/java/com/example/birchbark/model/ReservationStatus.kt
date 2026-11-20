package com.example.birchbark.model

// LLM assistance: see doc/LLM.md, Prompt #043 (Akshat)

/**
 * US 02.05.01: Where a reservation is in its lifecycle. Stored in Firestore as the
 * constant's name (e.g. "PENDING"), so renaming a value breaks existing data.
 */
enum class ReservationStatus {
    /** Entrant won the draw but hasn't accepted or declined yet. */
    PENDING,

    /** Entrant accepted the reservation and the campsite rules. US 02.11.01. */
    CONFIRMED,

    /** Entrant turned down the reservation after winning. US 01.04.03. */
    DECLINED,

    /** Entrant cancelled after confirming. US 01.07.01. */
    CANCELLED,

    /** Owner rejected the reservation. US 02.08.01. */
    REJECTED,
}