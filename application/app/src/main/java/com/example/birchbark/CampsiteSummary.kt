package com.example.birchbark

// Importing required libraries
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import com.example.birchbark.ui.theme.BirchBarkColors.TealCard
import com.example.birchbark.ui.theme.birchBarkButtonColors

/**
 * Displays a summary card for a specific campsite, including its name, location,
 * rating, and max occupancy.
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
        modifier = modifier.fillMaxWidth().padding(8.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = TealCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Our UI Mockups has rating, occupancy, name, then location
            // Rating
            Text(campsite.ratingLabel())
            // Occupancy
            Text(text = "Up to ${campsite.maxOccupancy} guests")
            // Name
            Text(text = campsite.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
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