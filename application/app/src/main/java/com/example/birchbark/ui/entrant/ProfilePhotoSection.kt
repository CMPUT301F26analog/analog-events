package com.example.birchbark.ui.entrant

// Photo picker: see [TBD 1] in References.md

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.birchbark.model.ProfileImageRepository
import com.example.birchbark.ui.theme.Avatar
import com.example.birchbark.ui.theme.BirchBarkColors
import com.example.birchbark.ui.theme.birchBarkButtonColors
import com.example.birchbark.util.ProfileImageCodec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * US 01.08.02: the profile photo and its buttons. Shows the photo (or the
 * entrant's initials if they don't have one), an "Add photo" button that becomes
 * "Change photo" once there is one, and a "Remove photo" button.
 *
 * It only shows things and reports clicks: picking, shrinking and saving are
 * done by [ProfilePhotoEditor].
 *
 * @param name the entrant's name, for the initials.
 * @param imageBytes the saved photo as JPEG bytes, or null if there isn't one.
 * @param working true while a photo is being saved or removed, which locks the buttons.
 * @param message a short result to show under the buttons, or null.
 * @param isError true if [message] is a problem, so it's shown in red.
 * @param onChoose called when the entrant taps Add photo or Change photo.
 * @param onRemove called when the entrant taps Remove photo.
 * @param modifier modifier from the parent layout.
 */
@Composable
fun ProfilePhotoSection(
    name: String,
    imageBytes: ByteArray?,
    working: Boolean,
    message: String?,
    isError: Boolean,
    onChoose: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Avatar(name = name, size = 96.dp, imageBytes = imageBytes)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onChoose,
                enabled = !working,
                colors = birchBarkButtonColors(),
                modifier = Modifier.testTag("choosePhotoButton")
            ) {
                Text(
                    when {
                        working -> "Saving..."
                        imageBytes != null -> "Change photo"
                        else -> "Add photo"
                    }
                )
            }

            //nothing to remove until there's a photo
            if (imageBytes != null) {
                OutlinedButton(
                    onClick = onRemove,
                    enabled = !working,
                    modifier = Modifier.testTag("removePhotoButton")
                ) {
                    Text("Remove photo", color = BirchBarkColors.Gold)
                }
            }
        }

        if (message != null) {
            Text(
                message,
                color = if (isError) MaterialTheme.colorScheme.error else BirchBarkColors.MutedText,
                modifier = Modifier.testTag("photoMessage")
            )
        }
    }
}

/**
 * US 01.08.02: adds, changes or removes the entrant's profile photo. Opens the
 * system photo picker (no storage permission needed), shrinks the picked photo
 * with [ProfileImageCodec], and saves it with [ProfileImageRepository].
 *
 * See [TBD 3] in LLM.md
 *
 * @param fid the entrant's FID, i.e. their document ID in the "users" collection.
 * @param name the entrant's name, for the initials.
 * @param imageBytes the saved photo as JPEG bytes, or null if there isn't one.
 * @param onImageChanged called after the photo is saved or removed, with the new photo or null.
 *   The caller should keep its copy of the profile in step with this.
 * @param modifier modifier from the parent layout.
 */
@Composable
fun ProfilePhotoEditor(
    fid: String,
    name: String,
    imageBytes: ByteArray?,
    onImageChanged: (ByteArray?) -> Unit,
    modifier: Modifier = Modifier
) {
    val resolver = LocalContext.current.contentResolver
    val repository = remember { ProfileImageRepository() }
    val scope = rememberCoroutineScope()

    var working by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        //null means the entrant closed the picker without choosing
        if (uri != null) {
            working = true
            message = null

            scope.launch {
                //reading and shrinking a photo takes a moment, so not on the main thread
                val bytes = withContext(Dispatchers.IO) { ProfileImageCodec.encode(resolver, uri) }

                if (bytes == null) {
                    working = false
                    isError = true
                    message = "Couldn't read that image, try another one"
                } else {
                    repository.setImage(fid, bytes, object : ProfileImageRepository.SaveCallback {
                        override fun onSaved(image: ByteArray) {
                            working = false
                            isError = false
                            message = "Photo saved"
                            onImageChanged(image)
                        }

                        override fun onInvalid(errors: List<String>) {
                            working = false
                            isError = true
                            message = errors.joinToString("\n")
                        }

                        override fun onError(error: Exception) {
                            working = false
                            isError = true
                            message = "Couldn't save your photo, check your connection and try again"
                        }
                    })
                }
            }
        }
    }

    ProfilePhotoSection(
        name = name,
        imageBytes = imageBytes,
        working = working,
        message = message,
        isError = isError,
        modifier = modifier,
        onChoose = {
            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        onRemove = {
            working = true
            message = null
            repository.removeImage(fid, object : ProfileImageRepository.RemoveCallback {
                override fun onRemoved() {
                    working = false
                    isError = false
                    message = "Photo removed"
                    onImageChanged(null)
                }

                override fun onError(error: Exception) {
                    working = false
                    isError = true
                    message = "Couldn't remove your photo, check your connection and try again"
                }
            })
        }
    )
}
