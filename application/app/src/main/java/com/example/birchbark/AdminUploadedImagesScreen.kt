package com.example.birchbark

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.birchbark.model.CampsiteImage
import com.example.birchbark.model.ImageRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

/**
 * US 03.06.01
 * References.md 67 to 78
 * Displays all uploaded images for an administrator.
 *
 * Images are loaded from Firestore through [ImageRepository]. Tapping an image
 * looks up and displays the name of its campsite or profile in a dialog.
 *
 * Campsite images use the image's `campsiteId` to look up a document in the
 * `campsites` collection. Images without a `campsiteId` use `ownerId` to look
 * up a profile in the `users` collection.
 */
@Composable
fun AdminUploadedImagesScreen() {
    var images by remember {
        mutableStateOf<List<CampsiteImage>>(emptyList()) }
    var isLoading by remember {
        mutableStateOf(true) }
    var loadError by remember {
        mutableStateOf<String?>(null) }
    var selectedImage by remember {
        mutableStateOf<CampsiteImage?>(null) }
    var associationType by remember {
        mutableStateOf("") }
    var associationName by remember {
        mutableStateOf<String?>(null) }
    var associationLoading by remember {
        mutableStateOf(false) }
    var associationError by remember {
        mutableStateOf<String?>(null) }

    // Loads the uploaded images once when this screen enters composition.
    LaunchedEffect(Unit) {
        ImageRepository().getAllImages(
            object : ImageRepository.ListCallback {
                override fun onLoaded(imagesFromFirestore: List<CampsiteImage>) {
                    Log.d(
                        "ImageRepository",
                        "Project: ${FirebaseApp.getInstance().options.projectId}, " +
                                "images returned: ${imagesFromFirestore.size}, " +
                                "IDs: ${imagesFromFirestore.map { it.id }}")

                    images = imagesFromFirestore
                    isLoading = false }

                override fun onError(error: Exception) {
                    Log.e("ImageRepository", "Failed to load images.", error)
                    loadError = error.localizedMessage ?: "Could not load images."
                    isLoading = false } }) }

    // Shows loading, error, empty, or image-list content as appropriate.
    when {isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center) {
                CircularProgressIndicator() } }

        loadError != null -> {
            Text(
                text = loadError ?: "Could not load images.",
                modifier = Modifier.padding(16.dp)
            )}
        images.isEmpty() -> {
            Text(
                text = "No uploaded images found.",
                modifier = Modifier.padding(16.dp)) }

        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(images, key = { it.id }) { image ->
                    // Decodes each image when its Base64 data changes.
                    val bitmap = remember(image.data) {
                        decodeBase64Image(image.data) }

                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Tap to see the campsite or profile name",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clickable {
                                    selectedImage = image
                                    associationType = ""
                                    associationName = null
                                    associationError = null
                                    associationLoading = true

                                    // Fetches the associated campsite or profile name.
                                    loadImageAssociation(image) { type, name, error ->
                                        // Ignores a result if another image was selected meanwhile.
                                        if (selectedImage?.id == image.id) {
                                            associationType = type
                                            associationName = name
                                            associationError = error
                                            associationLoading = false } } },
                            contentScale = ContentScale.Crop) } else {
                        Text("Could not display this image.") } } } }}

    // Displays the selected image's campsite or profile name in a dialog.
    selectedImage?.let { image ->
        AlertDialog(
            onDismissRequest = {
                selectedImage = null },
            title = {
                Text("Image belongs to") },
            text = {
                when {
                    associationLoading -> {
                        CircularProgressIndicator() }

                    associationError != null -> {
                        Text(
                            associationError
                                ?: "Could not load the name.") }
                    associationName != null -> {
                        Text("$associationType: $associationName") }

                    image.campsiteId.isNotBlank() -> {
                        Text("Campsite name not found.") }

                    else -> {
                        Text("Profile name not found.") } } },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedImage = null
                    }
                ) {
                    Text("Close") } }) } }

/**
 * Loads the name of the campsite or profile associated with [image].
 *
 * If `campsiteId` is present, this looks for the image's campsite in the
 * Firestore `campsites` collection. Otherwise, it uses `ownerId` to look for
 * the profile in the `users` collection.
 *
 * The lookup accepts common name fields: campsite names can use `name`,
 * `campsiteName`, or `title`; profile names can use `name`, `displayName`,
 * `fullName`, or `username`.
 *
 * @param image image whose associated campsite or profile should be found.
 * @param onResult called with the association type, its name if found, and an
 * error message if the Firestore lookup fails.
 */
private fun loadImageAssociation(
    image: CampsiteImage,
    onResult: (type: String, name: String?, error: String?) -> Unit
) { val isCampsiteImage = image.campsiteId.isNotBlank()
    val collection = if (isCampsiteImage) "campsites" else "users"
    val documentId = if (isCampsiteImage) image.campsiteId else image.ownerId

    if (documentId.isBlank()) {
        onResult(
            if (isCampsiteImage) "Campsite" else "Profile",
            null,
            "This image does not have an associated ID.")
        return }

    FirebaseFirestore.getInstance()
        .collection(collection)
        .document(documentId)
        .get()
        .addOnSuccessListener { document ->
            val name = if (isCampsiteImage) {
                document.getString("name")
                    ?: document.getString("campsiteName")
                    ?: document.getString("title")
            } else {
                document.getString("name")
                    ?: document.getString("displayName")
                    ?: document.getString("fullName")
                    ?: document.getString("username") }

            onResult(
                if (isCampsiteImage) "Campsite" else "Profile",
                name,
                null
            ) }
        .addOnFailureListener { error ->
            onResult(
                if (isCampsiteImage) "Campsite" else "Profile",
                null,
                error.localizedMessage ?: "Could not load the name."
            ) } }

/**
 * Decodes an image stored as Base64 text.
 *
 * Accepts either plain Base64 data or a data URL containing a comma-separated
 * header and Base64 payload.
 *
 * @param data Base64 image text read from Firestore.
 * @return decoded bitmap, or `null` if the data is invalid or cannot be decoded.
 */
private fun decodeBase64Image(data: String) =
    runCatching {
        val encodedData = data.substringAfter(",", data)
        val bytes = Base64.decode(encodedData, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }.getOrNull()