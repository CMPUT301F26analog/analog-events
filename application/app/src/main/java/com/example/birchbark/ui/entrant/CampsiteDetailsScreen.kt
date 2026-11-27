package com.example.birchbark.ui.entrant

// Importing required libraries
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.birchbark.model.Campsite
import com.example.birchbark.model.Lottery
import com.example.birchbark.ui.entrant.DetailTab
import com.example.birchbark.ui.theme.BirchBarkColors
import com.example.birchbark.ui.theme.PhotoCard
import com.example.birchbark.ui.theme.StatusChip
import com.example.birchbark.ui.theme.birchBarkButtonColors
import com.example.birchbark.util.priceLabel
import com.example.birchbark.util.ratingLabel

/**
 * Displays the complete details view for an individual campsite, styled like the
 * mockup's Campsite Details page: a photo card with the rating, name, location and
 * capacity on it, the price, tabs for the description, rules and cancellation
 * policy, and the lottery's join button.
 *
 * US 01.01.05 See details of a campsite
 *
 * Outstanding issues. TBD. The photo is a placeholder until image upload is done.
 * [joined], [isFull], [message], [onJoin] and [onLeave] aren't used yet, the
 * lottery part is drawn by [EntrantWaitlistSection], which has its own join button.
 *
 * @param campsite The campsite model containing metadata, description text, and rules.
 * @param lottery The active lottery instance associated with this campsite, if available.
 * @param joined True if the current user has already registered for this specific lottery.
 * @param isFull True if the current lottery is full.
 * @param message The user-facing contextual status or action message regarding lottery entry status.
 * @param onBack Callback execution when the user elects to navigate back to the previous screen.
 * @param onJoin Callback triggered when the user commits an entry action to join the lottery.
 * @param onLeave Callback triggered when the user elects to leave the lottery.
 * @param onReviewsClick Callback triggered when the user taps the rating chip to view all reviews.
 * @param modifier The modifier chain to pass down to the root scrolling Column layout.
 */
@Composable
fun CampsiteDetailsScreen(
    campsite: Campsite,
    lottery: Lottery?,
    joined: Boolean,
    isFull: Boolean,
    message: String?,               // Based on the joined boolean, what message is shown with the lottery
    onBack: () -> Unit,
    onJoin: () -> Unit,
    onLeave: () -> Unit,
    onReviewsClick: () -> Unit,
    writtenReviewCount: Int = 0,
    modifier: Modifier = Modifier
) {
    // Setting up the tabs : See [1] in References.md, which was used for documentation
    var selectedTab by remember { mutableStateOf(DetailTab.DESCRIPTION) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Back button
        TextButton(
            onClick = onBack
        ) {
            Text("Back", color = BirchBarkColors.Gold)
        }

        // Photo card with details: rating on top, then name, location and max occupancy
        PhotoCard(
            height = 240.dp,
            topRow = {
                val reviewSuffix = when (writtenReviewCount) {
                    0 -> ""
                    1 -> " · 1 review"
                    else -> " · $writtenReviewCount reviews"
                }
                Box(modifier = Modifier.clickable { onReviewsClick() }) {
                    StatusChip("${campsite.ratingLabel()}$reviewSuffix  ›")
                }
                if (!campsite.imageUrl.isNullOrBlank()) {
                    StatusChip("Promo Image")
                }
            },
            bottomStart = {
                Text(
                    text = campsite.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = BirchBarkColors.Cream
                )
                Text(text = campsite.location, color = BirchBarkColors.Cream)
                Text(
                    text = if (campsite.maxOccupancy == 1) "Up to 1 guest" else "Up to ${campsite.maxOccupancy} guests",
                    color = BirchBarkColors.MutedText
                )
            }
        )

        // Price
        Text(
            text = campsite.priceLabel(),
            style = MaterialTheme.typography.titleMedium,
            color = BirchBarkColors.Gold
        )

        // Details : Description, rules, cancellation policy
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (tab in DetailTab.entries) {
                TabButton(tab.label, selectedTab == tab) { selectedTab = tab }
            }
        }
        val tabText = when (selectedTab) {
            DetailTab.DESCRIPTION -> campsite.description
            DetailTab.RULES -> campsite.rules
            DetailTab.POLICY -> campsite.cancellationPolicy
        }.ifBlank { selectedTab.emptyText }
        Text(
            text = tabText,
            color = BirchBarkColors.Cream,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(BirchBarkColors.TealCard)
                .padding(16.dp)
        )

        // Lottery: shows the join button while registration is open
        EntrantWaitlistSection(lottery = lottery)
    }
}

/** A rounded tab button: gold when selected, dark teal when not. */
@Composable
private fun TabButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        contentPadding = PaddingValues(horizontal = 14.dp),
        colors = if (selected) {
            birchBarkButtonColors()
        } else {
            ButtonDefaults.buttonColors(
                containerColor = BirchBarkColors.TealCard,
                contentColor = BirchBarkColors.Cream
            )
        }
    ) {
        Text(label)
    }
}
