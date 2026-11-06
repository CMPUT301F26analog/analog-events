package com.example.birchbark
import androidx.compose.runtime.LaunchedEffect
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.birchbark.ui.theme.BirchBarkTheme

//Displays user profiles loaded from the Firestore `users` collection.
//Loads campsites from `campsites` and matches each campsite's `ownerId`
//to a user document ID. Shows loading and error messages, lets the admin
//filter and sort profiles, and opens a profile's details when its card is tapped.
//This composable has no parameters because it loads its information from Firebase.
//It also keeps the current filters, selected profile, and dialog state while it is shown.
@Composable
fun AdminProfilesScreen() {
    // This holds the profile that the admin picked to delete.
    var profileToDelete by remember {
        mutableStateOf<ProfilePreview?>(null)
    }
    // This is used to show an error if Firebase cannot delete the profile.
    var deleteError by remember { mutableStateOf<String?>(null) }
    var profiles by remember { mutableStateOf(emptyList<ProfilePreview>()) }
    var campsiteData by remember { mutableStateOf(emptyList<Pair<String, String>>()) }
    // Tracks whether the profile and campsite information is still being loaded.
    var isLoading by remember { mutableStateOf(true) }
    // Stores a message to show if either Firebase request fails.
    var loadError by remember { mutableStateOf<String?>(null) }
    // Runs the Firebase requests when this screen first appears.
    LaunchedEffect(Unit) {
        val db = Firebase.firestore

        // Campsites are loaded first so their owner IDs can be matched to users.
        db.collection("campsites")
            .get()
            .addOnSuccessListener { campsiteResult ->
                val campsiteByOwnerId = campsiteResult.documents.mapNotNull { campsite ->
                    val ownerId = campsite.getString("ownerId")
                        ?: return@mapNotNull null
                    val name = campsite.getString("name")
                        ?: return@mapNotNull null
                    val location = campsite.getString("location") ?: ""

                    ownerId to (name to location)
                }.toMap()

                campsiteData = campsiteResult.documents.mapNotNull { campsite ->
                    val name = campsite.getString("name")
                        ?: return@mapNotNull null
                    name to (campsite.getString("location") ?: "")
                }

                // Each users document ID is compared with campsite ownerId values.
                db.collection("users")
                    .get()
                    .addOnSuccessListener { userResult ->
                        profiles = userResult.documents.map { user ->
                            val ownedCampsite = campsiteByOwnerId[user.id]
                            val roles = (user.get("roles") as? List<*>)
                                ?.filterIsInstance<String>()
                                .orEmpty()

                            ProfilePreview(
                                userId = user.id,
                                name = listOfNotNull(
                                    user.getString("firstName"),
                                    user.getString("lastName")
                                ).joinToString(" ").ifBlank { "Unnamed user" },
                                email = user.getString("email") ?: "",
                                phone = user.getString("phoneNumber") ?: "",
                                role = roles.joinToString(", ").ifBlank { "Unknown" },
                                campsite = ownedCampsite?.first ?: "",
                                area = ownedCampsite?.second ?: "") }
                        isLoading = false }
                    .addOnFailureListener { error ->
                        loadError = error.localizedMessage ?: "Could not load profiles."
                        isLoading = false } }
            .addOnFailureListener { error ->
                loadError = error.localizedMessage ?: "Could not load campsites."
                isLoading = false } }

    // Builds the campsite and area choices shown in the filter dropdowns.
    val campsiteOptions = listOf("All campsites") +
            campsiteData.map { it.first }.distinct()

    val areaOptions = listOf("All areas") +
            campsiteData.map { it.second }.filter { it.isNotBlank() }.distinct()
    var appliedFilters by remember { mutableStateOf(ProfileFilters()) }
    var draftFilters by remember { mutableStateOf(ProfileFilters()) }
    var showFilters by remember { mutableStateOf(false) }
    var selectedProfile by remember {
        mutableStateOf<ProfilePreview?>(null) }



    // Keeps profiles that match the campsite and area filters selected by the admin.
    val matchingProfiles = profiles.filter { profile -> (appliedFilters.campsite == "All campsites" ||
            profile.campsite == appliedFilters.campsite) &&
            (appliedFilters.area == "All areas" ||
                    profile.area == appliedFilters.area) }

    // Sorts the filtered profiles by first or last name and applies the chosen order.
    val sortedProfiles = when (appliedFilters.sortBy) {
        "Last name" -> matchingProfiles.sortedBy { it.name.substringAfterLast(" ") }

        else -> matchingProfiles.sortedBy { it.name.substringBefore(" ") }
    }.let { profilesInOrder ->
        if (appliedFilters.sortOrder == "Z–A") { profilesInOrder.reversed() }
        else { profilesInOrder }
    }

    // Provides the screen background and the padding used by its content.
    Scaffold(containerColor = ProfileBackground) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 16.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) { Text(text = "Profiles",
            style = MaterialTheme.typography.titleLarge,
            color = ProfileGold)

            TextButton(onClick = {
                draftFilters = appliedFilters
                showFilters = true
            }) { Text("Filter", color = ProfileGold) } }

            if (isLoading) {
                Text("Loading profiles...", color = ProfileCream)
            } else if (loadError != null) {
                Text(loadError ?: "Could not load profiles.", color = ProfileCream)
            } else if (sortedProfiles.isEmpty()) {
                Text(
                    "No profiles match these filters.",
                    color = ProfileCream,
                    modifier = Modifier.padding(20.dp)
                )
            }
            else {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        horizontal = 20.dp,
                        vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    items(sortedProfiles) { profile ->
                        ProfileCard(profile) {
                            selectedProfile = profile } } } } } }

    // Shows the filter dialog while the admin is choosing profile filters.
    if (showFilters) {
        AlertDialog(
            onDismissRequest = { showFilters = false },
            containerColor = ProfileCardColor,
            title = {
                Text("Filter profiles", color = ProfileCream) },
            text = { Column(
                modifier = Modifier
                    .heightIn(max = 450.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Sort by", color = ProfileCream)

                ProfileFilterDropdown(
                    label = "Name",
                    value = draftFilters.sortBy,
                    options = listOf("First name", "Last name"),
                    onSelect = {
                        draftFilters = draftFilters.copy(sortBy = it) })

                ProfileFilterDropdown(
                    label = "Order",
                    value = draftFilters.sortOrder,
                    options = listOf("A–Z", "Z–A"),
                    onSelect = {
                        draftFilters = draftFilters.copy(sortOrder = it)})

                Text("Campsite", color = ProfileCream)

                ProfileFilterDropdown(
                    label = "Choose campsite",
                    value = draftFilters.campsite,
                    options = campsiteOptions,
                    onSelect = { draftFilters = draftFilters.copy(campsite = it) })

                Text("Area", color = ProfileCream)

                ProfileFilterDropdown(
                    label = "Choose area",
                    value = draftFilters.area,
                    options = areaOptions,
                    onSelect = {
                        draftFilters = draftFilters.copy(area = it) })} },
            confirmButton = {
                TextButton(onClick = {
                    appliedFilters = draftFilters
                    showFilters = false }) { Text("Apply Filters", color = ProfileGold) }
            },

            dismissButton = {
                TextButton(onClick = {
                    showFilters = false
                }) {
                    Text("Cancel", color = ProfileCream) } }) }

    // Shows details for the profile whose card was selected.
    selectedProfile?.let { profile ->
        AlertDialog(
            onDismissRequest = { selectedProfile = null },
            containerColor = ProfileCardColor,
            title = {
                Text(profile.name, color = ProfileCream)
            },
            text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Email: ${profile.email}", color = ProfileCream)
                Text("Phone: ${profile.phone}", color = ProfileCream)
                Text("Role: ${profile.role}", color = ProfileCream)
                Text("Campsite: ${profile.campsite}", color = ProfileCream)
                Text("Area: ${profile.area}", color = ProfileCream) } },

            dismissButton = {
                TextButton(onClick = {
                    // Opens the delete confirmation for this profile.
                    profileToDelete = profile
                    deleteError = null
                    selectedProfile = null }) {
                    Text("Delete profile", color = ProfileGold) } },
            confirmButton = {
                TextButton(onClick = { selectedProfile = null }) {
                    Text("Close", color = ProfileGold) } }) }
    // This dialog asks one more time before deleting the profile from Firebase.
    // profile is the profile selected in the details dialog for removal.
    profileToDelete?.let { profile ->
        AlertDialog(
            onDismissRequest = {
                profileToDelete = null
                deleteError = null },
            containerColor = ProfileCardColor,
            title = {
                Text("Delete profile?", color = ProfileCream) },
            text = {
                Column {
                    Text("Delete ${profile.name} from the profiles?",
                        color = ProfileCream)
                    // If Firebase gives an error, it shows up here.
                    deleteError?.let { error ->
                        Text(error, color = ProfileCream) } } },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Deletes the user's document from the Firestore users collection.
                        Firebase.firestore
                            .collection("users")
                            .document(profile.userId)
                            .delete()
                            .addOnSuccessListener {
                                // The profile disappears from the screen after Firebase says it worked.
                                profiles = profiles.filterNot {
                                    it.userId == profile.userId
                                }
                                profileToDelete = null
                                deleteError = null
                            }
                            .addOnFailureListener { error ->
                                // Keeps the dialog open and shows the error if the delete fails.
                                deleteError =
                                    error.localizedMessage ?: "Could not delete profile." } }) {
                    Text("Delete", color = ProfileGold) } },
            dismissButton = {
                TextButton(
                    onClick = {
                        // Cancel closes the dialog and does not delete the profile.
                        profileToDelete = null
                        deleteError = null }) {
                    Text("Cancel", color = ProfileCream) } }) }

}

//Displays a read-only dropdown field for choosing a profile filter.
// label is the text shown above the field.
// value is the option currently selected and displayed in the field.
// options is the list of choices that appears when the field is tapped.
// onSelect is called with the chosen option so the caller can update its filter.
// The function returns a dropdown field and menu as part of the Compose UI.
@Composable
private fun ProfileFilterDropdown(
    label: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit
)

{
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Text("⌄", color = ProfileCream)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ProfileCream,
                unfocusedTextColor = ProfileCream,
                focusedLabelColor = ProfileCream,
                unfocusedLabelColor = ProfileCream.copy(alpha = 0.7f),
                cursorColor = ProfileCream,
                focusedBorderColor = ProfileCream,
                unfocusedBorderColor = ProfileCream.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // This layer opens the choices when you tap the field.
        Box(modifier = Modifier
            .matchParentSize()
            .clickable { expanded = true })

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(ProfileCardColor)
        ) { options.forEach { option ->
            DropdownMenuItem(
                text = { Text(option, color = ProfileCream) },
                onClick = {
                    onSelect(option)
                    expanded = false }) } } } }

//Displays a profile's initials and name on a card.
// profile contains the information displayed on this card.
// onDetailsClick is called when the details button is tapped.
// The function returns the card UI; the selected profile is handled by its caller.
@Composable
private fun ProfileCard(
    profile: ProfilePreview,
    onDetailsClick: () -> Unit
) { Card(   modifier = Modifier
    .width(290.dp)
    .height(460.dp),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(
        containerColor = ProfileTeal)) {
    Box(modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(ProfileCream, CircleShape),
                contentAlignment = Alignment.Center)

            {
                Text(
                    text = profile.name
                        .split(" ")
                        .mapNotNull { it.firstOrNull() }
                        .joinToString(""),
                    style = MaterialTheme.typography.headlineMedium,
                    color = ProfileBackground) }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = profile.name,
                style = MaterialTheme.typography.titleLarge,
                color = ProfileCream)

            Button(
                onClick = onDetailsClick,
                modifier = Modifier.padding(top = 24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ProfileGold,
                    contentColor = ProfileBackground))
            { Text("→") } } } } }
//Holds the profile details used by the admin profiles screen.
// userId is the Firestore document ID used to identify this user.
// name, email, and phone are the user's contact details shown to the admin.
// role is the user's role or roles; campsite and area describe an owned campsite.
private data class ProfilePreview(
    val userId: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: String,
    val campsite: String,
    val area: String
)
//Holds the current campsite, area, and sorting choices for the profiles list.
// The default values mean no campsite or area filter is applied.
private data class ProfileFilters(
    val campsite: String = "All campsites",
    val area: String = "All areas",
    val sortBy: String = "First name",
    val sortOrder: String = "A–Z"
)

private val ProfileBackground = Color(0xFF1B3232)
private val ProfileCardColor = Color(0xFF244746)
private val ProfileTeal = Color(0xFF168F83)
private val ProfileGold = Color(0xFFD6A266)
private val ProfileCream = Color(0xFFFFFBF5)
//Previews the admin profiles screen in Android Studio.
// It uses the screen's normal default state without needing a signed-in Firebase user.
@Preview(showBackground = true)
@Composable
private fun AdminProfilesScreenPreview() {
    BirchBarkTheme { AdminProfilesScreen() } }