package com.example.birchbark.ui.entrant

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.birchbark.model.Review
import com.example.birchbark.model.ReviewValidator
// import com.example.birchbark.ui.theme.ScreenTitle // TBD
import com.example.birchbark.ui.theme.BirchBarkColors
import com.example.birchbark.ui.theme.birchBarkButtonColors
import com.example.birchbark.ui.theme.birchBarkFieldColors

import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

/**
 * Five tappable stars. Tapping a star sets that many stars. There's a separate
 * "Clear rating" button on the screen to remove the rating again.
 *
 * @param rating how many stars are filled, or null for none.
 * @param onRatingChange called with the star that was tapped (1 to 5).
 * @param enabled false to lock the stars, e.g. while saving.
 * @param modifier modifier from the parent layout.
 */
@Composable
fun StarRating(
    rating: Int?,
    onRatingChange: (Int) -> Unit,
    enabled: Boolean = true,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (star in 1..5) {
            val filled = rating != null && star <= rating
            Text(
                text = if (filled) "★" else "☆",
                style = MaterialTheme.typography.headlineMedium,
                color = BirchBarkColors.Gold,
                modifier = Modifier
                    .testTag("star-$star")
                    .clickable(enabled = enabled, onClickLabel = "Rate $star stars") {
                        onRatingChange(star)
                    }
                    .padding(4.dp)
            )
        }
    }
}

/**
 * US 01.13.01 and US 01.13.02: the form where an entrant gives a campsite a rating, a written
 * review, or both. Styled like the mockup's review page: stars, a review box
 * and a gold Post Review button (Update Review if they already reviewed it).
 *
 * It only shows things and reports a click: loading and saving are done by
 * [ReviewRoute].
 *
 * @param campsiteName the campsite being reviewed.
 * @param existing the entrant's earlier review of this campsite, or null if they haven't made one.
 * @param loading true while the earlier review is being loaded.
 * @param saving true while the review is being saved.
 * @param errors problems from saving (rule messages or a network problem), shown in red.
 * @param successMessage shown in gold after a save worked.
 * @param onSubmit called with the stars (null for none) and the written text (can be blank).
 * @param onBack called when the entrant taps Back.
 * @param modifier modifier from the parent layout.
 */
@Composable
fun ReviewScreen(
    campsiteName: String,
    existing: Review?,
    loading: Boolean,
    saving: Boolean,
    errors: List<String>,
    successMessage: String?,
    onSubmit: (Int?, String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // The stars and text. Rebuilt from the earlier review whenever a new one arrives (after loading, after saving)
    var rating by remember(existing) { mutableStateOf(existing?.rating) }
    var comment by remember(existing) { mutableStateOf(existing?.comment ?: "") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("Back", color = BirchBarkColors.Gold)
        }

        // ScreenTitle("Your Review") // TBD
        Text(
            campsiteName,
            style = MaterialTheme.typography.titleLarge,
            color = BirchBarkColors.Cream
        )

        if (loading) {
            Text("Loading...", color = BirchBarkColors.MutedText)
        } else {
            Text("Rating (optional)", color = BirchBarkColors.MutedText)
            StarRating(
                rating = rating,
                onRatingChange = { rating = it },
                enabled = !saving
            )
            if (rating != null) {
                TextButton(onClick = { rating = null }, enabled = !saving) {
                    Text("Clear rating", color = BirchBarkColors.Gold)
                }
            }

            OutlinedTextField(
                value = comment,
                //cut anything past the limit, e.g. from pasting a long text
                onValueChange = { comment = it.take(ReviewValidator.MAX_COMMENT_LENGTH) },
                label = { Text("Write a review (optional)") },
                supportingText = { Text("${comment.length}/${ReviewValidator.MAX_COMMENT_LENGTH}") },
                minLines = 4,
                enabled = !saving,
                colors = birchBarkFieldColors(),
                modifier = Modifier.fillMaxWidth().testTag("reviewField")
            )

            // Needs a rating, a review, or both
            Button(
                onClick = { onSubmit(rating, comment) },
                enabled = !saving && (rating != null || comment.isNotBlank()),
                colors = birchBarkButtonColors(),
                modifier = Modifier.fillMaxWidth().testTag("postReviewButton")
            ) {
                Text(
                    when {
                        saving -> "Saving..."
                        existing != null -> "Update Review"
                        else -> "Post Review"
                    }
                )
            }
        }

        // See [TBD 5] in LLM.md
        errors.forEach {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        if (successMessage != null) {
            Text(successMessage, color = BirchBarkColors.Gold)
        }
    }
}

@Preview(name = "New Review", showBackground = true, backgroundColor = 0xFF1A1F1B)
@Composable
private fun ReviewScreenNewPreview() {
    // Wrap in BirchBarkTheme { ... } if you have a MaterialTheme wrapper
    Surface(color = Color(0xFF1A1F1B)) { // Matches typical dark forest theme background
        ReviewScreen(
            campsiteName = "Whispering Pines Campground",
            existing = null,
            loading = false,
            saving = false,
            errors = emptyList(),
            successMessage = null,
            onSubmit = { _, _ -> },
            onBack = {}
        )
    }
}