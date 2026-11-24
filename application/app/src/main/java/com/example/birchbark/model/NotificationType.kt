package com.example.birchbark.model

// See #048 in LLM.mdgit status

/**
 * US 01.03.01, 01.03.02, 01.05.01, 01.07.02, 02.10.01, 02.12.01: what a
 * notification is about. Stored in Firestore as the constant's name
 * (e.g. "LOTTERY_WON"), so renaming a value breaks existing data and the
 * Firestore rules, which check these names.
 */
enum class NotificationType {
    /** Entrant won a lottery. US 01.03.01. */
    LOTTERY_WON,

    /** Entrant lost a lottery. US 01.03.02. */
    LOTTERY_LOST,

    /** Owner messaged everyone on a lottery's waitlist. US 02.10.01. */
    WAITLIST_MESSAGE,

    /** Entrant confirmed a reservation. US 01.05.01. */
    RESERVATION_CONFIRMED,

    /** Entrant cancelled a reservation. US 01.07.02. */
    RESERVATION_CANCELLED,

    /** Owner messaged one entrant with a reservation. US 02.12.01. */
    OWNER_MESSAGE,
}
