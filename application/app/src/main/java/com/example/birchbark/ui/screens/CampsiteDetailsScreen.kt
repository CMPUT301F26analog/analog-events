package com.example.birchbark.ui.screens

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
import androidx.compose.ui.tooling.preview.Preview

/**
 * Displays the complete details view for an individual campsite. Includes property details,
 * contextual informational tabs, and user registration for the campsite's open availability lottery.
 *
 * US 01.01.05 See details of a campsite
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
    // https://developer.android.com/develop/ui/compose/components/tabs
    var selectedTab by remember { mutableStateOf(DetailTab.DESCRIPTION)}

    Column(modifier = modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(16.dp)
    ) {
        // Back button
        Button(
            onClick = onBack,
            colors = birchBarkButtonColors()
        ) {
            Text("Back")
        }

        // Promo image - Only if owner adds an image
        if (campsite.imageUrl != null) {
            // Placeholder box --> TBD: Find out how to add the image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(vertical = 8.dp)
                    .background(TealCard),
                contentAlignment = Alignment.Center
            ) {
                Text("Promo Image")
            }
        }

        // Details: Rating, Name, Location, Max occupancy, Prince
        // Rating
        Text(campsite.ratingLabel())
        // Name
        Text(text = campsite.name, style = MaterialTheme.typography.headlineMedium)
        // Location
        Text(text = campsite.location)
        // Max occupancy
        Text(text = "Up to ${campsite.maxOccupancy} guests")
        // Price
        Text(campsite.priceLabel())

        Spacer(modifier = Modifier.height(16.dp))

        // Details : Description, rules, cancellation policy
        Row(modifier = Modifier.fillMaxWidth()) {
            TabButton("Description", selectedTab == DetailTab.DESCRIPTION) { selectedTab = DetailTab.DESCRIPTION }
            TabButton("Rules", selectedTab == DetailTab.RULES) { selectedTab = DetailTab.RULES }
            TabButton("Cancellation", selectedTab == DetailTab.POLICY) { selectedTab = DetailTab.POLICY }
        }
        val tabText = when (selectedTab) {
            DetailTab.DESCRIPTION -> campsite.description
            DetailTab.RULES -> campsite.rules
            DetailTab.POLICY -> campsite.cancellationPolicy
        }
        Text(
            text = tabText,
            modifier = Modifier.padding(vertical = 16.dp))

        /*
        // Lottery button - Outstanding Issue, TBD
        LotterySection(
            lottery = lottery,
            joined = joined,
            message = message,
            onJoin = onJoin
        )
         */
    }
}

@Composable
private fun TabButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    if (selected) {
        Button(
            onClick = onClick,
            colors = birchBarkButtonColors(),
            modifier = Modifier.padding(end = 4.dp))
        {
            Text(label)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.padding(end = 4.dp))
        {
            Text(label)
        }
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