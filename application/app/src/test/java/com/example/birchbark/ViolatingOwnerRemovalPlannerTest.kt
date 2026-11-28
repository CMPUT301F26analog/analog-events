package com.example.birchbark

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the pure owner-removal planner.
 *
 * These tests use in-memory sample records and do not connect to Firebase.
 */
class ViolatingOwnerRemovalPlannerTest {

    /**
     * Checks that planning fails when the owner's profile does not exist.
     */
    // See Prompt #093 in LLM.md.
    @Test
    fun rejectsProfileThatDoesNotExist() {
        assertPlanFails(
            input = baseInput().copy(
                profile = ownerProfile().copy(exists = false)
            ),
            expectedMessage = "The profile no longer exists."
        )
    }

    /**
     * Checks that a profile without the owner role cannot be removed as an owner.
     */
    // See Prompt #094 in LLM.md.
    @Test
    fun rejectsProfileWithoutOwnerRole() {
        assertPlanFails(
            input = baseInput().copy(
                profile = ownerProfile().copy(roles = listOf("entrant"))
            ),
            expectedMessage = "This profile is not an owner."
        )
    }

    /**
     * Checks that a profile with no roles is rejected.
     */
    // See Prompt #095 in LLM.md.
    @Test
    fun rejectsProfileWithNoRoles() {
        assertPlanFails(
            input = baseInput().copy(
                profile = ownerProfile().copy(roles = emptyList())
            ),
            expectedMessage = "This profile is not an owner."
        )
    }

    /**
     * Checks that a blank Auth UID causes planning to fail.
     */
    // See Prompt #096 in LLM.md.
    @Test
    fun rejectsBlankAuthUid() {
        assertPlanFails(
            input = baseInput().copy(
                profile = ownerProfile().copy(authUid = "  ")
            ),
            expectedMessage = "The owner profile does not have an authUid."
        )
    }

    /**
     * Checks that planning rejects an auth link associated with another profile.
     */
    // See Prompt #097 in LLM.md.
    @Test
    fun rejectsAuthLinkPointingToAnotherProfile() {
        assertPlanFails(
            input = baseInput().copy(authLinkFid = "different-fid"),
            expectedMessage =
                "The owner's auth link points to a different profile."
        )
    }

    /**
     * Checks that the planner accepts the owner role without case sensitivity.
     */
    // See Prompt #098 in LLM.md.
    @Test
    fun acceptsOwnerRoleRegardlessOfCapitalization() {
        val plan = ViolatingOwnerRemovalPlanner.createPlan(
            baseInput().copy(
                profile = ownerProfile().copy(roles = listOf("Owner"))
            )
        )

        assertEquals("owner-fid", plan.blockedFid)
    }

    /**
     * Checks that only this owner's campsites and lotteries are selected.
     */
    // See Prompt #099 in LLM.md.
    @Test
    fun selectsOnlyThisOwnersCampsitesAndLotteries() {
        val plan = ViolatingOwnerRemovalPlanner.createPlan(baseInput())

        assertEquals(setOf("camp-owner"), plan.campsiteIdsToDelete)
        assertEquals(setOf("lottery-owner"), plan.lotteryIdsToDelete)
    }

    /**
     * Checks that only bookings and waitlist entries related to owned items are selected.
     */
    // See Prompt #100 in LLM.md.
    @Test
    fun selectsOnlyBookingsAndWaitlistEntriesRelatedToOwnedItems() {
        val plan = ViolatingOwnerRemovalPlanner.createPlan(baseInput())

        assertEquals(setOf("booking-owner"), plan.bookingIdsToDelete)
        assertEquals(setOf("waitlist-owner"), plan.waitlistEntryIdsToDelete)
    }

    /**
     * Checks that all pending and confirmed reservations related to the owner
     * are selected, including reservations with missing or blank entrant IDs.
     */
    // See Prompts #101 and #109 in LLM.md.
    @Test
    fun cancelsPendingAndConfirmedReservationsForOwnerOrOwnedCampsite() {
        val plan = ViolatingOwnerRemovalPlanner.createPlan(baseInput())

        assertEquals(
            setOf(
                "pending-at-owned-campsite",
                "confirmed-at-owned-campsite",
                "confirmed-for-owner",
                "blank-entrant-id",
                "missing-entrant-id",
                "another-eligible-entrant"
            ),
            plan.reservationIdsToCancel
        )
    }

    /**
     * Checks that inactive, unrelated, and incorrectly cased statuses
     * are excluded from the cancellation plan.
     */
    // See Prompts #102 and #110 in LLM.md.
    @Test
    fun keepsInactiveAndUnrelatedReservationsOutOfCancellationPlan() {
        val plan = ViolatingOwnerRemovalPlanner.createPlan(baseInput())

        assertFalse("already-cancelled" in plan.reservationIdsToCancel)
        assertFalse("completed" in plan.reservationIdsToCancel)
        assertFalse("other-owners-reservation" in plan.reservationIdsToCancel)
        assertFalse("lowercase-status" in plan.reservationIdsToCancel)
    }

    /**
     * Checks that an entrant with multiple active reservations is listed
     * only once among notification recipients.
     */
    // See Prompt #103 in LLM.md.
    @Test
    fun notifiesEachEligibleEntrantOnlyOnce() {
        val plan = ViolatingOwnerRemovalPlanner.createPlan(baseInput())

        assertEquals(
            setOf("entrant-1", "entrant-4"),
            plan.entrantFidsToNotify
        )
    }

    /**
     * Checks that opted-out entrants and entrants with blank IDs are not
     * included in the notification recipient set.
     */
    // See Prompts #104 and #105 in LLM.md.
    @Test
    fun doesNotNotifyEntrantsWhoOptedOutOrHaveBlankIds() {
        val plan = ViolatingOwnerRemovalPlanner.createPlan(baseInput())

        assertFalse("entrant-opted-out" in plan.entrantFidsToNotify)
        assertFalse("" in plan.entrantFidsToNotify)
        assertFalse("   " in plan.entrantFidsToNotify)
    }

    /**
     * Checks that no auth-link deletion is planned when the auth link is absent.
     */
    // See Prompt #106 in LLM.md.
    @Test
    fun doesNotPlanAuthLinkDeletionWhenNoAuthLinkExists() {
        val plan = ViolatingOwnerRemovalPlanner.createPlan(
            baseInput().copy(authLinkFid = null)
        )

        assertNull(plan.authUidToDelete)
    }

    /**
     * Checks that the plan contains the correct IDs for blocking the owner
     * and removing the profile and matching auth link.
     */
    // See Prompt #107 in LLM.md.
    @Test
    fun includesOwnerAndMatchingAuthLinkInRemovalPlan() {
        val plan = ViolatingOwnerRemovalPlanner.createPlan(baseInput())

        assertEquals("owner-auth-uid", plan.blockedAuthUid)
        assertEquals("owner-fid", plan.blockedFid)
        assertEquals("owner-fid", plan.profileFidToDelete)
        assertEquals("owner-auth-uid", plan.authUidToDelete)
    }

    /**
     * Checks that empty related-data lists produce no cleanup or notification
     * actions, while the owner block and profile IDs remain in the plan.
     */
    // See Prompts #108 and #111 in LLM.md.
    @Test
    fun emptyRelatedDataProducesEmptyCleanupSets() {
        val input = baseInput().copy(
            campsites = emptyList(),
            lotteries = emptyList(),
            reservations = emptyList(),
            campsiteBookings = emptyList(),
            waitlistEntries = emptyList(),
            optedOutEntrantFids = emptySet()
        )

        val plan = ViolatingOwnerRemovalPlanner.createPlan(input)

        assertEquals(emptySet<String>(), plan.campsiteIdsToDelete)
        assertEquals(emptySet<String>(), plan.lotteryIdsToDelete)
        assertEquals(emptySet<String>(), plan.reservationIdsToCancel)
        assertEquals(emptySet<String>(), plan.bookingIdsToDelete)
        assertEquals(emptySet<String>(), plan.waitlistEntryIdsToDelete)
        assertEquals(emptySet<String>(), plan.entrantFidsToNotify)

        assertEquals("owner-auth-uid", plan.blockedAuthUid)
        assertEquals("owner-fid", plan.blockedFid)
        assertEquals("owner-fid", plan.profileFidToDelete)
    }

    /**
     * Runs the planner and checks that it rejects the input with the
     * expected validation error.
     */
    private fun assertPlanFails(
        input: OwnerRemovalInput,
        expectedMessage: String
    ) {
        val error = assertThrows(IllegalArgumentException::class.java) {
            ViolatingOwnerRemovalPlanner.createPlan(input)
        }

        assertEquals(expectedMessage, error.message)
    }

    /**
     * Creates the valid owner profile used by the test fixtures.
     */
    private fun ownerProfile() = RemovalProfile(
        fid = "owner-fid",
        authUid = "owner-auth-uid",
        roles = listOf("entrant", "owner")
    )

    /**
     * Creates sample owner-removal data, including records for another owner
     * and reservations covering active, inactive, opted-out, and missing-ID cases.
     */
    private fun baseInput() = OwnerRemovalInput(
        profile = ownerProfile(),
        authLinkFid = "owner-fid",

        campsites = listOf(
            OwnerDocument("camp-owner", "owner-fid"),
            OwnerDocument("camp-other", "other-fid")
        ),

        lotteries = listOf(
            OwnerDocument("lottery-owner", "owner-fid"),
            OwnerDocument("lottery-other", "other-fid")
        ),

        reservations = listOf(
            ReservationRecord(
                id = "pending-at-owned-campsite",
                ownerId = null,
                campsiteId = "camp-owner",
                status = "PENDING",
                entrantId = "entrant-1"
            ),
            ReservationRecord(
                id = "confirmed-at-owned-campsite",
                ownerId = null,
                campsiteId = "camp-owner",
                status = "CONFIRMED",
                entrantId = "entrant-opted-out"
            ),
            ReservationRecord(
                id = "confirmed-for-owner",
                ownerId = "owner-fid",
                campsiteId = null,
                status = "CONFIRMED",
                entrantId = "entrant-1"
            ),
            ReservationRecord(
                id = "already-cancelled",
                ownerId = "owner-fid",
                campsiteId = null,
                status = "CANCELLED",
                entrantId = "entrant-2"
            ),
            ReservationRecord(
                id = "completed",
                ownerId = "owner-fid",
                campsiteId = null,
                status = "COMPLETED",
                entrantId = "entrant-3"
            ),
            ReservationRecord(
                id = "other-owners-reservation",
                ownerId = "other-fid",
                campsiteId = "camp-other",
                status = "CONFIRMED",
                entrantId = "entrant-5"
            ),
            ReservationRecord(
                id = "lowercase-status",
                ownerId = "owner-fid",
                campsiteId = null,
                status = "pending",
                entrantId = "entrant-6"
            ),
            ReservationRecord(
                id = "blank-entrant-id",
                ownerId = "owner-fid",
                campsiteId = null,
                status = "PENDING",
                entrantId = " "
            ),
            ReservationRecord(
                id = "missing-entrant-id",
                ownerId = "owner-fid",
                campsiteId = null,
                status = "PENDING",
                entrantId = null
            ),
            ReservationRecord(
                id = "another-eligible-entrant",
                ownerId = "owner-fid",
                campsiteId = null,
                status = "PENDING",
                entrantId = "entrant-4"
            )
        ),

        campsiteBookings = listOf(
            RelatedRecord("booking-owner", "camp-owner"),
            RelatedRecord("booking-other", "camp-other")
        ),

        waitlistEntries = listOf(
            RelatedRecord("waitlist-owner", "lottery-owner"),
            RelatedRecord("waitlist-other", "lottery-other")
        ),

        optedOutEntrantFids = setOf("entrant-opted-out")
    )
}