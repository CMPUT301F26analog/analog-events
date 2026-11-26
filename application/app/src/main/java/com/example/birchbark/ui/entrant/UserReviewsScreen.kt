package com.example.birchbark.ui.entrant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.example.birchbark.model.ReviewRepository
import com.example.birchbark.ui.theme.BirchBarkColors
import com.example.birchbark.ui.theme.ScreenTitle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UserReviewsRoute(
    entrantId: String,
    onBack: () -> Unit
) {
    val repository = remember { ReviewRepository() }
    var items by remember { mutableStateOf<List<ReviewRepository.UserReviewItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(entrantId) {
        loading = true
        errorMessage = null
        repository.getReviewsByEntrant(entrantId, object : ReviewRepository.LoadUserReviewsCallback {
            override fun onLoaded(loadedItems: List<ReviewRepository.UserReviewItem>) {
                items = loadedItems
                loading = false
            }

            override fun onError(error: Exception) {
                loading = false
                errorMessage = "Couldn't load your reviews."
            }
        })
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TextButton(onClick = onBack) {
                Text("Back", color = BirchBarkColors.Gold)
            }
            ScreenTitle("Ratings & Reviews")
        }

        when {
            loading -> {
                item { Text("Loading your reviews...", color = BirchBarkColors.MutedText) }
            }
            errorMessage != null -> {
                item { Text(errorMessage!!, color = MaterialTheme.colorScheme.error) }
            }
            items.isEmpty() -> {
                item {
                    Text(
                        text = "You haven't posted any ratings or reviews yet.",
                        color = BirchBarkColors.MutedText
                    )
                }
            }
            else -> {
                items(items, key = { it.review.campsiteId }) { item ->
                    val dateText = remember(item.review.postedAt) {
                        if (item.review.postedAt > 0L) {
                            SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                                .format(Date(item.review.postedAt))
                        } else ""
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
                                    text = item.campsiteName,
                                    style = MaterialTheme.typography.titleMedium,
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

                            if (item.review.rating != null) {
                                val stars = "★".repeat(item.review.rating) + "☆".repeat(5 - item.review.rating)
                                Text(
                                    text = stars,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = BirchBarkColors.Gold
                                )
                            }

                            if (item.review.comment.isNotBlank()) {
                                Text(
                                    text = item.review.comment,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = BirchBarkColors.Cream
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
