package com.example.birchbark.model

// See #058 in LLM.md

/**
 * US 02.08.01: the notification an entrant gets when the owner rejects their
 * reservation. Pure logic so it can be unit tested.
 */
object RejectionNotice {

    /**
     * @param reservation the reservation being rejected.
     * @param campsiteName the campsite's name, or null if it isn't known.
     * @param now current time in epoch ms.
     * @return the notification to send to the entrant.
     */
    fun build(reservation: Reservation, campsiteName: String?, now: Long): AppNotification {
        val where = if (campsiteName != null) " at $campsiteName" else ""
        return AppNotification(
            id = "${reservation.id}_rejected",
            recipientId = reservation.entrantId,
            senderId = reservation.ownerId,
            type = NotificationType.RESERVATION_CANCELLED,
            title = "Reservation rejected",
            body = "The owner rejected your reservation$where.",
            lotteryId = reservation.lotteryId,
            createdAt = now,
        )
    }
}