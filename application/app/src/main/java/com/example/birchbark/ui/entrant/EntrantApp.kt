package com.example.birchbark.ui.entrant

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.birchbark.UserProfile
import com.example.birchbark.ui.theme.Avatar
import com.example.birchbark.ui.theme.EmptyState
import com.example.birchbark.ui.theme.MenuRow
import com.example.birchbark.ui.theme.ScreenTitle
import com.example.birchbark.ui.theme.UserHeader
import com.example.birchbark.ui.notifications.NotificationSettingsScreen
import com.example.birchbark.ui.screens.BlankEntrantScreen
import com.example.birchbark.ui.theme.BirchBarkColors

/**
 * The entrant's whole app: a header, the current tab, and the bottom navigation.
 * Use this in MainActivity instead of EntrantCampsitesApp. The Browse tab shows
 * EntrantCampsitesApp, so searching, filtering and campsite details work as before.
 *
 * Outstanding issues. The Home tab only shows the empty "Your Lotteries" message,
 * it needs the lotteries the entrant has joined. Bookmarks isn't a user story
 * yet, so it's a placeholder. The bell in the header is hidden until the
 * notifications screen exists. The reservations list has to be loaded by the caller.
 *
 * @param profile the signed-in entrant's profile.
 * @param onProfileDeleted called after the entrant deletes their profile.
 * @param reservations the entrant's reservations for the booking list.
 */
@Composable
fun EntrantApp(
    profile: UserProfile,
    onProfileDeleted: () -> Unit,
    reservations: List<ReservationListItem> = emptyList()
) {
    var currentProfile by remember { mutableStateOf(profile) } // TBD
    var tab by rememberSaveable { mutableStateOf(EntrantTab.HOME) }
    var profilePage by rememberSaveable { mutableStateOf(ProfilePage.MENU) }

    val name = listOf(currentProfile.firstName, currentProfile.lastName) // changed to var TBD
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .ifBlank { "Entrant" }

    // See [TBD 1] in LLM.md
    Scaffold(
        containerColor = BirchBarkColors.DarkGreen,
        //MainActivity already leaves room for the status bar
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            EntrantBottomBar(
                selected = tab,
                onSelect = {
                    tab = it
                    profilePage = ProfilePage.MENU
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            //the profile tab shows who you are in its own page
            if (tab != EntrantTab.PROFILE) {
                UserHeader(name)
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (tab) {
                    EntrantTab.HOME -> YourLotteries()
                    EntrantTab.BOOKMARKS -> EmptyState(
                        title = "Nothing saved yet",
                        hint = "Campsites you bookmark will show up here",
                        modifier = Modifier.fillMaxSize()
                    )
                    EntrantTab.BROWSE -> EntrantCampsitesApp()
                    EntrantTab.MESSAGES -> BlankEntrantScreen(onBack = { tab = EntrantTab.HOME })
                    EntrantTab.PROFILE -> ProfileTab(
                        profile = currentProfile,
                        name = name,
                        page = profilePage,
                        onPage = { profilePage = it },
                        reservations = reservations,
                        onProfileUpdated = { currentProfile = it },
                        onProfileDeleted = onProfileDeleted
                    )
                }
            }
        }
    }
}

// See [TBD 2] in LLM.md
/** The bottom navigation bar, styled like the admin one (gold selected tab). */
@Composable
private fun EntrantBottomBar(selected: EntrantTab, onSelect: (EntrantTab) -> Unit) {
    NavigationBar(containerColor = BirchBarkColors.DarkGreen) {
        for (tab in EntrantTab.entries) {
            NavigationBarItem(
                modifier = Modifier.testTag("entrant-${tab.name.lowercase()}-tab"),
                selected = selected == tab,
                onClick = { onSelect(tab) },
                icon = {
                    Text(
                        tab.icon,
                        color = if (selected == tab) BirchBarkColors.DarkGreen else BirchBarkColors.MutedText
                    )
                },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BirchBarkColors.DarkGreen,
                    selectedTextColor = BirchBarkColors.Gold,
                    indicatorColor = BirchBarkColors.Gold,
                    unselectedIconColor = BirchBarkColors.MutedText,
                    unselectedTextColor = BirchBarkColors.MutedText
                )
            )
        }
    }
}

// TBD
/** The Home tab, "Your Lotteries" in the mockup. Only the empty message for now. */
@Composable
private fun YourLotteries() {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        ScreenTitle("Your Lotteries")
        EmptyState(
            title = "No current lotteries",
            hint = "Try finding one to join",
            modifier = Modifier.weight(1f).fillMaxWidth()
        )
    }
}

// See [TBD 3] in LLM.md
/** The Profile tab: a menu, and the pages it opens. System back returns to the menu. */
@Composable
private fun ProfileTab(
    profile: UserProfile,
    name: String,
    page: ProfilePage,
    onPage: (ProfilePage) -> Unit,
    reservations: List<ReservationListItem>,
    onProfileUpdated: (UserProfile) -> Unit,
    onProfileDeleted: () -> Unit
) {
    BackHandler(enabled = page != ProfilePage.MENU) { onPage(ProfilePage.MENU) }

    when (page) {
        ProfilePage.MENU -> ProfileMenu(profile, name, onPage)
        ProfilePage.EDIT -> EditProfileScreen(
            onBack = { onPage(ProfilePage.MENU) },
            onProfileUpdated = onProfileUpdated,
            onDeleted = onProfileDeleted
        )
        ProfilePage.RESERVATIONS -> Column(modifier = Modifier.fillMaxSize()) {
            TextButton(onClick = { onPage(ProfilePage.MENU) }) {
                Text("Back", color = BirchBarkColors.Gold)
            }
            EntrantReservationsScreen(reservations = reservations)
        }
        // TBD
        ProfilePage.WAITLIST -> BlankEntrantScreen(
            title = "Current Waitlists",
            onBack = { onPage(ProfilePage.MENU) }
        )
        //US 01.10.01, the notifications on/off switch
        ProfilePage.SETTINGS -> Column(modifier = Modifier.fillMaxSize()) {
            TextButton(onClick = { onPage(ProfilePage.MENU) }) {
                Text("Back", color = BirchBarkColors.Gold)
            }
            NotificationSettingsScreen()
        }
        // TBD
        ProfilePage.REVIEWS -> BlankEntrantScreen(
            title = "Ratings & Reviews",
            onBack = { onPage(ProfilePage.MENU) }
        )
        // TBD
        ProfilePage.POLICY -> BlankEntrantScreen(
            title = "App Policy & Guidelines",
            onBack = { onPage(ProfilePage.MENU) }
        )
    }
}

/** The list of options on the profile page. */
@Composable
private fun ProfileMenu(profile: UserProfile, name: String, onPage: (ProfilePage) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(name, size = 56.dp)
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = BirchBarkColors.Cream
                )
                Text(profile.email, color = BirchBarkColors.MutedText)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        MenuRow("Your Profile") { onPage(ProfilePage.EDIT) }
        MenuRow("Reservations History") { onPage(ProfilePage.RESERVATIONS) }
        MenuRow("Current Waitlists") { onPage(ProfilePage.WAITLIST  ) }
        MenuRow("Ratings & Reviews") { onPage(ProfilePage.WAITLIST  ) }
        MenuRow("Settings") { onPage(ProfilePage.SETTINGS) }
        MenuRow("App Policy") { onPage(ProfilePage.POLICY) }
    }
}