package com.example.birchbark.model

// See #049 in LLM.md

/** The three lists on the owner's reservations screen. */
enum class OwnerReservationTab {
    /** Not finished yet: waiting for the entrant, or confirmed and not over. US 02.07.01. */
    ACTIVE,

    /** Confirmed and the stay is over. US 02.07.02. */
    PAST,

    /** Cancelled or declined by the entrant, or rejected by the owner. US 02.07.03. */
    CANCELLED,
}

/**
 * US 02.07.01, 02.07.02, 02.07.03: sorts an owner's reservations into the
 * active, past and cancelled lists. Pure logic with no Firestore, so it can be
 * unit tested.
 */
object OwnerReservationTabs {

    /**
     * Decides which list a reservation belongs in.
     *
     * @param reservation the reservation to place.
     * @param now current time in epoch ms.
     * @return [OwnerReservationTab.CANCELLED] if it won't happen,
     *   [OwnerReservationTab.PAST] if its stay has ended, otherwise
     *   [OwnerReservationTab.ACTIVE] (including ones without dates yet).
     */
    fun tabOf(reservation: Reservation, now: Long): OwnerReservationTab {
        return when (reservation.status) {
            ReservationStatus.CANCELLED,
            ReservationStatus.DECLINED,
            ReservationStatus.REJECTED -> OwnerReservationTab.CANCELLED

            ReservationStatus.PENDING,
            ReservationStatus.CONFIRMED -> {
                //endDate is exclusive (the moment the last night ends), like Lottery's end times
                val end = reservation.endDate
                if (end != null && now >= end) OwnerReservationTab.PAST else OwnerReservationTab.ACTIVE
            }
        }
    }

    /**
     * The reservations that belong in one list, keeping their order.
     *
     * @param reservations every reservation for the owner's campsites.
     * @param tab the list to show.
     * @param now current time in epoch ms.
     */
    fun forTab(reservations: List<Reservation>, tab: OwnerReservationTab, now: Long): List<Reservation> {
        return reservations.filter { tabOf(it, now) == tab }
    }
}
