package com.example.birchbark

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import android.util.Log
import com.google.firebase.firestore.ListenerRegistration
import kotlin.String

/**
 * The [UserProfile] repository class is mainly responsible for accessing user profile data,
 * manipulating profile data, sending state changes to the viewer, accepting changes from
 * the controller.
 *
 * The basis of the class is around the Firebase Firestore. It should only work with the [users]
 * collection!
 *
 * The basis for the code is from the ListyCity Lab examples and exercises, simulating the
 * cityRepository class.
 *
 * See [2], [4], and [5] in References.md
 *
 * @param db Constructor to create the db object that UserRepository will use and test with
 */


class UserRepository(
    // Constructor parameter, used for dependency injection (easier to test)
    // Helps with the reusability of code, makes refactoring easier, helps with
    // testing. See [5] in References.md
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val profilesRef = db.collection("users")
    private val _profiles = mutableStateListOf<UserProfile>()

    val profiles: List <UserProfile>
        get() = _profiles

    private var listener: ListenerRegistration? = null

    /**
     * Starts a live listener on ALL profiles, and is intended for Admin profile screen only.
     * Retrieves all firestore profiles when needed.
     * Entrants should use [getProfile] for their own profile, for privacy concerns. See [2]
     * in LLM.md (references).
     */
    fun startListening() {
        // Preventing duplicate listeners
        if (listener!=null) return // i.e., already listening, so we don't need to register a new listener

        // Real-time snapshot listener. Uses [2] in LLM.md as a structure/code helper.
        listener = profilesRef.addSnapshotListener { snapshots, exception ->
            // Handing errors
            if (exception != null) {
                Log.e("UserRepository", "Listen failed", exception) // Logging the error and stopping
                return@addSnapshotListener
            }
            // Get up-to-date user profile information from db
            if (snapshots != null) {
                _profiles.clear() // Throw away old list
                _profiles.addAll(snapshots.toObjects(UserProfile::class.java)) // Redo the firestore
            }
        }
    }

    /** Stops the live listening. Should be called when the screen 'leaves' */
    fun stopListening() {
        listener?.remove()
        listener = null
    }

    /** Returns [profile] with leading/trailing spaces removed and inner whitespace taken care of,
     * including blank spaces and newlines. Strings are formatted as required. Concatenation is
     * resolved. See [6] and [7] in References.md, used to help understand the documentation.
     */
    fun cleanProfile(profile: UserProfile): UserProfile = profile.copy(
        // All regex was with the help of [6] in References.md
        fid = profile.fid.trim(),
        lastName = profile.lastName.replace(Regex("\\s+"), " ").trim(),
        firstName = profile.firstName.replace(Regex("\\s+"), " ").trim(),
        email = profile.email.trim().lowercase(),
        phoneNumber = profile.phoneNumber.replace(Regex("\\s+"), " ").trim(),
        userName = profile.userName.replace(Regex("\\s+"), " ").trim(),
        roles = profile.roles.map { it.trim().lowercase() } // [7] in References.md
    )

    /**
     * Validates a clean profile. Returns an error message to show the user, or null if it's valid.
     * Check [8] in References.md, which was used for documentation.
     */
    fun validateProfile(profile: UserProfile): String? {
        if (profile.fid.isBlank()) return "Profile ID is missing."
        if (profile.lastName.isBlank() || profile.firstName.isBlank()) return "Name can't be blank."
        // Email should look like: something@something.something
        val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
        if (profile.email.isBlank() || !emailRegex.matches(profile.email)) {
            return "That email address doesn't look right."
        }
        // Phone number should look like generally have minimum 3 (area code) & check [8] in References.md
        if (profile.phoneNumber.isBlank() || profile.phoneNumber.count { it.isDigit() } !in 3..15) {
            return "That phone number doesn't look right."
        }
        for (role in profile.roles){
            if ((role != "entrant") && (role != "admin") && (role != "owner")) {
                return "Unknown role."
            }
        }
        return null // No problems found
    }

    /**
     * Creates a profile at users/{fid}. Using FID as the documentID means only one device
     * can have one profile. See [3] in LLM.md (references), which was used as a starting base
     * for the structure of this code. See [10] in References.md, used for documentation.
     */
    fun addProfile(
        profile: UserProfile,
        onResult: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        // Clean the data
        val clean = cleanProfile(profile)
        // Validate the data. Uses [3] in LLM.md as a structure/code helper.
        val error = validateProfile(clean)
        if (error != null){
            onFailure(IllegalArgumentException(error))
            return
        }
        // Route to the FID document path and save it. Uses [3] in LLM.md as a structure/code helper.
        profilesRef.document(clean.fid)
            .set(clean)  // [10] in References.md
            .addOnSuccessListener { onResult() }
            .addOnFailureListener { onFailure(it) }
    }

    /**
     * Gets the profile for [fid]. Calls [onResult] with the profile, or null if none exists yet.
     * Uses similar structure as [addProfile] and [startListening] functions for help.
     * */
    fun getProfile(
        fid: String,
        onResult: (UserProfile?) -> Unit,
        onFailure: (Exception) -> Unit = {}
    ) {
        profilesRef.document(fid).get()
            .addOnSuccessListener { doc -> onResult(doc.toObject(UserProfile::class.java)) } // null if missing
            .addOnFailureListener { onFailure(it) }
    }

    /**
     * Updates name, email, and phone number of an existing profile. Role is not changed here.
     * Uses similar structure as [addProfile] and [startListening] functions for help. See [9]
     * in References.md, used for documentation.
     */
    fun updateProfile(
        profile: UserProfile,
        onResult: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        // Clean the data
        val clean = cleanProfile(profile)
        // Validate the data
        val error = validateProfile(clean)
        if (error != null) {
            onFailure(IllegalArgumentException(error))
            return
        }
        // Set up the updates. See [9] in References.md
        val updates = mapOf(
            "lastName" to clean.lastName,
            "firstName" to clean.firstName,
            "email" to clean.email,
            "phoneNumber" to clean.phoneNumber,
            "userName" to clean.userName
        )
        // Now actually update the data
        profilesRef.document(clean.fid)
            .update(updates)
            .addOnSuccessListener { onResult() }
            .addOnFailureListener { onFailure(it) }

    }

    /**
     * Set's the user's role [entrant, owner, admin]. Uses similar structure as [addProfile]
     * and [startListening] functions for help.
     */
    fun setRole(
        fid: String,
        roles: List<String>,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        // Validating the roles first
        val cleanRoles = roles.map {it.trim().lowercase() }
        for (role in cleanRoles){
            if ((role != "entrant") && (role != "admin") && (role != "owner")) {
                onFailure(IllegalArgumentException("Unknown role."))
                return
            }
        }
        // Actually updating the roles
        profilesRef.document(fid)
            .update("roles", cleanRoles)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    /**
     * Deletes the profile document. Uses similar structure as [addProfile]
     * and [startListening] functions for help. See [11] in References.md, used for documentation.
     */
    fun removeProfile(
        fid: String,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        profilesRef.document(fid).delete() // [11] in References.md
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

}