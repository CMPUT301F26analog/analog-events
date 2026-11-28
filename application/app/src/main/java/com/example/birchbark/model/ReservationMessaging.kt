package com.example.birchbark.model

/**
 * US 01.14.01 and US 02.12.01
 *
 * Identifies the participant's side in this booking.
 * A user with both roles can enter a different inbox from each role's navigation.
 *
 * @Note See #[RW-2-LLM] in LLM.md
 *.
 */

// Handle for a live message or booking query.
fun interface MessageSubscription {
    // Detaches the listener when the observing screen leaves composition.
    fun cancel()
}
// Receives a validation, authorization or storage failure from an asynchronous operation.
typealias MessageFailure = (Exception) -> Unit

/**
 * Participant operations shared by entrant and owner screens.
 * [MessageSubscription] lets the caller release a listener without depending on Firestore.
 */
interface ReservationMessaging {
    /**
     * Loads active, confirmed bookings for the recipient picker.
     *
     * @param fid FID of the current participant.
     * @param side participant role whose bookings to load.
     * @param loaded receives eligible bookings with their selected campsite names.
     * @param failed receives booking or campsite lookup failures.
     */
    fun bookings(fid: String, side: MessageSide, loaded: (List<MessageBooking>) -> Unit, failed: MessageFailure)
    /**
     * Observes the conversations for one side, newest activity first.
     *
     * @param fid FID used to scope the inbox query.
     * @param side role selected in navigation.
     * @param loaded receives the current inbox after each change.
     * @param failed receives listener failures.
     * @return a subscription that detaches the listener.
     */
    fun inbox(fid: String, side: MessageSide, loaded: (List<ConversationSummary>) -> Unit, failed: MessageFailure): MessageSubscription
    /**
     * Observes the saved entries in a thread, oldest first.
     *
     * @param id reservation ID identifying the conversation.
     * @param loaded receives the ordered history after each change.
     * @param failed receives listener failures.
     * @return a subscription that detaches the listener.
     */
    fun history(id: String, loaded: (List<ReservationMessage>) -> Unit, failed: MessageFailure): MessageSubscription
    /**
     * Observes the reservation so checkout or a change of status can close the composer.
     *
     * @param id reservation document ID.
     * @param loaded receives the current reservation, or null for a missing document when readable.
     * @param failed receives listener failures, including loss of access.
     * @return a subscription that detaches the listener.
     */
    fun booking(id: String, loaded: (Reservation?) -> Unit, failed: MessageFailure): MessageSubscription
    /**
     * Records a read receipt only while the displayed message is still the latest.
     *
     * @param id reservation ID identifying the thread.
     * @param side participant whose receipt should change.
     * @param messageId latest incoming message actually shown to the viewer.
     * @param failed receives transaction failures.
     */
    fun markSeen(id: String, side: MessageSide, messageId: String, failed: MessageFailure)
    /**
     * Saves a message and its inbox summary together for the selected booking.
     *
     * @param id reservation ID from which the recipient is resolved.
     * @param side role in which the sender opened the conversation.
     * @param fid sender’s installation FID.
     * @param messageId stable ID reused when retrying the same send.
     * @param body draft text, trimmed before saving.
     * @param sent called after Firestore confirms the send or its identical retry.
     * @param failed receives validation or transaction failures.
     */
    fun send(id: String, side: MessageSide, fid: String, messageId: String, body: String, sent: () -> Unit, failed: MessageFailure)
}

/**
 * Read-only access to owner-authored messages for the admin log.
 **/
interface OwnerMessageLogReader {
    /**
     * Observes owner-to-entrant messages across reservations, newest first.
     *
     * @param loaded receives the ordered owner-authored entries.
     * @param failed receives listener failures.
     * @return a subscription that detaches the listener.
     */
    fun ownerLog(loaded: (List<ReservationMessage>) -> Unit, failed: MessageFailure): MessageSubscription
}