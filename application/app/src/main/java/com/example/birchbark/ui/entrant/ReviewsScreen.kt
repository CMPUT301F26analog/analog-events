package com.example.birchbark.ui.entrant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.birchbark.model.Campsite
import com.example.birchbark.model.Review
import com.example.birchbark.model.ReviewRepository
import com.example.birchbark.ui.theme.BirchBarkColors
import com.example.birchbark.ui.theme.birchBarkButtonColors
import com.example.birchbark.util.ratingLabel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReviewsRoute(
    campsite: Campsite,
    onBack: () -> Unit,
    onWriteReview: () -> Unit
) {
    val repository = remember { ReviewRepository() }
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(campsite.id) {
        loading = true
        errorMessage = null
        repository.getAllReviews(campsite.id, object : ReviewRepository.LoadAllCallback {
            override fun onLoaded(loadedReviews: List<Review>) {
                reviews = loadedReviews
                loading = false
            }

            override fun onError(error: Exception) {
                loading = false
                errorMessage = "Couldn't load reviews. Check your connection and try again."
            }
        })
    }

    ReviewsScreen(
        campsiteName = campsite.name,
        overallRatingLabel = campsite.ratingLabel(),
        reviews = reviews,
        loading = loading,
        errorMessage = errorMessage,
        onBack = onBack,
        onWriteReview = onWriteReview
    )
}

@Composable
fun ReviewsScreen(
    campsiteName: String,
    overallRatingLabel: String,
    reviews: List<Review>,
    loading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onWriteReview: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TextButton(onClick = onBack) {
                Text("Back", color = BirchBarkColors.Gold)
            }
            Text(
                text = campsiteName,
                style = MaterialTheme.typography.titleLarge,
                color = BirchBarkColors.Cream
            )
            Text(
                text = overallRatingLabel,
                style = MaterialTheme.typography.titleMedium,
                color = BirchBarkColors.Gold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onWriteReview,
                colors = birchBarkButtonColors(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Write or Edit Your Review")
            }
        }

        when {
            loading -> {
                item {
                    Text("Loading reviews...", color = BirchBarkColors.MutedText)
                }
            }
            errorMessage != null -> {
                item {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                }
            }
            reviews.isEmpty() -> {
                item {
                    Text(
                        text = "No reviews yet. Be the first to review this campground!",
                        color = BirchBarkColors.MutedText
                    )
                }
            }
            else -> {
                items(reviews, key = { it.entrantId }) { review ->
                    ReviewItemCard(review = review)
                }
            }
        }
    }
}

@Composable
private fun ReviewItemCard(review: Review) {
    val dateText = remember(review.postedAt) {
        if (review.postedAt > 0L) {
            SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(review.postedAt))
        } else {
            ""
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = BirchBarkColors.TealCard),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = review.entrantName.ifBlank { "Anonymous Camper" },
                    style = MaterialTheme.typography.titleSmall,
                    color = BirchBarkColors.Cream
                )
                if (dateText.isNotEmpty()) {
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.bodySmall,
                        color = BirchBarkColors.MutedText
                    )
                }
            }

            // Show stars if the entrant left a star rating
            if (review.rating != null) {
                val stars = "★".repeat(review.rating) + "☆".repeat(5 - review.rating)
                Text(
                    text = stars,
                    style = MaterialTheme.typography.bodyLarge,
                    color = BirchBarkColors.Gold
                )
            }

            // Show written comment if provided
            if (review.comment.isNotBlank()) {
                Text(
                    text = review.comment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = BirchBarkColors.Cream
                )
            }
        }
    }
}

@Preview(name = "Campground Reviews Preview", showBackground = true, backgroundColor = 0xFF1A1F1B)
@Composable
private fun ReviewsScreenPreview() {
    Surface(color = Color(0xFF1A1F1B)) {
        ReviewsScreen(
            campsiteName = "Whispering Pines Campground",
            overallRatingLabel = "★ 4.5 (2 reviews)",
            reviews = listOf(
                Review(
                    entrantId = "user1",
                    entrantName = "Alex M.",
                    rating = 5,
                    comment = "Incredible lakeside views and spotless fire pits!",
                    postedAt = System.currentTimeMillis()
                ),
                Review(
                    entrantId = "user2",
                    entrantName = "Jordan K.",
                    rating = 4,
                    comment = "Quiet spot, though firewood was a bit damp.",
                    postedAt = System.currentTimeMillis() - 86_400_000L
                )
            ),
            loading = false,
            errorMessage = null,
            onBack = {},
            onWriteReview = {}
        )
    }
}
