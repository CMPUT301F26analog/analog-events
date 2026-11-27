package com.example.birchbark.model

// See #051 in LLM.md

/**
 * US 01.04.01, 02.06.01: the dates one confirmed reservation takes up at a
 * campsite, as stored in the Firestore "campsiteBookings" collection.
 *
 * Kept apart from [Reservation] on purpose: every user can read bookings so
 * entrants can see which dates are still free, but a booking holds no names
 * or contact details, so nobody sees who booked.
 *
 * @property id Firestore document ID, the same as the reservation's [Reservation.id].
 * @property campsiteId the campsite that is booked.
 * @property startDate first day of the stay (epoch ms, start of that day).
 * @property endDate last day of the stay (epoch ms, end of that day).
 * @property reservationIds the confirmed reservations on this night. A
 *   campground holds up to [Campsite.maxOccupancy] parties a night, so the
 *   night is full once there are that many (Riley's PR feedback). See #091 in LLM.md.
 */
data class CampsiteBooking(
    val id: String = "",
    val campsiteId: String = "",
    val startDate: Long = 0L,
    val endDate: Long = 0L,
    val reservationIds: List<String> = listOf(),
)
