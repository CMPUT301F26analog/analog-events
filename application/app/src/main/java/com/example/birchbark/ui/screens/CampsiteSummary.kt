package com.example.birchbark.ui.screens

// Importing required libraries
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import com.example.birchbark.model.Campsite
import com.example.birchbark.ui.theme.BirchBarkColors.TealCard
import com.example.birchbark.ui.theme.birchBarkButtonColors
import com.example.birchbark.util.ratingLabel

/**
 * Displays a summary card for a specific campsite, including its name, location,
 * rating, and max occupancy.
 *
 * US 01.01.04 See campsite summary information including name, location, maximum
 * occupancy, and rating
 *
 * @param campsite The campsite data model containing details to display.
 * @param onClick Callback triggered when the card or the "View" button is clicked.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
fun CampsiteSummary(
    campsite: Campsite,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = TealCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Our UI Mockups has rating, occupancy, name, then location
            // Rating
            Text(campsite.ratingLabel())
            // Occupancy
            Text(text = "Up to ${campsite.maxOccupancy} guests")
            // Name
            Text(text = campsite.name, style = MaterialTheme.typography.titleLarge)
            // Location
            Text(text = campsite.location)
            // View button --> to see the details
            Button(
                onClick = onClick,
                colors = birchBarkButtonColors()
            ) {
                Text("View")
            }
        }
    }
}