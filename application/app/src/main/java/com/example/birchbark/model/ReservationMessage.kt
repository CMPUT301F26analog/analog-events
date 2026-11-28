package com.example.birchbark.model

/**
 * US 01.14.01 and US 02.12.01
 *
 * One immutable entry in a reservation conversation.
 *
 * @property id message document ID within its thread.
 * @property sender FID of the participant who sent the message.
 * @property recipient FID of the other participant in the booking.
 * @property body saved message text.
 * @property sentAt server-assigned send time in epoch milliseconds.
 * @property ownerSent true for a message sent as the selected campsite's owner.
 */
data class ReservationMessage(
    val id: String, val sender: String, val recipient: String,
    val body: String, val sentAt: Long, val ownerSent: Boolean
)