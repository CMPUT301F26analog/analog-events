package com.example.birchbark.model

/**
 * One photo of a campsite, as stored in the Firestore "images" collection.
 * US 02.01.02 (optional image) and the image gallery on the campsite details
 * screen. Admins browse and remove these too (US 03.06.01, US 03.03.01).
 *
 * Photos are kept in their own collection instead of inside the campsite, so a
 * campsite can have more than one photo without going over Firestore's 1 MB
 * limit per document, and search doesn't have to load every photo.
 *
 * Firebase Storage needs a paid plan, so the photo itself is saved as text.
 * It's shrunk first and then turned into a Base64 string (see ImageUtils).
 *
 * Every field has a default so Firestore can create an empty CampsiteImage
 * before filling it in.
 *
 * Outstanding issues. Deleting a campsite doesn't delete its photos yet.
 *
 * @property id Firestore document ID. Empty until the photo is first saved.
 * @property campsiteId ID of the campsite this photo belongs to ([Campsite.id]).
 * @property ownerId FID of the campsite's owner, so rules can check who's allowed to delete it.
 * @property data The shrunk photo as a Base64 JPEG string.
 * @property createdAt When the photo was added, in epoch ms. Used to show photos in the order they were added.
 */
data class CampsiteImage(
    val id: String = "",
    val campsiteId: String = "",
    val ownerId: String = "",
    val data: String = "",
    val createdAt: Long = 0L
)