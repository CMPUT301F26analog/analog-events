package com.example.birchbark.model

/**
 * US 01.14.01 and US 02.12.01
 *
 * Inbox row for one reservation.
 *
 * @property id reservation ID and conversation document ID.
 * @property siteName name used to identify campsite management to the entrant.
 * @property entrantName name supplied when the entrant confirmed the booking.
 * @property preview text of the latest message, shortened for display by the inbox.
 * @property sentAt time of the latest message in epoch milliseconds.
 * @property lastId document ID of the latest message.
 * @property lastSender FID of the participant who sent the latest message.
 * @property seenId ID last marked as read by the viewer's side, or null before the first receipt.
 */
data class ConversationSummary(
    val id: String, val siteName: String, val entrantName: String, val preview: String,
    val sentAt: Long, val lastId: String, val lastSender: String, val seenId: String?
) {
    /** Returns true when [fid] received the latest message and has yet to mark it as seen. */
    fun unread(fid: String) = fid != lastSender && lastId != seenId
    /** Returns the counterpart shown in the inbox for [side]. */
    fun label(side: MessageSide) = if (side == MessageSide.OWNER) entrantName.ifBlank { "Entrant" } else "$siteName management"
}