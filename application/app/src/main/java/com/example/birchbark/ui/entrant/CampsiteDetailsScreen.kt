package com.example.birchbark.ui.entrant

// Importing required libraries
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.birchbark.model.Campsite
import com.example.birchbark.ui.theme.BirchBarkColors.TealCard
import com.example.birchbark.ui.theme.birchBarkButtonColors
import com.example.birchbark.util.priceLabel
import com.example.birchbark.util.ratingLabel
import com.example.birchbark.model.Lottery
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.birchbark.ui.theme.PhotoCard
import com.example.birchbark.ui.theme.StatusChip
import com.example.birchbark.ui.entrant.EntrantWaitlistSection
import com.example.birchbark.ui.theme.BirchBarkColors

import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.tooling.preview.Preview

/**
 * Displays the complete details view for an individual campsite, styled like the
 * mockup's Campsite Details page: a photo card with the rating, name, location and
 * capacity on it, the price, tabs for the description, rules and cancellation
 * policy, and the lottery's join button.
 *
 * US 01.01.05 See details of a campsite
 *
 * Outstanding issues. The photo is a placeholder until image upload is done.
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
 * @param modifier The modifier chain to pass down to the root scrolling Column layout.
 * @param onLeave Callback triggered when the user elects to leave the lottery.
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

        // Photo card: rating on top, then name, location and max occupancy
        PhotoCard(
            height = 240.dp,
            topRow = {
                StatusChip(campsite.ratingLabel())
                // Placeholder until the real promo image is loaded, only if the owner added one
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

        /*
        // Details: Rating, Name, Location, Max occupancy, Prince
        // Rating
        Text(campsite.ratingLabel())
        // Name
        Text(text = campsite.name, style = MaterialTheme.typography.headlineMedium)
        // Location
        Text(text = campsite.location)
        // Max occupancy
        Text(text = "Up to ${campsite.maxOccupancy} guests")
         */
        // Price
        Text(
            text = campsite.priceLabel(),
            style = MaterialTheme.typography.titleMedium,
            color = BirchBarkColors.Gold
        )

        // Spacer(modifier = Modifier.height(16.dp))

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

@Preview(showBackground = true)
@Composable
fun CampsiteDetailsScreenPreview() {
    val sampleCampsite = Campsite(
        id = "1",
        name = "Pine Ridge Overlook",
        location = "Banff National Park, AB",
        maxOccupancy = 6,
        description = "A secluded ridge-top campsite with panoramic views of the Bow Valley, " +
                "flat tent pads, and direct access to alpine hiking trails.",
        rules = "• Quiet hours from 10:00 PM to 7:00 AM.\n" +
                "• Store all food in bear-proof lockers.\n" +
                "• Fires in designated rings only.",
        cancellationPolicy = "Free cancellation up to 48 hours before check-in."
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BirchBarkColors.DarkGreen)
            .safeDrawingPadding()
    ) {
        CampsiteDetailsScreen(
            campsite = sampleCampsite,
            lottery = null,
            joined = false,
            isFull = false,
            message = null,
            onBack = {},
            onJoin = {},
            onLeave = {}
        )
    }
}


/*
// See [3] in LLM.md, used to help create the preview function below
@Preview(showBackground = true, name = "Campsite Details Screen Preview")
@Composable
fun CampsiteDetailsScreenPreview() {
    // 1. Create a dummy campsite model with sample text for tabs
    // Note: Adjust the parameter names based on your actual Campsite data class structure
    val mockCampsite = Campsite(
        name = "Birchwood Haven",
        location = "Jasper National Park, AB",
        maxOccupancy = 4,
        imageUrl = "placeholder_url", // Set to null if you want to test layout without the promo box
        description = "A beautiful secluded spot surrounded by ancient birch trees with an excellent lakeside view.",
        rules = "1. Pack out what you pack in.\n2. Quiet hours from 10 PM to 7 AM.",
        cancellationPolicy = "Full refund if canceled at least 48 hours before check-in."
    )

    // 2. Create a dummy lottery model (using an empty constructor or mock fields)
    // If your Lottery model requires constructor arguments, add mock values here.
    val mockLottery = Lottery()

    // 3. Wrap your screen inside the theme wrapper to see proper colors and buttons
    // Switch to 'BirchBarkTheme' if you have a custom theme wrapper set up.
    MaterialTheme {
        CampsiteDetailsScreen(
            campsite = mockCampsite,
            lottery = mockLottery,
            joined = false,
            isFull = false,
            message = "Registration for the weekend lottery is currently open!",
            onBack = {},
            onJoin = {},
            onLeave = {}
        )
    }
}
 */