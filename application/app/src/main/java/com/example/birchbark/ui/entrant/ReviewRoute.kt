package com.example.birchbark.ui.entrant

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.birchbark.model.Campsite
import com.example.birchbark.model.Review
import com.example.birchbark.model.ReviewRepository
import com.example.birchbark.util.ratingLabel

/**
 * US 01.13.01 and US 01.13.02. Give a rating for a campsite. Loads the entrant's earlier review
 * of the campsite (if any), shows it in [ReviewScreen], and saves with
 * [ReviewRepository.saveReview], which also updates the campsite's overall rating.
 *
 * @param campsite the campsite being reviewed.
 * @param entrantId the signed-in entrant's FID.
 * @param entrantName the entrant's name, saved with the review.
 * @param onBack called when the entrant taps Back.
 * @param onCampsiteUpdated called with the campsite and its new rating after a save,
 *   so the details screen can show the new overall rating.
 */
@Composable
fun ReviewRoute(
    campsite: Campsite,
    entrantId: String,
    entrantName: String,
    onBack: () -> Unit,
    onCampsiteUpdated: (Campsite) -> Unit = {}
) {
    val repository = remember { ReviewRepository() }
    var existing by remember { mutableStateOf<Review?>(null) }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var canReview by remember { mutableStateOf(true) }
    var errors by remember { mutableStateOf<List<String>>(emptyList()) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    //load the entrant's earlier review when the screen opens
    LaunchedEffect(campsite.id, entrantId) {

        repository.checkCanReview(campsite.id, entrantId, object : ReviewRepository.EligibilityCallback {
            override fun onResult(eligible: Boolean) {
                canReview = eligible
                if (!eligible) {
                    errors = listOf("You can only rate or review a campsite after your confirmed stay has ended.")
                }
            }

            override fun onError(error: Exception) {
                canReview = false
                errors = listOf("Couldn't verify your stay history. Check your connection and try again.")
            }
        })

        repository.getReview(campsite.id, entrantId, object : ReviewRepository.LoadCallback {
            override fun onLoaded(review: Review) {
                existing = review
                loading = false
            }

            override fun onNotFound() {
                loading = false
            }

            override fun onError(error: Exception) {
                loading = false
                errors = listOf("Couldn't load your review, check your connection and try again")
            }
        })
    }

    // See [TBD 6] in LLM.md
    ReviewScreen(
        campsiteName = campsite.name,
        existing = existing,
        loading = loading,
        saving = saving,
        errors = errors,
        successMessage = successMessage,
        canReview = canReview,
        onSubmit = { rating, comment ->
            if (!canReview) return@ReviewScreen
            saving = true
            errors = emptyList()
            successMessage = null
            val wasUpdate = existing != null

            val review = Review(
                campsiteId = campsite.id,
                entrantId = entrantId,
                entrantName = entrantName,
                rating = rating,
                comment = comment
            )
            repository.saveReview(review, object : ReviewRepository.SaveCallback {
                override fun onSuccess(review: Review, campsite: Campsite) {
                    saving = false
                    existing = review
                    //only mention the overall rating if the campsite has one
                    val overall = if (campsite.averageRating() != null) {
                        ". Overall rating is now ${campsite.ratingLabel()}"
                    } else {
                        ""
                    }
                    successMessage = (if (wasUpdate) "Review updated" else "Review posted") + overall
                    onCampsiteUpdated(campsite)
                }

                override fun onInvalid(problems: List<String>) {
                    saving = false
                    errors = problems
                }

                override fun onError(error: Exception) {
                    saving = false
                    errors = listOf("Error: ${error.message}")
                }
            })
        },
        onBack = onBack
    )
}
