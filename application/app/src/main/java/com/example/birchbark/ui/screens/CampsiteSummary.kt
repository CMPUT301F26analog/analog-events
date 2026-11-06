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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview



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
            Text(campsite.ratingLabel(),
                color = Color.White)
            // Occupancy
            Text(text = "Up to ${campsite.maxOccupancy} guests",
                color = Color.White)
            // Name
            Text(text = campsite.name, style = MaterialTheme.typography.titleLarge,
                color = Color.White)
            // Location
            Text(text = campsite.location,
                color = Color.White)
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

/*
// See [3] in LLM.md, which was used for creating the preview function below
@Preview(showBackground = true, name = "Campsite Summary Preview")
@Composable
fun CampsiteSummaryPreview() {
    // 1. Create a dummy or mock campsite object to feed into your UI
    // Note: Replace these parameters with whatever fields your actual
    // com.example.birchbark.model.Campsite class requires.
    val mockCampsite = Campsite(
        name = "Whispering Pines",
        location = "Banff National Park, AB",
        maxOccupancy = 6,

        // Add any other properties your Campsite model requires (e.g., rating = 4.8)
    )

    // 2. Wrap it inside your app's theme so colors like TealCard render properly
    // Replace 'MaterialTheme' with 'BirchBarkTheme' if you have a custom theme file!
    MaterialTheme {
        CampsiteSummary(
            campsite = mockCampsite,
            onClick = { /* Do nothing in preview */ }
        )
    }
}
*/
