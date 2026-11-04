package com.example.birchbark

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.installations.FirebaseInstallations
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


// Created using [4] in LLM references.     
@RunWith(AndroidJUnit4::class)
class UserRepositoryTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var userRepository: UserRepository

    @Before
    fun setUp() {
        // Points directly to your live cloud sandbox database
        firestore = FirebaseFirestore.getInstance()

        // Turn off caching so your tests always read the freshest network data
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false)
            .build()
        firestore.firestoreSettings = settings

        userRepository = UserRepository(firestore)
    }

    /**
     * US 01.09.01: Verifies that an entrant profile can be successfully created
     * and fetched synchronously using ONLY the real device installation ID (FID).
     */
    @Test
    fun testAddAndGetProfile_IdentifiedByDeviceFid_Synchronous() {
        // 1. Fetch the actual unique device installation ID using Tasks.await()
        val realDeviceFid = Tasks.await(
            FirebaseInstallations.getInstance().id,
            15, TimeUnit.SECONDS
        )

        assertTrue("Device FID should not be blank", realDeviceFid.isNotBlank())

        val mockProfile = UserProfile(
            fid = realDeviceFid, // Explicitly linking account identity directly to the hardware ID
            firstName = "Alice",
            lastName = "Smith",
            userName = "alicesmith",
            email = "alice@example.com",
            phoneNumber = "5551234567",
            roles = listOf("entrant")
        )

        // 2. Add the profile using a Latch to wait for the repository callback loop
        val addLatch = CountDownLatch(1)
        var addSuccess = false
        var addException: Exception? = null

        userRepository.addProfile(
            profile = mockProfile,
            onResult = {
                addSuccess = true
                addLatch.countDown()
            },
            onFailure = { error ->
                addException = error
                addLatch.countDown()
            }
        )

        assertTrue("addProfile operation timed out", addLatch.await(15, TimeUnit.SECONDS))
        assertNull("Profile registration failed with: ${addException?.message}", addException)
        assertTrue("Profile creation pipeline should report success", addSuccess)

        // 3. Retrieve the profile back using a Latch to verify passwordless identification
        val getLatch = CountDownLatch(1)
        var fetchedProfile: UserProfile? = null
        var getException: Exception? = null

        userRepository.getProfile(
            fid = realDeviceFid, // Querying strictly via device signature
            onResult = { result ->
                fetchedProfile = result
                getLatch.countDown()
            },
            onFailure = { error ->
                getException = error
                getLatch.countDown()
            }
        )

        assertTrue("getProfile operation timed out", getLatch.await(15, TimeUnit.SECONDS))
        assertNull("Profile retrieval failed with: ${getException?.message}", getException)

        // 4. Final assertions proving the profile matches the specific device account parameters
        assertNotNull("Should find a profile bound to this device ID", fetchedProfile)
        assertEquals(realDeviceFid, fetchedProfile?.fid)
        assertEquals("alicesmith", fetchedProfile?.userName)
        assertEquals("alice@example.com", fetchedProfile?.email)
    }
}
