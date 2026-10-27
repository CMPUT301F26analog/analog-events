package com.example.birchbark

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.installations.FirebaseInstallations
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class AuthenticatorTest {

    // Test 1: Ensure the FID remains the same
    @Test
    fun fidIsConsistent() {
        val installations = FirebaseInstallations.getInstance()

        val fid1 = Tasks.await(
            installations.id, 15, TimeUnit.SECONDS
        )

        val fid2 = Tasks.await(
            installations.id, 15, TimeUnit.SECONDS
        )

        Log.d("AuthenticatorTest", "FID: $fid1")

        assertTrue(fid1.isNotBlank())
        assertEquals(fid1, fid2)
    }

    // Test 2: Check account existence in Firestore
    @Test
    fun checkForAccountWorks() {
        val fid = Tasks.await(
            FirebaseInstallations.getInstance().id,
            15, TimeUnit.SECONDS
        )

        // Independently check the expected result
        val document = Tasks.await(
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(fid)
                .get(),
            15, TimeUnit.SECONDS
        )

        val expectedExists = document.exists()
        val latch = CountDownLatch(1)

        var returnedFid: String? = null
        var accountFound: Boolean? = null
        var failure: Exception? = null

        val authenticator = Authenticator()

        authenticator.checkForAccount(
            object : Authenticator.Callback {

                override fun onAccountFound(fid: String) {
                    returnedFid = fid
                    accountFound = true
                    Log.d("AuthenticatorTest", "Account found")
                    latch.countDown()
                }

                override fun onAccountGone(fid: String) {
                    returnedFid = fid
                    accountFound = false
                    Log.d("AuthenticatorTest", "Account missing")
                    latch.countDown()
                }

                override fun onError(error: Exception) {
                    failure = error
                    latch.countDown()
                }
            }
        )

        // Wait for the asynchronous Firebase callback
        assertTrue(
            "Firebase callback timed out",
            latch.await(20, TimeUnit.SECONDS)
        )

        failure?.let {
            throw AssertionError("Firebase lookup failed", it)
        }

        assertEquals(fid, returnedFid)
        assertEquals(expectedExists, accountFound)
    }
}
