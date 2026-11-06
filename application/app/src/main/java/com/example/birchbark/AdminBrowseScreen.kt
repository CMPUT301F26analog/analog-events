package com.example.birchbark

import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.birchbark.ui.theme.BirchBarkTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

// Lists the sections that can be opened from the admin navigation bar.
private enum class AdminSection {
    CAMPSITES,
    PROFILES,
    LOGS
}

// Shows the admin browsing screen, including the admin header and bottom tabs.
// adminName is the name displayed in the header.
// It uses the signed-in user's name by default if no name is passed in.
@Composable
fun AdminBrowseScreen(adminName: String = currentAdminName()) {
    // Saves the selected tab when the screen is recreated, such as after rotation.
    var selectedSection by rememberSaveable { mutableStateOf(AdminSection.CAMPSITES) }

    // Scaffold provides the main screen layout and a place for the bottom navigation bar.
    Scaffold(
        bottomBar = {
            // Shows the tabs the admin can use to switch between sections.
            NavigationBar(containerColor = DarkGreen) {
                NavigationBarItem(
                    // Gives this tab a tag that can be used by UI tests.
                    modifier = Modifier.testTag("admin-campsites-tab"),
                    selected = selectedSection == AdminSection.CAMPSITES,
                    // Changes the selected section to Campsites when this tab is tapped.
                    onClick = { selectedSection = AdminSection.CAMPSITES },
                    // Shows the icon for the Campsites tab.
                    icon = {
                        Text(
                            "⌂",
                            color = if (selectedSection == AdminSection.CAMPSITES) {
                                DarkGreen
                            } else {
                                MutedText
                            }
                        )
                    },
                    label = { Text("Campsites") },
                    // Uses the same selected and unselected colors as the other tabs.
                    colors = navigationItemColors()
                )

                NavigationBarItem(
                    // Gives this tab a tag that can be used by UI tests.
                    modifier = Modifier.testTag("admin-profiles-tab"),
                    selected = selectedSection == AdminSection.PROFILES,
                    // Changes the selected section to Profiles when this tab is tapped.
                    onClick = { selectedSection = AdminSection.PROFILES },
                    // Shows the icon for the Profiles tab.
                    icon = {
                        Text(
                            "○",
                            color = if (selectedSection == AdminSection.PROFILES) {
                                DarkGreen
                            } else {
                                MutedText
                            }
                        )
                    },
                    label = { Text("Profiles") },
                    colors = navigationItemColors()
                )

                NavigationBarItem(
                    // Gives this tab a tag that can be used by UI tests.
                    modifier = Modifier.testTag("admin-logs-tab"),
                    selected = selectedSection == AdminSection.LOGS,
                    // Changes the selected section to Logs when this tab is tapped.
                    onClick = { selectedSection = AdminSection.LOGS },
                    // Shows the icon for the Logs tab.
                    icon = {
                        Text(
                            "▤",
                            color = if (selectedSection == AdminSection.LOGS) {
                                DarkGreen
                            } else {
                                MutedText
                            }
                        )
                    },
                    label = { Text("Logs") },
                    colors = navigationItemColors()
                )
            }
        }
    ) { padding ->
        // Applies the padding provided by Scaffold so content does not overlap the navigation bar.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkGreen)
                .padding(padding)
        ) {
            // Displays the admin's initials and name at the top of the screen.
            AdminUserHeader(adminName)

            // Uses the remaining screen space to show the currently selected section.
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Opens the screen that matches the selected navigation tab.
                when (selectedSection) {
                    AdminSection.CAMPSITES -> AdminCampsitesScreen()
                    AdminSection.PROFILES -> AdminProfilesScreen()
                    AdminSection.LOGS -> PlaceholderScreen("Logs")
                }
            }
        }
    }
}

// Returns the colors used for selected and unselected navigation items.
// There are no parameters; the function returns the navigation item's color settings.
@Composable
private fun navigationItemColors() =
    NavigationBarItemDefaults.colors(
        // Sets the color of the selected tab's icon.
        selectedIconColor = DarkGreen,
        // Sets the color of the selected tab's label.
        selectedTextColor = Gold,
        // Sets the background color behind the selected tab.
        indicatorColor = Gold,
        // Sets the icon color for tabs that are not selected.
        unselectedIconColor = MutedText,
        // Sets the label color for tabs that are not selected.
        unselectedTextColor = MutedText
    )

// Shows the admin's initials, welcome message, and name.
// name is the admin's name that will appear in the header.
@Composable
private fun AdminUserHeader(name: String) {
    // Takes the first letter of up to two words in the name to make the initials.
    val initials = name
        .split(" ")
        .mapNotNull { it.firstOrNull() }
        .take(2)
        .joinToString("")
        .uppercase()

    // Places the initials circle beside the welcome text and name.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Shows the admin's initials inside a circular background.
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFF526B64), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(initials, color = Cream)
        }

        // Adds space between the initials circle and the text.
        Spacer(modifier = Modifier.width(10.dp))

        // Shows the welcome message and admin name in a column.
        Column {
            Text("Welcome", color = MutedText)
            Text(name, color = Cream)
        }
    }
}

// Shows a temporary message for a section that has not been implemented yet.
// sectionName is the name shown in the message.
@Composable
private fun PlaceholderScreen(sectionName: String) {
    // Centers the temporary message in the available screen area.
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("$sectionName screen goes here", color = Cream)
    }
}

// Gets the signed-in user's display name from Firebase Authentication.
// If there is no display name, it uses the part of their email before the @.
// If neither value is available, it returns "Admin".
private fun currentAdminName(): String {
    // Firebase auth may be unavailable in some preview or test situations.
    val user = try {
        Firebase.auth.currentUser
    } catch (_: IllegalStateException) {
        null
    }

    // Picks the first available non-blank name, email username, or default name.
    return user?.displayName?.takeIf { it.isNotBlank() }
        ?: user?.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
        ?: "Admin"
}

// Colors used by the admin browse screen.
private val DarkGreen = Color(0xFF1B3232)
private val Gold = Color(0xFFD6A266)
private val Cream = Color(0xFFF5EADD)
private val MutedText = Color(0xFFB8C3BE)

// Creates a sample admin browse screen in Android Studio's preview.
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AdminBrowseScreenPreview() {
    BirchBarkTheme {
        // Uses a sample name so the preview can show the header without Firebase sign-in.
        AdminBrowseScreen(adminName = "Sara Safari")
    }
}