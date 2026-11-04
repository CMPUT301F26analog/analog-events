package com.example.birchbark

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

private enum class AdminSection {
    CAMPSITES,
    PROFILES,
    LOGS
}

/**
 * Shows the admin browsing tabs and the signed-in admin.
 */
@Composable
fun AdminBrowseScreen(adminName: String = currentAdminName()) {
    var selectedSection by rememberSaveable {
        mutableStateOf(AdminSection.CAMPSITES)
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = DarkGreen) {
                NavigationBarItem(
                    selected = selectedSection == AdminSection.CAMPSITES,
                    onClick = { selectedSection = AdminSection.CAMPSITES },
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
                    colors = navigationItemColors()
                )

                NavigationBarItem(
                    selected = selectedSection == AdminSection.PROFILES,
                    onClick = { selectedSection = AdminSection.PROFILES },
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
                    selected = selectedSection == AdminSection.LOGS,
                    onClick = { selectedSection = AdminSection.LOGS },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkGreen)
                .padding(padding)
        ) {
            AdminUserHeader(adminName)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedSection) {
                    AdminSection.CAMPSITES -> AdminCampsitesScreen()
                    AdminSection.PROFILES -> AdminProfilesScreen()
                    AdminSection.LOGS -> PlaceholderScreen("Logs")
                }
            }
        }
    }
}

@Composable
private fun navigationItemColors() =
    NavigationBarItemDefaults.colors(
        selectedIconColor = DarkGreen,
        selectedTextColor = Gold,
        indicatorColor = Gold,
        unselectedIconColor = MutedText,
        unselectedTextColor = MutedText
    )

@Composable
private fun AdminUserHeader(name: String) {
    val initials = name
        .split(" ")
        .mapNotNull { it.firstOrNull() }
        .take(2)
        .joinToString("")
        .uppercase()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFF526B64), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(initials, color = Cream)
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text("Welcome", color = MutedText)
            Text(name, color = Cream)
        }
    }
}

@Composable
private fun PlaceholderScreen(sectionName: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("$sectionName screen goes here", color = Cream)
    }
}

private fun currentAdminName(): String {
    val user = try {
        Firebase.auth.currentUser
    } catch (_: IllegalStateException) {
        null
    }

    return user?.displayName?.takeIf { it.isNotBlank() }
        ?: user?.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
        ?: "Admin"
}

private val DarkGreen = Color(0xFF1B3232)
private val Gold = Color(0xFFD6A266)
private val Cream = Color(0xFFF5EADD)
private val MutedText = Color(0xFFB8C3BE)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AdminBrowseScreenPreview() {
    BirchBarkTheme {
        AdminBrowseScreen(adminName = "Sara Safari")
    }
}