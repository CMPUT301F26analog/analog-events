package com.example.birchbark

// Importing required libraries
import android.R
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import com.example.birchbark.ui.theme.BirchBarkColors.TealCard
import com.example.birchbark.ui.theme.birchBarkButtonColors

/**
 * Displays the complete details screen for a selected campsite, featuring an image background,
 * summary details, additional pricing, information buttons (Description, Rules, Policy), and
 * the ability to enter the lottery registration system.
 *
 * @param campsite The campsite data model holding all structural information to present.
 * @param onBack Callback triggered when the "Back" button is clicked to return to the previous screen.
 * @param onLotteryClick Callback triggered when the user enters the campsite access lottery.
 * @param modifier The modifier to be applied to the root screen layout.
 */
@Composable
fun CampsiteDetailsScreen(
    campsite: Campsite,
    lottery: Lottery?,
    joined: Boolean,
    message: String?,               // Based on the joined boolean, what message is shown with the lottery
    onBack: () -> Unit,
    onJoin: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Setting up the tabs
    // https://developer.android.com/develop/ui/compose/components/tabs
    var selectedTab by remember { mutableStateOf(DetailTab.DESCRIPTION)}

    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {

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
        Text(text = campsite.name, fontSize = 28.sp, fontWeight = FontWeight.Bold)
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
        Text(tabText, modifier = Modifier.padding(vertical = 16.dp))

        // Lottery button
        LotterySection(lottery = lottery, joined = joined, message = message, onJoin = onJoin)
    }
}

@Composable
private fun TabButton(label: String, selected: Boolean, onClick: () -> Unit) {
    if (selected) {
        Button(onClick = onClick, colors = birchBarkButtonColors(), modifier = Modifier.padding(end = 4.dp)) { Text(label) }
    } else {
        OutlinedButton(onClick = onClick, modifier = Modifier.padding(end = 4.dp)) { Text(label) }
    }
}