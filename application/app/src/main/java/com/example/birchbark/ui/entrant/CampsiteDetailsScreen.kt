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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.birchbark.ui.theme.RemotePhoto
import java.util.Locale
import androidx.compose.ui.platform.testTag

/**
 * Represents the sections displayed on a details screen, along with their UI metadata.
 *
 * @property label The display title for the section header.
 * @property emptyText The fallback message shown when the section has no content.
 */
private enum class DetailsSection(val label: String, val emptyText: String) {
    DESCRIPTION("Description", "No description provided."),
    LOCATION("Location", "No coordinates provided."),
    RULES("Rules", "No rules provided."),
    POLICY("Policy", "No cancellation policy provided.")
}

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
    // OUT OF DATE: Setting up the tabs : See [1] in References.md, which was used for documentation
    // var selectedTab by remember { mutableStateOf(DetailTab.DESCRIPTION) }

    // See [TBD 10] in LLM.md
    var selectedSection by remember { mutableStateOf(DetailsSection.DESCRIPTION) }
    var repoImages by remember(campsite.id) { mutableStateOf<List<String>>(emptyList()) }
    androidx.compose.runtime.LaunchedEffect(campsite.id) {
        if (campsite.id.isNotBlank()) {
            com.example.birchbark.model.ImageRepository().getImagesForCampsite(
                campsite.id,
                object : com.example.birchbark.model.ImageRepository.ListCallback {
                    override fun onLoaded(found: List<com.example.birchbark.model.CampsiteImage>) {
                        repoImages = found.map { it.data }.filter { it.isNotBlank() }
                    }
                    override fun onError(error: Exception) {}
                }
            )
        }
    }
    val images = remember(campsite.id, campsite.imageUrl, campsite.galleryUrls, repoImages) {
        (campsite.allImages() + repoImages).distinct()
    }
    var selectedImageIndex by remember(campsite.id) { mutableIntStateOf(0) }

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
            height = 280.dp,
            imageUrl = images.getOrNull(selectedImageIndex),
            topRow = {
                val reviewSuffix = when (writtenReviewCount) {
                    0 -> ""
                    1 -> " · 1 review"
                    else -> " · $writtenReviewCount reviews"
                }
                Box(modifier = Modifier.clickable { onReviewsClick() }) {
                    StatusChip("${campsite.ratingLabel()}$reviewSuffix  ›")
                }
                if (images.size > 1) {
                    StatusChip("${selectedImageIndex + 1} / ${images.size}")
                }
            },
            topEnd = {
                if (images.size > 1) {
                    Column(
                        modifier = Modifier
                            .height(180.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        images.forEachIndexed { idx, url ->
                            val isSelected = idx == selectedImageIndex
                            RemotePhoto(
                                imageUrl = url,
                                contentDescription = "Gallery image ${idx + 1}",
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) BirchBarkColors.Gold else BirchBarkColors.Cream.copy(alpha = 0.6f),
                                        shape = CircleShape
                                    )
                                    .clickable { selectedImageIndex = idx }
                            )
                        }
                    }
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
            },
            bottomEnd = {
                if (images.size > 1) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(BirchBarkColors.TealCard.copy(alpha = 0.9f))
                                .clickable {
                                    selectedImageIndex = (selectedImageIndex - 1 + images.size) % images.size
                                }
                        ) {
                            Text(
                                text = "‹",
                                color = BirchBarkColors.Gold,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(BirchBarkColors.TealCard.copy(alpha = 0.9f))
                                .clickable {
                                    selectedImageIndex = (selectedImageIndex + 1) % images.size
                                }
                        ) {
                            Text(
                                text = "›",
                                color = BirchBarkColors.Gold,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
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
            for (section in DetailsSection.entries) {
                TabButton(section.label, selectedSection == section) { selectedSection = section }
            }
        }

        if (selectedSection == DetailsSection.LOCATION) {
            CampsiteMapAndCoordinatesCard(campsite = campsite)
        } else {
            val tabText = when (selectedSection) {
                DetailsSection.DESCRIPTION -> campsite.description
                DetailsSection.RULES -> campsite.rules
                DetailsSection.POLICY -> campsite.cancellationPolicy
                DetailsSection.LOCATION -> ""
            }.ifBlank { selectedSection.emptyText }

            Text(
                text = tabText,
                color = BirchBarkColors.Cream,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BirchBarkColors.TealCard)
                    .padding(16.dp)
            )
        }

        // Lottery: shows the join button while registration is open
        EntrantWaitlistSection(lottery = lottery)
    }
}

// See [TBD 9] in LLM.md
/**
 * Displays a card showing the [campsite]'s location name, formatted coordinates, and an
 * interactive [com.example.birchbark.ui.owner.CampsiteMap] when valid coordinates are available.
 *
 * Falls back to `"Location"` if [Campsite.location] is blank, and renders a placeholder message
 * instead of the map if [Campsite.latitude] or [Campsite.longitude] fails validation via
 * [com.example.birchbark.util.MapUtils.isValidLocation].
 *
 * @param campsite The [Campsite] data model providing the location name, coordinates, and pin title.
 */
@Composable
private fun CampsiteMapAndCoordinatesCard(campsite: Campsite) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BirchBarkColors.TealCard)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = campsite.location.ifBlank { "Location" },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = BirchBarkColors.Cream
        )

        if (com.example.birchbark.util.MapUtils.isValidLocation(campsite.latitude, campsite.longitude)) {
            Text(
                text = com.example.birchbark.util.MapUtils.locationText(campsite.latitude, campsite.longitude),
                color = BirchBarkColors.Gold,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.testTag("mapLocationText")
            )
            com.example.birchbark.ui.owner.CampsiteMap(
                latitude = campsite.latitude,
                longitude = campsite.longitude,
                pinTitle = campsite.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .testTag("detailsMap")
            )
        } else {
            Text(
                text = "Map location not set for this campsite.",
                color = BirchBarkColors.MutedText
            )
        }
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
