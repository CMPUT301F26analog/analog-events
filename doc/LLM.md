# LLM Reference Document

> Records interactions such as prompts and responses from Large-Language Models (LLMs).

---

## **_STRUCTURE IS AS FOLLOWS_**

## Prompt #001

|    Field     |  Details   |
|:-------------|:-----------|
|**Promptee**  | Your name  |
| **Agent**    | ChatGPT / Gemini / Claude / Muse (lol) |
| **Model**    | The name and version of model |
| **Date**     | MM-DD-YYYY   |

### Prompt

> Make sure to paste your prompt here

### Answer

> Paste the LLM's response here

<!-- Copy the section beginnning at "Prompt #001..." for each new interaction. Increment as we go on -->

## Prompt #001

|    Field     |  Details   |
|:-------------|:-----------|
|**Promptee**  | Riley Whitford  |
| **Agent**    | ChatGPT  |
| **Model**    | GPT-6 |
| **Date**     | 10-26-2026   |

### Prompt

> Hey, I am currently developing an android app in Android Studio using Kotlin. I am creating a small authenticator app, where I followed the majority of the code from the Firebase Installations Page. I don't know how one would test a function like this using Junit in android studio since I am working with a live database. What would be an example of this be like? Please provide something well explained (and with resources), so that for future tests, I can semi-follow this approach. Here is the current code: **INSTERTED CODE**

### Answer

You can test your Authenticator class using JUnit 4, just like your other Android tests. However, since your class communicates with Firebase Installations and Cloud Firestore, I recommend an instrumented JUnit test rather than a regular local unit test.

This allows you to run the test on your Android emulator and communicate with your actual Firebase project. 
Android Developers


For your Birchbark proof-of-concept, I would start with two tests:
1. FID consistency: Verify that Firebase returns the same installation ID on repeated requests.
2. Account lookup: Verify that checkForAccount() invokes the correct callback depending on whether that FID exists in Firestore.
You can do both without modifying your existing Authenticator class.
1. Create your test class
In Android Studio, create:

app/src/androidTest/java/com/example/birchbark/AuthenticatorTest.kt

Make sure the file is in androidTest, not test.

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



Why use CountDownLatch?
Your checkForAccount() method is asynchronous. JUnit cannot simply call it and immediately check the result because Firebase might still be processing the request.

CountDownLatch(1) lets the test wait until one of your callbacks executes.
latch.countDown()


signals that the Firebase operation has completed, while:
latch.await(20, TimeUnit.SECONDS)


waits for the result, with a timeout to prevent the test from hanging indefinitely.
2. Verify your Gradle dependencies
In your application module's build.gradle.kts, make sure you have the Android test dependencies:

android {
    defaultConfig {
        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
}



These are the published AndroidX test libraries. If your project already declares them through libs.versions.toml, you don't need to add duplicate entries. 
Android Developers

3. Run the tests
In Android Studio:
1. Start your Android emulator.
2. Right-click AuthenticatorTest.kt.
3. Select Run 'AuthenticatorTest'.
4. Open the test results and Logcat.
For the first test, you should see something similar to:
fidIsConsistent — Passed

D/AuthenticatorTest: FID: cXyz123ABC...

The second test will check the document with that FID in Firestore and verify that your callback agrees with the database result.
4. Test both account outcomes manually
To exercise both callback paths, you can use the Firestore Console.
1

Test a missing account

Run the tests on a fresh emulator installation with no matching document in the users collection.

Expected Logcat output:
D/AuthenticatorTest: Account missing


The checkForAccountWorks test should pass.



2

Test an existing account

Open the Firebase Console and navigate to Firestore Database.

Create a document in the users collection with the exact FID printed in Logcat as its document ID. Add a sample field, such as username: "testUser".

Rerun the test.

Expected output:
D/AuthenticatorTest: Account found


Both tests should pass.



This approach doesn't delete or overwrite any existing user profiles.
5. What the tests actually verify
Test	Verification
fidIsConsistent()	FID is nonempty and identical between requests
checkForAccountWorks()	The callback corresponds to Firestore document existence
Manual missing-account test	onAccountGone() executes
Manual existing-account test	onAccountFound() executes
Firestore error	Test fails instead of incorrectly reporting a missing account

One minor correction to the documentation in your Authenticator class: the FID is generated and managed by Firebase Installations, not by Cloud Firestore. Firestore simply stores your user profile under that identifier. 
Firebase


I would start with this instrumented test before introducing mocks or dependency injection. For your current proof-of-concept, it verifies the actual Firebase communication and your class's callback behavior while preserving the class structure you've already implemented.

## Prompt #002

|    Field     |  Details   |
|:-------------|:-----------|
|**Promptee**  | Eliza Lazar  |
| **Agent**    | Gemini |
| **Model**    | N/A |
| **Date**     | 11-04-2026   |

### Prompt

> How do I start listening with Firebase, with general steps and explanations?

### Answer

> To listen for real-time data changes in Firebase Cloud Firestore, you set up an active listener on a specific database reference or query. Instead of requesting data once, the listener stays open and automatically pushes a new data "snapshot" to your app whenever data is added, modified, or deleted.
Here is the chronological workflow to start listening to Firestore data.

Phase 1: Set Up the Database Connection
Before listening, you must initialize Firestore and point to the exact collection or document you want to monitor.
Step 1: Get the Firestore instance.
Access the entry point for Firebase in your application code.
Step 2: Define the target path.
Specify the path to a specific collection (e.g., "users") or a single document (e.g., "users/user_123").
Step 3: (Optional) Apply filters.

Phase 2: Attach the Snapshot Listener
You bind a listener to your targeted path using the Firebase SDK.
Step 1: Call the listening method.
Invoke addSnapshotListener on your collection, document, or query reference.
Step 2: Capture the callback.
Provide a callback closure (block of code) that receives two parameters from Firebase: a snapshot object and an error object.
Failure Checkpoint: Always verify if the error object is present. If network connectivity fails or security rules deny access, the listener will fail here, and you must log or handle the error immediately to prevent app silent failures.

Phase 3: Process the Incoming Snapshot
Once the listener is active, Firebase immediately triggers it with the current data state, and then triggers it again for every subsequent database change.
Step 1: Verify data existence.
Ensure the snapshot is not null and actually contains data (e.g., snapshot != null && !snapshot.isEmpty).
Step 2: Clear old local data.
Wipe your local memory array list to prepare for the full fresh state.
Step 3: Deserialize the payload.
Convert the raw Firestore documents into your custom code objects (e.g., using snapshot.toObjects(YourModelClass::class.java)).
Step 4: Update the user interface.
Add the new objects back to your list and notify your UI thread to redraw the screen with the fresh data.

Phase 4: Lifecycle Management (Crucial)
Real-time listeners consume battery, data, and active connection limits. They must be safely shut down when no longer needed.
Step 1: Store the registration handle.
When you call addSnapshotListener, it returns a ListenerRegistration object. Keep a reference to this object.
Step 2: Detach on lifecycle destruction.
When your app screen or component closes (e.g., in onDestroy(), onStop(), or a Compose onDispose), call listenerRegistration.remove() to close the pipeline.

Ready-to-Use Implementation Template
Here is a clean implementation pattern utilizing the steps detailed above:
kotlin
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot

class ProfileManager {

    private val db = FirebaseFirestore.getInstance()
    private var snapshotListener: ListenerRegistration? = null
    val localProfiles = mutableListOf<UserProfile>()

    fun startListeningToProfiles() {
        // Phase 1: Set up target
        val query = db.collection("profiles").orderBy("username")

        // Phase 2: Attach listener
        snapshotListener = query.addSnapshotListener { snapshot, error ->
            // Failure Checkpoint
            if (error != null) {
                println("Firestore listening failed: ${error.message}")
                return@addSnapshotListener
            }

            // Phase 3: Process snapshot
            if (snapshot != null) {
                localProfiles.clear()
                localProfiles.addAll(snapshot.toObjects(UserProfile::class.java))
                
                // Trigger your UI update mechanism here (e.g., adapter.notifyDataSetChanged())
                println("Successfully synchronized ${localProfiles.size} profiles.")
            }
        }
    }

    fun stopListeningToProfiles() {
        // Phase 4: Clean up connection
        snapshotListener?.remove()
        snapshotListener = null
    }
}
Let me know if you need help tailoring this.


## Prompt #003

|    Field     |  Details   |
|:-------------|:-----------|
|**Promptee**  | Eliza Lazar  |
| **Agent**    | Gemini |
| **Model**    | N/A |
| **Date**     | 11-04-2026   |

### Prompt

> How do i add profiles based on FID as documentID in kotlin, using addProfile as general steps? 

### Answer

>Here is a clear breakdown of the general steps to implement your addProfile function using the FID as the document ID, matching the callback pattern (onSuccess/onFailure) and validation pipeline you provided.
General Steps to Implement addProfile
To map your architecture to this flow, follow these 4 sequential steps:
Ensure the Data Class holds the FID: Your UserProfile object must contain a field (like fid) so it can be passed into the function and extracted for the document reference.
Clean & Validate: Sanitize input strings (trimming whitespace, formatting) and run logic checks (e.g., ensuring the FID is not empty). Fail early and catch errors before hitting the database.
Target the Specific Document: Instead of using .add() (which auto-generates a random string ID), look up the explicit path using .document(profile.fid).
Write and Listen: Execute the write using .set(), appending the asynchronous callback listeners to handle the UI state or navigation.

The Complete Implementation
Here is how the complete pattern looks in standard Kotlin, using your exact structure and the Firebase Android SDK:
kotlin
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

// 0. Define the profile data structure
data class UserProfile(
    val fid: String = "",       // Used as the Document ID
    val displayName: String = "",
    val email: String = "",
    val bio: String = ""
)

class ProfileManager {
    private val db = FirebaseFirestore.getInstance()
    private val profilesRef: CollectionReference = db.collection("profiles")

    /**
     * General Step-by-Step Flow to create/update a profile by FID
     */
    fun addProfile(
        profile: UserProfile,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        // Step 1: Clean and tidy the profile text data
        val cleaned = cleanProfile(profile)            

        // Step 2: Validate the cleaned profile data
        val error = validateProfile(cleaned)           
        if (error != null) {
            // Early exit: report validation errors and halt execution
            onFailure(IllegalArgumentException(error)) 
            return
        }

        // Step 3 & 4: Route directly to the FID document path and save
        profilesRef.document(cleaned.fid) // Sets FID as document ID
            .set(cleaned)                 // Overwrites or creates the document
            .addOnSuccessListener { 
                onSuccess() 
            }
            .addOnFailureListener { exception -> 
                onFailure(exception) 
            }
    }

    // Example helper helper to trim whitespace
    private fun cleanProfile(profile: UserProfile): UserProfile {
        return profile.copy(
            fid = profile.fid.trim(),
            displayName = profile.displayName.trim(),
            email = profile.email.trim().lowercase()
        )
    }

    // Example validation rule check
    private fun validateProfile(profile: UserProfile): String? {
        return when {
            profile.fid.isEmpty() -> "Firebase Installation ID (FID) cannot be empty."
            profile.displayName.isEmpty() -> "Display name cannot be blank."
            !profile.email.contains("@") -> "Invalid email address format."
            else -> null // No errors found
        }
    }
}
Key Considerations for this Workflow
Overwrite Warning: .set(cleaned) replaces the entire document if that FID already exists. If your addProfile function is also used for editing later, and you want to preserve fields not modified here, change it to .set(cleaned, SetOptions.merge()).
Network Behavior: Firestore cached writes trigger onSuccess almost instantly if you are offline. If your app relies on strict server-side confirmation, ensure your listeners account for local vs. server events.


## Prompt #004

|    Field     |  Details   |
|:-------------|:-----------|
|**Promptee**  | Eliza Lazar  |
| **Agent**    | Gemini |
| **Model**    | N/A |
| **Date**     | 11-04-2026   |

### Prompt

> Create a test for my UserRepository.kt [INSERT CODE] that follows a similar structure to AuthenticatorTest.kt [INSERT (partial) CODE]. My user story is US 01.09.01 As an entrant, I want to be identified by my device, so that I do not have to use a username and password. 

### Answer

>Yes, absolutely! You can write it exactly like that using Tasks.await().
Using Tasks.await() blocks the current test thread until Firebase finishes its background network task. This completely eliminates deep callback nests inside your test and removes the need to use CountDownLatch for adding or getting data.
Here is how you can write your UserRepositoryTest using that precise synchronous style to satisfy US 01.09.01:

Synchronous UserRepositoryTest.kt
kotlin
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
