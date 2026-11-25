package com.example.birchbark.ui.owner
// See #TBD6 in LLM.md

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.birchbark.model.Campsite
import com.example.birchbark.model.CampsiteImage
import com.example.birchbark.model.ImageRepository
import com.example.birchbark.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * US 02.01.02 (optional image), the owner's campsite gallery from the
 * My Campgrounds mockup. Shows every photo of one campsite in a grid, and has
 * an Add image button so the owner can upload photos from their phone so
 * entrants know what the campsite is like. Tapping a photo shows it bigger,
 * with a button to remove it.
 *
 * Loads the photos with [ImageRepository] and then shows
 * [CampsiteGalleryContent], which only draws, so tests can use it with fake
 * photos and no Firestore.
 *
 * Outstanding issues. Photos are added one at a time.
 *
 * @param campsite the campsite whose photos to show.
 * @param onBack called when the owner taps Back.
 */
@Composable
fun CampsiteGalleryScreen(
    campsite: Campsite,
    onBack: () -> Unit
) {
    val repository = remember { ImageRepository() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var images by remember { mutableStateOf<List<CampsiteImage>?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var adding by remember { mutableStateOf(false) }
    //bumped every time a photo is added or removed so the list loads again
    var reloadCount by remember { mutableStateOf(0) }

    //stops the spinner and shows what went wrong
    fun showProblem(text: String) {
        adding = false
        message = text
    }

    LaunchedEffect(campsite.id, reloadCount) {
        repository.getImagesForCampsite(campsite.id, object : ImageRepository.ListCallback {
            override fun onLoaded(images: List<CampsiteImage>) {
                setImages(images)
            }

            override fun onError(error: Exception) {
                showProblem("Couldn't load the photos, check your connection and try again")
                setImages(emptyList())
            }

            fun setImages(found: List<CampsiteImage>) {
                images = found
            }
        })
    }

    //opens the phone's photo picker, only showing photos
    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        //uri is null if the owner closed the picker without picking anything
        if (uri != null) {
            adding = true
            message = null
            scope.launch {
                //shrinking a photo takes a moment, so it's done off the main thread so the screen doesn't freeze
                val data = withContext(Dispatchers.IO) {
                    ImageUtils.uriToBase64(context, uri, ImageRepository.MAX_IMAGE_SIZE)
                }
                if (data == null) {
                    showProblem("Couldn't read that photo, try a different one")
                } else {
                    val image = CampsiteImage(
                        campsiteId = campsite.id,
                        ownerId = campsite.ownerId,
                        data = data
                    )
                    repository.addImage(image, object : ImageRepository.SaveCallback {
                        override fun onSuccess(imageId: String) {
                            adding = false
                            reloadCount++
                        }

                        override fun onInvalid(message: String) {
                            showProblem(message)
                        }

                        override fun onError(error: Exception) {
                            showProblem("Couldn't save the photo, please try again later")
                        }
                    })
                }
            }
        }
    }

    val list = images
    if (list == null) {
        //still loading
        Box(
            modifier = Modifier.fillMaxSize().safeDrawingPadding(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        CampsiteGalleryContent(
            campsiteName = campsite.name,
            images = list,
            adding = adding,
            message = message,
            onAddClick = {
                photoPicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onRemove = { image ->
                repository.deleteImage(image.id, object : ImageRepository.DeleteCallback {
                    override fun onDeleted() {
                        reloadCount++
                    }

                    override fun onError(error: Exception) {
                        showProblem("Couldn't remove the photo, please try again later")
                    }
                })
            },
            onBack = onBack
        )
    }
}

/**
 * Draws the gallery: the campsite name, the Add image button, and the photos
 * in a 2 column grid like the mockup. Tapping a photo shows it bigger with a
 * Remove photo button. Only draws, so tests can give it fake photos.
 *
 * @param campsiteName shown in the title, like "Hope Lake Gallery".
 * @param images the campsite's photos, oldest first.
 * @param adding true while a new photo is being shrunk and saved, so the button is turned off.
 * @param message a problem to show the owner, or null if there isn't one.
 * @param onAddClick called when the owner taps Add image.
 * @param onRemove called with the photo the owner wants to remove.
 * @param onBack called when the owner taps Back.
 */
@Composable
fun CampsiteGalleryContent(
    campsiteName: String,
    images: List<CampsiteImage>,
    adding: Boolean,
    message: String?,
    onAddClick: () -> Unit,
    onRemove: (CampsiteImage) -> Unit,
    onBack: () -> Unit
) {
    //the photo the owner tapped to see bigger, or null to show the grid
    var opened by remember { mutableStateOf<CampsiteImage?>(null) }
    val openedImage = opened

    if (openedImage != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(onClick = { opened = null }) { Text("Back to gallery") }
            GalleryPhoto(
                image = openedImage,
                modifier = Modifier.fillMaxWidth().testTag("bigImage")
            )
            OutlinedButton(
                onClick = {
                    onRemove(openedImage)
                    opened = null
                },
                modifier = Modifier.fillMaxWidth().testTag("removeImageButton")
            ) {
                Text("Remove photo")
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp)
    ) {
        TextButton(onClick = onBack) { Text("Back") }
        Text(
            "$campsiteName Gallery",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.testTag("galleryTitle")
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onAddClick,
            enabled = !adding,
            modifier = Modifier.fillMaxWidth().testTag("addImageButton")
        ) {
            Text(if (adding) "Adding photo..." else "Add image")
        }
        if (message != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.testTag("galleryMessage")
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (images.isEmpty()) {
            Text(
                "No photos yet. Add some so entrants know what your campsite is like.",
                modifier = Modifier.testTag("emptyGallery")
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(images, key = { it.id }) { image ->
                    GalleryPhoto(
                        image = image,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .testTag("galleryImage")
                            .clickable { opened = image }
                    )
                }
            }
        }
    }
}

/**
 * One photo with rounded corners. If the saved text isn't a valid photo it
 * shows a grey box instead, so one bad photo doesn't break the screen.
 *
 * @param image the photo to show.
 * @param modifier size and tags from the caller.
 */
@Composable
private fun GalleryPhoto(image: CampsiteImage, modifier: Modifier = Modifier) {
    //only turn the text back into a picture once, not on every redraw
    val picture = remember(image.id, image.data) { ImageUtils.base64ToImageBitmap(image.data) }
    if (picture != null) {
        Image(
            bitmap = picture,
            contentDescription = "Campsite photo",
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(RoundedCornerShape(12.dp))
        )
    } else {
        Box(
            modifier = modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text("Photo")
        }
    }
}