package com.example.birchbark

import com.google.firebase.firestore.DocumentId

/**
 * Current working version for [UserProfile] data class. The data
 * class primarily holds information related to each user profile
 * for the application.
 *
 * The @set:DocumentId retrieves the documentId, in this case, the FID
 * from the firestore generation when an app is first installed; this minimizes
 * duplication of data inside the document.
 *
 * @param fid The FID value from installing the app first time
 * @param lastName Last name of user profile
 * @param firstName First name of user profile
 * @param email Email of user
 * @param phoneNumber The phone number of the user
 * @param userName The username that a profile can opt in to use for display purposes
 * @param roles A string list of roles that users can have [entrant, owner, admin]
 *
 * See [3] and [5] in References.md
 *
 * Followed data class structure from ListyCity lab code.
 */
data class UserProfile(
    @set:DocumentId var fid: String = "",
    val lastName: String = "",
    val firstName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val userName: String = "",
    val roles: List<String> = emptyList()
) {

}