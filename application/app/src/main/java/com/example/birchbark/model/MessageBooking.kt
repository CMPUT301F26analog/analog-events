package com.example.birchbark.model

/**
 * US 01.14.01 and US 02.12.01
 *
 * A booking the participant can select in the composer.
 *
 * @property reservation confirmed stay used to identify the thread and its participants.
 * @property siteName display name of the selected campsite.
 */
data class MessageBooking(val reservation: Reservation, val siteName: String) {
    // Reservation document ID shared by both participants for this thread.
    val id get() = reservation.id

    // Returns the recipient label for the participant composing from [side].
    fun label(side: MessageSide) = if (side == MessageSide.OWNER)
        reservation.entrantName.orEmpty().ifBlank { "Entrant" } else "$siteName management"
}
