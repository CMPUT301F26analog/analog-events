package com.example.birchbark

import com.example.birchbark.model.ProfileDeletionCoordinator
import org.junit.Assert.*
import org.junit.Test

/*
Additional Testing Requested:
Verify deletion for an entrant belonging to multiple waitlists.
Confirm that all affected waitlist membership documents are removed.
Verify that waitlist counts are decremented correctly.
Confirm that users/{fid} and authLinks/{authUid} are removed.
Verify that Cancel preserves the profile and its memberships. - Done in DeleteProfileSectionTest
Verify that unauthorized users cannot delete another person's profile. - Done in ProfileDeletionCoordinatorTest
 */

/* See [41] in LLM.md, used to help understand what can be done in Test file versus through
application self-testing. Some of the below tests were planned out/built on with LLM assistance.
 */

class ProfileDeletionCoordinatorTest {

    @Test
    fun deleteProfile_failsWhenProfileIdentityMissing() {
        val coordinator = ProfileDeletionCoordinator()

        var error: Exception? = null

        coordinator.deleteProfile(
            profile = UserProfile(
                fid = "",
                authUid = ""
            ),
            onSuccess = {},
            onFailure = { error = it }
        )

        assertNotNull(error)
        assertTrue(error is IllegalArgumentException)
        assertEquals(
            "Profile identity is incomplete.",
            error?.message
        )
    }

    @Test
    fun deleteProfile_failsWhenFidMissing() {
        val coordinator = ProfileDeletionCoordinator()

        var error: Exception? = null

        coordinator.deleteProfile(
            profile = UserProfile(
                fid = "",
                authUid = "auth123"
            ),
            onSuccess = {},
            onFailure = { error = it }
        )

        assertNotNull(error)
        assertTrue(error is IllegalArgumentException)
    }

    @Test
    fun deleteProfile_failsWhenAuthUidMissing() {
        val coordinator = ProfileDeletionCoordinator()

        var error: Exception? = null

        coordinator.deleteProfile(
            profile = UserProfile(
                fid = "user123",
                authUid = ""
            ),
            onSuccess = {},
            onFailure = { error = it }
        )

        assertNotNull(error)
        assertTrue(error is IllegalArgumentException)
    }

    @Test
    fun deleteProfile_failsWhenDeletingOtherUsersProfile() {
        val coordinator = ProfileDeletionCoordinator()

        var error: Exception? = null

        coordinator.deleteProfile(
            profile = UserProfile(
                fid = "user123",
                authUid = "someOtherUid"
            ),
            onSuccess = {
                fail("Should not succeed")
            },
            onFailure = {
                error = it
            }
        )

        assertNotNull(error)
    }
}