package com.example.birchbark

// The test scenarios for this planner were developed with LLM assistance.


/**
 * Profile data needed to validate an owner before planning removal.
 *
 * @property fid The profile's document ID in the users collection.
 * @property authUid The Firebase Authentication UID linked to this profile.
 * @property roles Roles assigned to the profile.
 * @property exists Whether the profile document exists.
 */
data class RemovalProfile(
    val fid: String,
    val authUid: String,
    val roles: List<String>,
    val exists: Boolean = true
)

/**
 * Identifies a campsite or lottery document and its owner.
 *
 * @property id The document ID.
 * @property ownerId The FID of the document's owner.
 */
data class OwnerDocument(val id: String, val ownerId: String)

/**
 * Reservation fields used to decide whether a reservation should be cancelled
 * and whether its entrant should receive a notification.
 *
 * @property id Reservation document ID.
 * @property ownerId FID of the reservation's owner, if present.
 * @property campsiteId ID of the campsite, if present.
 * @property status Current reservation status.
 * @property entrantId FID of the entrant, if present.
 */
data class ReservationRecord(
    val id: String,
    val ownerId: String?,
    val campsiteId: String?,
    val status: String,
    val entrantId: String?
)

/**
 * Identifies a child record associated with a campsite or lottery.
 *
 * @property id Child document ID.
 * @property parentId ID of the campsite or lottery containing the record.
 */
data class RelatedRecord(val id: String, val parentId: String)

/**
 * All profile and related-record data required to calculate an owner-removal
 * plan without accessing Firebase.
 *
 * @property profile Profile being validated and removed.
 * @property authLinkFid FID stored in the owner's auth-link document, if any.
 * @property campsites Campsite documents to inspect for ownership.
 * @property lotteries Lottery documents to inspect for ownership.
 * @property reservations Reservations to inspect for cancellation.
 * @property campsiteBookings Booking records to inspect for cleanup.
 * @property waitlistEntries Waitlist records to inspect for cleanup.
 * @property optedOutEntrantFids Entrant FIDs that must not be notified.
 */
data class OwnerRemovalInput(
    val profile: RemovalProfile,
    val authLinkFid: String?,
    val campsites: List<OwnerDocument>,
    val lotteries: List<OwnerDocument>,
    val reservations: List<ReservationRecord>,
    val campsiteBookings: List<RelatedRecord>,
    val waitlistEntries: List<RelatedRecord>,
    val optedOutEntrantFids: Set<String>
)

/**
 * The document IDs and recipient FIDs selected for a violating-owner removal.
 * The caller performs these actions; creating this value does not access or
 * modify Firebase.
 *
 * @property blockedAuthUid Auth UID used for the blocked-owner record.
 * @property blockedFid Profile FID used for the blocked-owner-FID record.
 * @property profileFidToDelete FID of the user profile to delete.
 * @property authUidToDelete Auth UID to delete from authLinks, or null if no
 * matching auth-link document exists.
 * @property campsiteIdsToDelete Owned campsite document IDs to delete.
 * @property lotteryIdsToDelete Owned lottery document IDs to delete.
 * @property reservationIdsToCancel Active related reservation IDs to cancel.
 * @property bookingIdsToDelete Booking document IDs associated with owned
 * campsites.
 * @property waitlistEntryIdsToDelete Waitlist document IDs associated with
 * owned lotteries.
 * @property entrantFidsToNotify Eligible entrant FIDs to notify once each.
 */
data class OwnerRemovalPlan(
    val blockedAuthUid: String,
    val blockedFid: String,
    val profileFidToDelete: String,
    val authUidToDelete: String?,
    val campsiteIdsToDelete: Set<String>,
    val lotteryIdsToDelete: Set<String>,
    val reservationIdsToCancel: Set<String>,
    val bookingIdsToDelete: Set<String>,
    val waitlistEntryIdsToDelete: Set<String>,
    val entrantFidsToNotify: Set<String>
)

/**
 * Pure, deterministic planner for removing a violating campsite owner.
 *
 * The planner validates the supplied profile and identifies related documents
 * for deletion, active reservations for cancellation, and eligible entrants
 * for notification. It makes no Firebase calls and performs no writes.
 */
object ViolatingOwnerRemovalPlanner {

    /**
     * Validates the profile and calculates the actions needed for its removal.
     *
     * Owned campsites and lotteries are selected by matching their `ownerId`
     * with the profile FID. Related bookings and waitlist entries are selected
     * by their parent document IDs. Reservations with exact `PENDING` or
     * `CONFIRMED` status are selected when they belong to the owner or one of
     * the owner's campsites. Eligible nonblank entrants are included once,
     * unless they opted out.
     *
     * @param input Profile and related records used to build the plan.
     * @return A plan containing the identifiers for block, deletion,
     * cancellation, and notification actions.
     * @throws IllegalArgumentException if the profile does not exist, is not
     * an owner, has a blank auth UID, or its auth link points to another FID.
     */
    fun createPlan(input: OwnerRemovalInput): OwnerRemovalPlan {
        val profile = input.profile

        require(profile.exists) { "The profile no longer exists." }
        require(profile.roles.any { it.equals("owner", ignoreCase = true) }) {
            "This profile is not an owner."
        }
        require(profile.authUid.isNotBlank()) {
            "The owner profile does not have an authUid."
        }
        require(input.authLinkFid == null || input.authLinkFid == profile.fid) {
            "The owner's auth link points to a different profile."
        }

        val ownedCampsites = input.campsites.filter { it.ownerId == profile.fid }
        val ownedLotteries = input.lotteries.filter { it.ownerId == profile.fid }
        val ownedCampsiteIds = ownedCampsites.mapTo(linkedSetOf()) { it.id }
        val ownedLotteryIds = ownedLotteries.mapTo(linkedSetOf()) { it.id }

        val reservationsToCancel = input.reservations.filter { reservation ->
            val active = reservation.status == "PENDING" ||
                    reservation.status == "CONFIRMED"
            val relatedToOwner = reservation.ownerId == profile.fid ||
                    reservation.campsiteId in ownedCampsiteIds
            active && relatedToOwner
        }

        val entrantsToNotify = reservationsToCancel
            .mapNotNull { it.entrantId?.takeIf(String::isNotBlank) }
            .filterNot { it in input.optedOutEntrantFids }
            .toCollection(linkedSetOf())

        return OwnerRemovalPlan(
            blockedAuthUid = profile.authUid,
            blockedFid = profile.fid,
            profileFidToDelete = profile.fid,
            authUidToDelete = profile.authUid.takeIf { input.authLinkFid != null },
            campsiteIdsToDelete = ownedCampsiteIds,
            lotteryIdsToDelete = ownedLotteryIds,
            reservationIdsToCancel = reservationsToCancel
                .mapTo(linkedSetOf()) { it.id },
            bookingIdsToDelete = input.campsiteBookings
                .filter { it.parentId in ownedCampsiteIds }
                .mapTo(linkedSetOf()) { it.id },
            waitlistEntryIdsToDelete = input.waitlistEntries
                .filter { it.parentId in ownedLotteryIds }
                .mapTo(linkedSetOf()) { it.id },
            entrantFidsToNotify = entrantsToNotify
        )
    }
}