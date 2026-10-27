package com.example.birchbark

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

/**
 * The [UserProfile] repository class is mainly responsible for accessing user profile data,
 * manipulating profile data, sending state changes to the viewwer, accepting changes from
 * the controller.
 *
 * The basis of the class is around the Firebase Firestore. It should only work with the [users]
 * collection!
 *
 * The basis for the code is from the ListyCity Lab examples and excersisits, simulating the
 * cityRepository class.
 *
 * See [2], [4], and [5] in References.md
 *
 * @param db Constructor to create the db object that UserRepository will use and test with
 */


class UserRepository(
    // Constructor parameter, used for dependency injection (easier to test)
    // Helps with the reusability of code, makes refactoring easier, helps with
    // testing. See [5] in references
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val profilesRef = db.collection("users")
    private val _profiles = mutableStateListOf<UserProfile>()

    val profiles: List <UserProfile>
        get() = _profiles

    // Need to retrieve all firestore profiles when needed
    // TODO

    // Need to validate profile information; trim, no blank spaces, take care of leading
    // white space or trailing, no newline, format strings as required, concatenate
    // TODO

    // Need to set roles of users
    // TODO

    // Need to add profile based on the FID as documentId
    // TODO

    // Need to retrieve profile using FID
    // TODO

    // Need to update existing user profile
    // TODO

    // Need to delete profile
    // TODO

}