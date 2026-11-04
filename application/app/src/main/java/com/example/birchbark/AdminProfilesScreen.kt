package com.example.birchbark

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

@Composable
fun AdminProfilesScreen() {
// List of the Profiles
    val profiles = listOf(
        ProfilePreview(
            name = "Eliza Lazar",
            email = "eliza@example.com",
            phone = "780-555-0142",
            role = "Entrant",
            campsite = "Pine Ridge",
            area = "Banff"
        ),
        ProfilePreview(
            name = "Anahita Houshyar",
            email = "anahita@example.com",
            phone = "780-555-0186",
            role = "Owner",
            campsite = "Eagle Creek",
            area = "Hinton"
        ),
        ProfilePreview(
            name = "Sam Patel",
            email = "sam@example.com",
            phone = "780-555-0119",
            role = "Entrant",
            campsite = "Willow Lake",
            area = "Jasper"
        )
    )

    var appliedFilters by remember { mutableStateOf(ProfileFilters()) }
    var draftFilters by remember { mutableStateOf(ProfileFilters()) }
    var showFilters by remember { mutableStateOf(false) }
    var selectedProfile by remember {
        mutableStateOf<ProfilePreview?>(null)
    }
//Variables used for the campsite filtering
    val campsiteOptions =
        listOf("All campsites") +
                SampleData.campsites.map { it.name }.distinct()
    val areaOptions =
        listOf("All areas") + profiles.map { it.area }.distinct()

    val matchingProfiles = profiles.filter { profile -> (appliedFilters.campsite == "All campsites" ||
                profile.campsite == appliedFilters.campsite) &&
                (appliedFilters.area == "All areas" ||
                        profile.area == appliedFilters.area) }

    val sortedProfiles = when (appliedFilters.sortBy) {
        "Last name" -> matchingProfiles.sortedBy {
            it.name.substringAfterLast(" ")
        }

        else -> matchingProfiles.sortedBy {
            it.name.substringBefore(" ")
        }
    }.let { profilesInOrder ->
        if (appliedFilters.sortOrder == "Z–A") {
            profilesInOrder.reversed()
        } else {
            profilesInOrder
        }
    }

    Scaffold(containerColor = ProfileBackground) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 16.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Profiles",
                    style = MaterialTheme.typography.titleLarge,
                    color = ProfileGold
                )

                TextButton(onClick = {
                    draftFilters = appliedFilters
                    showFilters = true
                }) {
                    Text("Filter", color = ProfileGold)
                }
            }

            if (sortedProfiles.isEmpty()) {
                Text(
                    text = "No profiles match these filters.",
                    color = ProfileCream,
                    modifier = Modifier.padding(20.dp)
                )
            } else {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        horizontal = 20.dp,
                        vertical = 12.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(sortedProfiles) { profile ->
                        ProfileCard(profile) {
                            selectedProfile = profile
                        }
                    }
                }
            }
        }
    }

    if (showFilters) {
        AlertDialog(
            onDismissRequest = { showFilters = false },
            containerColor = ProfileCardColor,
            title = {
                Text("Filter profiles", color = ProfileCream)
            },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 450.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Sort by", color = ProfileCream)

                    ProfileFilterDropdown(
                        label = "Name",
                        value = draftFilters.sortBy,
                        options = listOf("First name", "Last name"),
                        onSelect = {
                            draftFilters = draftFilters.copy(sortBy = it)
                        }
                    )

                    ProfileFilterDropdown(
                        label = "Order",
                        value = draftFilters.sortOrder,
                        options = listOf("A–Z", "Z–A"),
                        onSelect = {
                            draftFilters = draftFilters.copy(sortOrder = it)
                        }
                    )

                    Text("Campsite", color = ProfileCream)

                    ProfileFilterDropdown(
                        label = "Choose campsite",
                        value = draftFilters.campsite,
                        options = campsiteOptions,
                        onSelect = {
                            draftFilters = draftFilters.copy(campsite = it)
                        }
                    )

                    Text("Area", color = ProfileCream)

                    ProfileFilterDropdown(
                        label = "Choose area",
                        value = draftFilters.area,
                        options = areaOptions,
                        onSelect = {
                            draftFilters = draftFilters.copy(area = it)
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    appliedFilters = draftFilters
                    showFilters = false
                }) {
                    Text("Apply Filters", color = ProfileGold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showFilters = false
                }) {
                    Text("Cancel", color = ProfileCream)
                }
            }
        )
    }

    selectedProfile?.let { profile ->
        AlertDialog(
            onDismissRequest = { selectedProfile = null },
            containerColor = ProfileCardColor,
            title = {
                Text(profile.name, color = ProfileCream)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Email: ${profile.email}", color = ProfileCream)
                    Text("Phone: ${profile.phone}", color = ProfileCream)
                    Text("Role: ${profile.role}", color = ProfileCream)
                    Text("Campsite: ${profile.campsite}", color = ProfileCream)
                    Text("Area: ${profile.area}", color = ProfileCream)
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedProfile = null }) {
                    Text("Close", color = ProfileGold)
                }
            }
        )
    }
}

@Composable
private fun ProfileFilterDropdown(
    label: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
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
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(ProfileCardColor)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = ProfileCream) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ProfileCard(
    profile: ProfilePreview,
    onDetailsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(290.dp)
            .height(460.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = ProfileTeal
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(ProfileCream, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profile.name
                            .split(" ")
                            .mapNotNull { it.firstOrNull() }
                            .joinToString(""),
                        style = MaterialTheme.typography.headlineMedium,
                        color = ProfileBackground
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = ProfileCream
                )

                Button(
                    onClick = onDetailsClick,
                    modifier = Modifier.padding(top = 24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ProfileGold,
                        contentColor = ProfileBackground
                    )
                ) {
                    Text("→")
                }
            }
        }
    }
}

private data class ProfilePreview(
    val name: String,
    val email: String,
    val phone: String,
    val role: String,
    val campsite: String,
    val area: String
)

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

@Preview(showBackground = true)
@Composable
private fun AdminProfilesScreenPreview() {
    BirchBarkTheme {
        AdminProfilesScreen()
    }
}