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
import com.example.birchbark.ui.screens.PolicyScreen
import com.example.birchbark.ui.theme.BirchBarkColors
import com.example.birchbark.model.Campsite
import com.example.birchbark.model.ReviewRepository
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import com.example.birchbark.model.CampsiteRepository
import com.example.birchbark.model.Lottery
import com.example.birchbark.model.LotteryStatus
import com.example.birchbark.model.LotteryStatusCalculator
import com.example.birchbark.model.Reservation
import com.example.birchbark.model.ReservationRepository
import com.example.birchbark.model.ReservationStatus
import com.example.birchbark.model.WaitlistRepository
import com.example.birchbark.ui.theme.ChipKind
import com.example.birchbark.ui.theme.PhotoCard
import com.example.birchbark.ui.theme.StatusChip
import com.example.birchbark.ui.theme.birchBarkButtonColors
import com.example.birchbark.util.Time
import com.example.birchbark.util.ratingLabel
import com.google.firebase.firestore.FirebaseFirestore


/**
 * UI state model representing a single lottery entry displayed to an entrant, combining
 * domain models with derived status metadata and action labels.
 *
 * @property lottery The underlying [Lottery] domain object.
 * @property campsite The associated [Campsite] for the lottery, or `null` if unavailable.
 * @property reservation The entrant's resulting [Reservation] if one exists, or `null` otherwise.
 * @property statusLabel Short text displayed inside the status badge/chip.
 * @property statusKind Visual styling category ([ChipKind]) applied to the status badge.
 * @property statusMessage Detailed descriptive text explaining the entrant's current lottery status.
 * @property actionLabel Label for the primary interactive button, or `null` if no action is available.
 */
private data class EntrantLotteryItem(
    val lottery: Lottery,
    val campsite: Campsite?,
    val reservation: Reservation?,
    val statusLabel: String,
    val statusKind: ChipKind,
    val statusMessage: String,
    val actionLabel: String?
)

// See [TBD 3] in LLM.md
/**
 * Represents a generic, immutable tuple of four values.
 *
 * @param A The type of the [first] value.
 * @param B The type of the [second] value.
 * @param C The type of the [third] value.
 * @param D The type of the [fourth] value.
 * @property first The first value in the tuple.
 * @property second The second value in the tuple.
 * @property third The third value in the tuple.
 * @property fourth The fourth value in the tuple.
 */
private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

/**
 * The entrant's whole app: a header, the current tab, and the bottom navigation.
 * Use this in MainActivity instead of EntrantCampsitesApp. The Browse tab shows
 * EntrantCampsitesApp, so searching, filtering and campsite details work as before.
 *
 * Outstanding issues. The Home tab only shows the empty "Your Lotteries" message,
 * it needs the lotteries the entrant has joined. The bell in the header is hidden until the
 * notifications screen exists. Reservations are loaded by [EntrantReservationsRoute].
 *
 * @param profile the signed-in entrant's profile.
 * @param onProfileDeleted called after the entrant deletes their profile.
 */
@Composable
fun EntrantApp(
    profile: UserProfile,
    onProfileDeleted: () -> Unit
) {
    var currentProfile by remember { mutableStateOf(profile) } // TBD
    var tab by rememberSaveable { mutableStateOf(EntrantTab.HOME) }
    var profilePage by rememberSaveable { mutableStateOf(ProfilePage.MENU) }

    // See [TBD 5] in LLM.md (end marker is // END TBD)
    var browseInitialCampsite by remember { mutableStateOf<Campsite?>(null) }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var loadingData by remember { mutableStateOf(true) }
    var campsitesById by remember { mutableStateOf<Map<String, Campsite>>(emptyMap()) }
    var lotteriesById by remember { mutableStateOf<Map<String, Lottery>>(emptyMap()) }
    var waitlistLotteryIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var rawReservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }

    LaunchedEffect(currentProfile.fid, refreshTrigger) {
        if (currentProfile.fid.isBlank()) {
            loadingData = false
            return@LaunchedEffect
        }
        loadingData = true

        CampsiteRepository().getAllCampsites(object : CampsiteRepository.ListCallback {
            override fun onLoaded(campsites: List<Campsite>) {
                campsitesById = campsites.associateBy { it.id }
            }
            override fun onError(error: Exception) {}
        })

        FirebaseFirestore.getInstance().collection("lotteries").get()
            .addOnSuccessListener { snapshot ->
                lotteriesById = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Lottery::class.java)?.copy(id = doc.id)
                }.associateBy { it.id }
            }

        WaitlistRepository().getWaitlistLotteryIdsForEntrant(
            userId = currentProfile.fid,
            onResult = { ids ->
                waitlistLotteryIds = ids
                loadingData = false
            },
            onFailure = { loadingData = false }
        )

        ReservationRepository().getReservationsForEntrant(
            entrantId = currentProfile.fid,
            onResult = { list -> rawReservations = list },
            onFailure = {}
        )
    }

    val lotteryItems = remember(waitlistLotteryIds, rawReservations, lotteriesById, campsitesById) {
        val now = System.currentTimeMillis()
        val resByLottery = rawReservations.associateBy { it.lotteryId }
        val allLotteryIds = (waitlistLotteryIds + resByLottery.keys).distinct()

        allLotteryIds.mapNotNull { lotteryId ->
            val lottery = lotteriesById[lotteryId] ?: return@mapNotNull null
            val res = resByLottery[lotteryId]
            val campId = res?.campsiteId ?: lottery.campsiteIds.firstOrNull()
            val camp = campId?.let { campsitesById[it] }

            val (label, kind, msg, action) = when {
                res?.status == ReservationStatus.PENDING -> Quad(
                    "Won", ChipKind.ACTIVE, "You won! Pick your dates", "Pick your dates"
                )

                res?.status == ReservationStatus.CONFIRMED -> {
                    val range = if (res.startDate != null && res.endDate != null) {
                        "Reserved ${Time.format(Time.toLocalDate(res.startDate))} to ${
                            Time.format(
                                Time.toLocalDate(res.endDate)
                            )
                        }"
                    } else "Reservation confirmed"
                    Quad("Reserved", ChipKind.ACTIVE, range, "View Reservation")
                }

                res?.status == ReservationStatus.DECLINED -> Quad(
                    "Declined", ChipKind.NEUTRAL, "You declined this offer.", null
                )

                res?.status == ReservationStatus.CANCELLED || res?.status == ReservationStatus.REJECTED -> Quad(
                    "Cancelled",
                    ChipKind.CANCELLED,
                    "This reservation was cancelled.",
                    "View Reservation"
                )

                lottery.drawn -> Quad(
                    "Lost", ChipKind.CANCELLED, "You weren't selected this time.", null
                )

                LotteryStatusCalculator.status(lottery, now) == LotteryStatus.SCHEDULED -> Quad(
                    "Waitlist", ChipKind.NEUTRAL, "You're on the waitlist.", "View Campsite"
                )

                else -> Quad(
                    "Waiting", ChipKind.ACTIVE,
                    "You're in the lottery. Draw after ${Time.format(Time.toLocalDate(lottery.registrationEnd))}",
                    "View Campsite"
                )
            }

            EntrantLotteryItem(lottery, camp, res, label, kind, msg, action)
        }
    }
    // END TBD

    val name = listOf(currentProfile.firstName, currentProfile.lastName) // changed to var TBD
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .ifBlank { "Entrant" }

    // See [TBD 2] in LLM.md
    val photo = remember(currentProfile.profileImage) { currentProfile.profileImage?.toBytes() }

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
                UserHeader(name, imageBytes = photo)
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (tab) {
                    EntrantTab.HOME -> YourLotteries(
                        items = lotteryItems,
                        loading = loadingData,
                        onOpenCampsite = { camp ->
                            browseInitialCampsite = camp
                            tab = EntrantTab.BROWSE
                        },
                        onOpenReservations = {
                            tab = EntrantTab.PROFILE
                            profilePage = ProfilePage.RESERVATIONS
                        }
                    )
                    EntrantTab.BROWSE -> EntrantCampsitesApp(
                        entrantName = name,
                        initialCampsite = browseInitialCampsite,
                        onInitialCampsiteConsumed = { browseInitialCampsite = null }
                    )
                    EntrantTab.MESSAGES -> BlankEntrantScreen(onBack = { tab = EntrantTab.HOME })
                    EntrantTab.PROFILE -> ProfileTab(
                        profile = currentProfile,
                        name = name,
                        page = profilePage,
                        onPage = { profilePage = it },
                        waitlistItems = lotteryItems.filter { it.lottery.id in waitlistLotteryIds },
                        onOpenCampsiteFromWaitlist = { camp ->
                            browseInitialCampsite = camp
                            profilePage = ProfilePage.MENU
                            tab = EntrantTab.BROWSE
                        },
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

// See [TBD 6] in LLM.md
/** The Home tab, "Your Lotteries" in the mockup. Only the empty message for now. */
@Composable
private fun YourLotteries(
    items: List<EntrantLotteryItem>,
    loading: Boolean,
    onOpenCampsite: (Campsite) -> Unit,
    onOpenReservations: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        ScreenTitle("Your Lotteries")
        Spacer(modifier = Modifier.height(10.dp))

        when {
            loading && items.isEmpty() -> Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BirchBarkColors.Gold)
            }

            items.isEmpty() -> EmptyState(
                title = "No current lotteries",
                hint = "Try finding one to join",
                modifier = Modifier.weight(1f).fillMaxWidth()
            )

            else -> LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items, key = { it.lottery.id }) { item ->
                    val camp = item.campsite
                    PhotoCard(
                        height = 220.dp,
                        imageUrl = camp?.allImages()?.firstOrNull(),
                        campsiteId = camp?.id,
                        onClick = {
                            if (item.actionLabel == "View Reservation") onOpenReservations()
                            else if (camp != null) onOpenCampsite(camp)
                        },
                        topRow = {
                            if (camp != null) StatusChip(camp.ratingLabel())
                            StatusChip(item.statusLabel, item.statusKind)
                        },
                        bottomStart = {
                            Text(
                                text = item.statusMessage,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = BirchBarkColors.Gold
                            )
                            Text(
                                text = camp?.name ?: "Campsite Lottery",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = BirchBarkColors.Cream
                            )
                            if (camp != null) Text(camp.location, color = BirchBarkColors.Cream)
                        },
                        bottomEnd = {
                            val label = item.actionLabel
                            if (label != null) {
                                Button(
                                    onClick = {
                                        if (label == "View Reservation") onOpenReservations()
                                        else if (camp != null) onOpenCampsite(camp)
                                    },
                                    colors = birchBarkButtonColors()
                                ) {
                                    Text(label)
                                }
                            }
                        }
                    )
                }
            }
        }
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
    waitlistItems: List<EntrantLotteryItem>,
    onOpenCampsiteFromWaitlist: (Campsite) -> Unit,
    onProfileUpdated: (UserProfile) -> Unit,
    onProfileDeleted: () -> Unit
) {
    var editingReviewItem by remember(page) {
        mutableStateOf<ReviewRepository.UserReviewItem?>(null)
    }

    BackHandler(enabled = page != ProfilePage.MENU) {
        if (editingReviewItem != null) {
            editingReviewItem = null
        } else {
            onPage(ProfilePage.MENU)
        }
    }

    when (page) {
        ProfilePage.MENU -> ProfileMenu(profile, name, onPage)
        ProfilePage.EDIT -> EditProfileScreen(
            onBack = { onPage(ProfilePage.MENU) },
            onProfileUpdated = onProfileUpdated,
            onDeleted = onProfileDeleted
        )
        //US 01.04.01, 01.04.03, 01.06.01, 01.06.02: wins to answer, then active / past stays
        ProfilePage.RESERVATIONS -> EntrantReservationsRoute(
            profile = profile,
            onBack = { onPage(ProfilePage.MENU) }
        )
        ProfilePage.WAITLIST -> CurrentWaitlistsScreen(
            items = waitlistItems,
            onBack = { onPage(ProfilePage.MENU) },
            onCampsiteClick = onOpenCampsiteFromWaitlist
        )
        //US 01.10.01, the notifications on/off switch
        ProfilePage.SETTINGS -> Column(modifier = Modifier.fillMaxSize()) {
            TextButton(onClick = { onPage(ProfilePage.MENU) }) {
                Text("Back", color = BirchBarkColors.Gold)
            }
            NotificationSettingsScreen()
        }
        ProfilePage.REVIEWS -> {
            val selectedItem = editingReviewItem
            if (selectedItem != null) {
                val targetCampsite = selectedItem.campsite
                    ?: Campsite(
                        id = selectedItem.review.campsiteId,
                        name = selectedItem.campsiteName
                    )
                ReviewRoute(
                    campsite = targetCampsite,
                    entrantId = profile.fid,
                    entrantName = name,
                    onBack = { editingReviewItem = null }
                )
            } else {
                UserReviewsRoute(
                    entrantId = profile.fid,
                    onBack = { onPage(ProfilePage.MENU) },
                    onReviewClick = { item -> editingReviewItem = item }
                )
            }
        }

        ProfilePage.POLICY -> PolicyScreen(
            onBack = { onPage(ProfilePage.MENU) }
        )
    }
}

/**
 * Displays the screen listing all campsite lottery waitlists the entrant is currently joined to.
 *
 * Shows a back navigation button, screen header, and either an [EmptyState] placeholder if [items]
 * is empty or a scrollable list of [PhotoCard]s displaying each campsite's photo, status chip,
 * location, status message, and a button to view campsite details.
 *
 * @param items The list of [EntrantLotteryItem] entries representing the entrant's active waitlists.
 * @param onBack Callback invoked when the user taps the `"Back"` button.
 * @param onCampsiteClick Callback invoked with the selected [Campsite] when a card or its `"View"` button is clicked.
 */
@Composable
private fun CurrentWaitlistsScreen(
    items: List<EntrantLotteryItem>,
    onBack: () -> Unit,
    onCampsiteClick: (Campsite) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("Back", color = BirchBarkColors.Gold)
        }

        ScreenTitle("Current Waitlists")

        if (items.isEmpty()) {
            EmptyState(
                title = "No active waitlists",
                hint = "Join a campsite lottery waitlist from Browse to see it here",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(items, key = { it.lottery.id }) { item ->
                    val camp = item.campsite
                    PhotoCard(
                        imageUrl = camp?.allImages()?.firstOrNull(),
                        campsiteId = camp?.id,
                        onClick = { if (camp != null) onCampsiteClick(camp) },
                        topRow = { StatusChip(item.statusLabel, item.statusKind) },
                        bottomStart = {
                            Text(
                                text = camp?.name ?: "Campsite Waitlist",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = BirchBarkColors.Cream
                            )
                            if (camp != null) Text(camp.location, color = BirchBarkColors.Cream)
                            Text(item.statusMessage, color = BirchBarkColors.MutedText)
                        },
                        bottomEnd = {
                            if (camp != null) {
                                Button(
                                    onClick = { onCampsiteClick(camp) },
                                    colors = birchBarkButtonColors()
                                ) {
                                    Text("View")
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

/** The list of options on the profile page. */
@Composable
private fun ProfileMenu(profile: UserProfile, name: String, onPage: (ProfilePage) -> Unit) {
    val photo = remember(profile.profileImage) { profile.profileImage?.toBytes() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(name, size = 56.dp, imageBytes = photo)
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
        MenuRow("Ratings & Reviews") { onPage(ProfilePage.REVIEWS  ) }
        MenuRow("Settings") { onPage(ProfilePage.SETTINGS) }
        MenuRow("App Policy") { onPage(ProfilePage.POLICY) }
    }
}