package com.example.birchbark
import androidx.compose.runtime.LaunchedEffect

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import androidx.compose.ui.platform.testTag
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.birchbark.ui.theme.BirchBarkTheme

//Shows the campsite cards from Firebase.
//The admin can filter the campsites, open the details, and remove a campsite.
//This composable has no parameters because it loads its campsite data from Firebase.
//It keeps track of the campsite list, loading state, filters, and open dialogs.
@Composable

fun AdminCampsitesScreen() {


    var campsites by remember {
        mutableStateOf(emptyList<CampsitePreview>())
    }
    // This saves the campsite that the admin wants to delete.
    var campsiteToDelete by remember {
        mutableStateOf<CampsitePreview?>(null)
    }
    // This shows an error if Firebase cannot delete the campsite.
    var deleteError by remember { mutableStateOf<String?>(null) }
    // Tracks whether the campsite request is still running.
    var isLoading by remember { mutableStateOf(true) }
    // Stores a message to show if Firebase cannot load the campsite list.
    var loadError by remember { mutableStateOf<String?>(null) }

    // Loads the campsite documents when this screen first appears.
    LaunchedEffect(Unit) {
        // Reads all campsite documents from the Firestore campsites collection.
        Firebase.firestore.collection("campsites")
            .get()
            .addOnSuccessListener { result ->
                // Turns each Firestore document into the smaller object used by the UI.
                // Documents without a name are skipped because the card needs a name.
                campsites = result.documents.mapNotNull { document ->
                    val name = document.getString("name") ?: return@mapNotNull null
                    val ratingCount =
                        (document.get("ratingCount") as? Number)?.toInt() ?: 0
                    val ratingTotal =
                        (document.get("ratingTotal") as? Number)?.toDouble() ?: 0.0

                    // Uses the Firestore field names to fill in the campsite card details.
                    CampsitePreview(
                        campsiteId = document.id,
                        name = name,
                        location = document.getString("location") ?: "",
                        rating = if (ratingCount > 0) ratingTotal / ratingCount else 0.0,
                        owner = document.getString("ownerId") ?: "",
                        keywords = (document.get("tags") as? List<*>)
                            ?.filterIsInstance<String>()
                            ?.joinToString(" ") ?: "",
                        capacity = (document.get("maxOccupancy") as? Number)?.toInt() ?: 0
                    )
                }
                isLoading = false
            }
            .addOnFailureListener { error ->
                loadError = error.localizedMessage ?: "Could not load campsites."
                isLoading = false
            }
    }

    var appliedFilters by remember { mutableStateOf(CampsiteFilters()) }
    var draftFilters by remember { mutableStateOf(CampsiteFilters()) }
    var showFilters by remember { mutableStateOf(false) }
    var selectedCampsite by remember {
        mutableStateOf<CampsitePreview?>(null)
    }

    // Keeps only campsites that match every filter the admin has applied.
    val visibleCampsites = campsites.filter { campsite ->
        val minCapacity = appliedFilters.minCapacity.toIntOrNull()
        val maxCapacity = appliedFilters.maxCapacity.toIntOrNull()
        val minRating = appliedFilters.minRating.toDoubleOrNull()

        (appliedFilters.location.isBlank() ||
                campsite.location.contains(appliedFilters.location, true)) &&
                (appliedFilters.keywords.isBlank() ||
                        campsite.name.contains(appliedFilters.keywords, true) ||
                        campsite.location.contains(appliedFilters.keywords, true) ||
                        campsite.keywords.contains(appliedFilters.keywords, true)) &&
                (appliedFilters.siteType.isBlank() ||
                        campsite.siteType.contains(appliedFilters.siteType, true)) &&
                (minCapacity == null || campsite.capacity >= minCapacity) &&
                (maxCapacity == null || campsite.capacity <= maxCapacity) &&
                (appliedFilters.status.isBlank() ||
                        campsite.status.contains(appliedFilters.status, true)) &&
                (minRating == null || campsite.rating >= minRating) }

    // Applies the same text, cursor, and border colors to the filter fields.
    val filterFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Cream,
        unfocusedTextColor = Cream,
        focusedLabelColor = Cream,
        unfocusedLabelColor = Cream.copy(alpha = 0.7f),
        cursorColor = Cream,
        focusedBorderColor = Cream,
        unfocusedBorderColor = Cream.copy(alpha = 0.5f))

//Scaffold gives a screen a standard layout structure with places for things like a top bar,
// bottom bar, and floating button

    // Sets the screen background and gives the content padding around the scaffold.
    Scaffold(containerColor = DarkGreen) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 16.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) { Text(
                text = "Campsites",
                style = MaterialTheme.typography.titleLarge,
                color = Gold
            )

                TextButton(
                    modifier = Modifier.testTag("campsite-filter-button"),
                    // Starts a new filter edit using the filters that are already applied.
                    onClick = {
                        draftFilters = appliedFilters
                        showFilters = true
                    }) { Text("Filter", color = Gold) } }

            // Shows different content depending on whether loading succeeded or found results.
            if (isLoading) {
                Text("Loading campsites...", color = Cream)
            } else if (loadError != null) {
                Text(loadError ?: "Could not load campsites.", color = Cream)
            } else if (visibleCampsites.isEmpty()) {
                Text(
                    text = "No campsites match these filters.",
                    color = Cream,
                    modifier = Modifier.padding(20.dp)
                )
            }
            else {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 20.dp,
                        vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    // Creates one card for each campsite that passed the filters.
                    items(visibleCampsites) { campsite ->
                        CampsiteCard(campsite) {
                            selectedCampsite = campsite } } } } } }

    // Shows the filter dialog while the admin chooses campsite search options.
    if (showFilters) {
        AlertDialog(
            onDismissRequest = { showFilters = false },
            containerColor = TealCard,
            title = {
                Text("Filter campsites", color = Cream)
            },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 450.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp))
                { OutlinedTextField(
                    modifier = Modifier.testTag("campsite-location-field"),
                    value = draftFilters.location,

                    onValueChange = {
                        draftFilters = draftFilters.copy(location = it)
                    },
                    label = { Text("Location") },
                    singleLine = true,colors = filterFieldColors)

                    OutlinedTextField(modifier = Modifier.testTag("campsite-keywords-field"),
                        value = draftFilters.keywords,
                        onValueChange = {
                            draftFilters = draftFilters.copy(keywords = it)
                        },
                        label = { Text("Keywords") },
                        singleLine = true,colors = filterFieldColors)

                    OutlinedTextField(
                        value = draftFilters.siteType,
                        onValueChange = {
                            draftFilters = draftFilters.copy(siteType = it) },
                        label = { Text("Site type") },
                        singleLine = true,colors = filterFieldColors)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            value = draftFilters.minCapacity,
                            onValueChange = {
                                draftFilters = draftFilters.copy(minCapacity = it) },
                            label = { Text("Min capacity") },
                            singleLine = true,colors = filterFieldColors)

                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            value = draftFilters.maxCapacity,
                            onValueChange = {
                                draftFilters = draftFilters.copy(maxCapacity = it) },
                            label = { Text("Max capacity") },
                            singleLine = true,colors = filterFieldColors) }

                    OutlinedTextField(
                        value = draftFilters.status,
                        onValueChange = {
                            draftFilters = draftFilters.copy(status = it) },
                        label = { Text("Status (Open or Closed)") },
                        singleLine = true,colors = filterFieldColors)

                    OutlinedTextField(
                        value = draftFilters.minRating,
                        onValueChange = {
                            draftFilters = draftFilters.copy(minRating = it) },
                        label = { Text("Minimum rating (1–5)") },
                        singleLine = true,colors = filterFieldColors) } },
            // Saves the draft values as the active filters when Apply is tapped.
            confirmButton = {
                TextButton(modifier = Modifier.testTag("apply-campsite-filters"),
                    onClick = {
                        appliedFilters = draftFilters
                        showFilters = false
                    }) {
                    Text("Apply Filters", color = Gold) } },
            dismissButton = {
                TextButton(onClick = { showFilters = false }) {
                    Text("Cancel", color = Cream) } }) }

    // Shows the details dialog for the campsite whose card was tapped.
    selectedCampsite?.let { campsite ->
        AlertDialog(
            onDismissRequest = { selectedCampsite = null },
            containerColor = TealCard,
            title = {
                Text(campsite.name, color = Cream) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Location: ${campsite.location}", color = Cream)
                    Text("Owner: ${campsite.owner}", color = Cream)
                    Text("Rating: ★ ${campsite.rating}", color = Gold)
                    Text("Site type: ${campsite.siteType}", color = Cream)
                    Text("Capacity: ${campsite.capacity}", color = Cream)
                    Text("Status: ${campsite.status}", color = Cream) } },
            dismissButton = {
                TextButton(onClick = {
                    // Opens the confirmation before deleting this campsite.
                    campsiteToDelete = campsite
                    deleteError = null
                    selectedCampsite = null }) {
                    Text("Delete campsite", color = Gold) } },
            confirmButton = {
                TextButton(onClick = { selectedCampsite = null }) {
                    Text("Close", color = Gold) } }) }

    // This asks the admin to confirm the delete before it is sent to Firebase.
    // campsite is the campsite selected for deletion in the details dialog.
    campsiteToDelete?.let { campsite ->
        AlertDialog(
            onDismissRequest = {
                campsiteToDelete = null
                deleteError = null },
            containerColor = TealCard,
            title = {
                Text("Delete campsite?", color = Cream) },
            text = {
                Column {
                    Text("Delete ${campsite.name} from the campsites?",
                        color = Cream)
                    // Shows the Firebase error in the dialog if there is one.
                    deleteError?.let { error ->
                        Text(error, color = Cream) } } },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Deletes this campsite document from Firestore.
                        Firebase.firestore
                            .collection("campsites")
                            .document(campsite.campsiteId)
                            .delete()
                            .addOnSuccessListener {
                                // Takes the campsite off the screen after Firebase deletes it.
                                campsites = campsites.filterNot {
                                    it.campsiteId == campsite.campsiteId
                                }
                                campsiteToDelete = null
                                deleteError = null
                            }
                            .addOnFailureListener { error ->
                                // Shows the error if the delete does not work.
                                deleteError =
                                    error.localizedMessage ?: "Could not delete campsite." } }) {
                    Text("Delete", color = Gold) } },
            dismissButton = {
                TextButton(
                    onClick = {
                        // Cancel closes this dialog without deleting the campsite.
                        campsiteToDelete = null
                        deleteError = null }) {
                    Text("Cancel", color = Cream) } }) } }

//Shows one campsite card with its rating, name, location, and details button.
// campsite contains the values displayed on the card.
// onDetailsClick is called when the details button is tapped.
// This composable displays the card UI; the parent screen handles the click result.
@Composable
private fun CampsiteCard(
    campsite: CampsitePreview,
    onDetailsClick: () -> Unit
) { Card(
    modifier = Modifier
        .width(290.dp)
        .height(460.dp),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = TealCard)) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF547B75),
                        Color(0xFF294B47),
                        Color(0xFF172D2C))))) {
        Text(
            text = "Campsite photo",
            color = Cream,
            modifier = Modifier.align(Alignment.Center))
        Text(
            text = "★ ${campsite.rating}",
            color = DarkGreen,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(Gold, RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp))
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Color(0x990F2221))
                .padding(18.dp))
        {  Text(
            text = campsite.name,
            style = MaterialTheme.typography.headlineSmall,
            color = Cream)

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = campsite.location,
                style = MaterialTheme.typography.bodyMedium,
                color = Cream)
            Button(
                onClick = onDetailsClick,
                modifier = Modifier
                    .testTag("campsite-details-${campsite.name}")
                    .align(Alignment.End)
                    .padding(top = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gold,
                    contentColor = DarkGreen
                ))
            { Text("→") } } } } }


//Keeps the campsite information used by the screen.
// campsiteId is the Firestore document ID used when deleting this campsite.
// name and location are shown on the card and details dialog.
// rating is the average rating, and owner is the campsite owner's ID.
// keywords, siteType, capacity, and status are also used by the filters or details.
private data class CampsitePreview(
    val campsiteId: String,
    val name: String,
    val location: String,
    val rating: Double,
    val owner: String,
    val keywords: String = "",
    val siteType: String = "",
    val capacity: Int = 0,
    val status: String = ""
)
//Stores the filter values that the admin enters.
// location, keywords, siteType, and status are matched against campsite details.
// minCapacity, maxCapacity, and minRating are Strings because they come from text fields.
// Empty filter values mean that the filter should not restrict the results.
private data class CampsiteFilters(
    val location: String = "",
    val keywords: String = "",
    val siteType: String = "",
    val minCapacity: String = "",
    val maxCapacity: String = "",
    val status: String = "",
    val minRating: String = ""
)
private val DarkGreen = Color(0xFF1B3232)
private val TealCard = Color(0xFF244746)
private val Gold = Color(0xFFD6A266)
private val Cream = Color(0xFFFFFBF5)
//Shows the campsite screen preview in Android Studio.
// The preview calls the screen with its default state and does not need parameters.
@Preview(showBackground = true)
@Composable
private fun AdminCampsitesScreenPreview() {
    BirchBarkTheme {
        AdminCampsitesScreen()
    }
}