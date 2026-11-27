package com.example.birchbark.ui.entrant

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.birchbark.model.Campsite
import com.example.birchbark.model.CampsiteFilter
import com.example.birchbark.model.EntrantFilters
import com.example.birchbark.model.Lottery
import com.example.birchbark.model.LotteryRepository
import com.example.birchbark.model.LotteryStatus
import com.example.birchbark.model.LotteryStatusCalculator
import com.example.birchbark.model.RegistrationChecker
import com.example.birchbark.util.Time
import com.example.birchbark.waitlist.JoinWaitlistButton
import com.example.birchbark.model.Review
import com.example.birchbark.model.ReviewRepository

fun selectOpenLottery(
    lotteries: List<Lottery>,
    now: Long
): Lottery? {
    return lotteries
        .filter {
            it.id.isNotBlank() &&
                    RegistrationChecker.isOpen(it, now)
        }
        .maxByOrNull { it.registrationStart }
}

/**
 * The Browse tab: search results, the filter screen, and campsite details.
 * System back closes the filter screen or the details first.
 */
@Composable
fun EntrantCampsitesApp(
    //entrantId: String = "",     // Pass the signed-in user's ID/FID from MainActivity/Auth
    //entrantName: String = ""    // Pass the signed-in user's name from MainActivity/Auth
    entrantId: String = "",
    entrantName: String = "Entrant",
    initialCampsite: Campsite? = null,
    onInitialCampsiteConsumed: () -> Unit = {}
) {
    var selectedCampsite by remember(initialCampsite) { mutableStateOf(initialCampsite) }
    var filters by remember { mutableStateOf(EntrantFilters()) }
    var showFilters by remember { mutableStateOf(false) }
    var lotteries by remember { mutableStateOf<List<Lottery>>(emptyList()) }

    // Load lotteries once so CampsiteFilter can match booking dates
    LaunchedEffect(Unit) {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("lotteries")
            .get()
            .addOnSuccessListener { snapshot ->
                lotteries = snapshot.toObjects(Lottery::class.java)
            }
    }

    val campsite = selectedCampsite

    when {
        showFilters -> {
            BackHandler { showFilters = false }
            FilterScreen(
                currentFilters = filters,
                onApply = {
                    filters = it
                    showFilters = false
                },
                onCancel = { showFilters = false }
            )
        }

        campsite == null -> {
            SearchCampsitesScreen(
                onCampsiteClick = { selectedCampsite = it },
                filter = { CampsiteFilter.matches(it, filters, lotteries) },
                onFilterClick = { showFilters = true }
            )
        }

        else -> {
            BackHandler {
                selectedCampsite = null
                onInitialCampsiteConsumed()
            }
            EntrantCampsiteDetailsRoute(
                campsite = campsite,
                entrantId = entrantId,
                entrantName = entrantName,
                onBack = {
                    selectedCampsite = null
                    onInitialCampsiteConsumed()
                }
            )
        }
    }
}

/**
 * US 01.01.05, US 01.12.01: shared entrant details route for Browse and QR results.
 * Loads the campsite's lottery and presents the existing waitlist-aware details.
 * @param campsite the retrieved campsite.
 * @param onBack returns to the previous entrant screen.
 */
@Composable
internal fun EntrantCampsiteDetailsRoute(
    campsite: Campsite,
    entrantId: String,
    entrantName: String,
    onBack: () -> Unit
) {
    val repository = remember { LotteryRepository() }

    var lotteries by remember(campsite.id) {
        mutableStateOf<List<Lottery>?>(null)
    }

    var error by remember(campsite.id) {
        mutableStateOf<String?>(null)
    }

    // Track updated campsite (so rating averages refresh immediately after posting a review)
    var currentCampsite by remember(campsite) { mutableStateOf(campsite) }

    // Track which sub-screen is active: "details", "all_reviews", or "write_review"
    var activeSubScreen by remember(campsite.id) { mutableStateOf("details") }

    var writtenReviewCount by remember(campsite.id) { mutableStateOf(0) }

    LaunchedEffect(currentCampsite) {
        ReviewRepository().getAllReviews(currentCampsite.id, object : ReviewRepository.LoadAllCallback {
            override fun onLoaded(reviews: List<Review>) {
                writtenReviewCount = reviews.count { it.comment.isNotBlank() }
            }
            override fun onError(error: Exception) {}
        })
    }

    // Handle Android system back button cleanly across all sub-screens
    BackHandler {
        when (activeSubScreen) {
            "write_review" -> activeSubScreen = "all_reviews"
            "all_reviews" -> activeSubScreen = "details"
            else -> onBack()
        }
    }

    LaunchedEffect(campsite.id) {
        repository.getLotteriesForCampsite(
            campsite.id,
            object : LotteryRepository.ListCallback {
                override fun onLoaded(found: List<Lottery>) {
                    lotteries = found
                }

                override fun onError(exception: Exception) {
                    error = exception.message
                        ?: "Unable to load lottery"
                }
            }
        )
    }

    when {
        error != null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                TextButton(onClick = onBack) {
                    Text("Back")
                }

                Text("Error: $error")
            }
        }

        lotteries == null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        else -> {
            val list = lotteries.orEmpty()
            //the open lottery if there is one, otherwise the latest one so we can say why they can't join
            val lottery = selectOpenLottery(list, System.currentTimeMillis())
                ?: list.maxByOrNull { it.registrationStart }

            when (activeSubScreen) {
                "details" -> {
                    CampsiteDetailsScreen(
                        campsite = currentCampsite,
                        lottery = lottery,
                        joined = false,
                        isFull = false,
                        message = null,
                        onBack = onBack,
                        onJoin = {},
                        onLeave = {},
                        onReviewsClick = { activeSubScreen = "all_reviews" },
                        writtenReviewCount = writtenReviewCount
                    )
                }

                "all_reviews" -> {
                    ReviewsRoute(
                        campsite = currentCampsite,
                        onBack = { activeSubScreen = "details" },
                        onWriteReview = { activeSubScreen = "write_review" }
                    )
                }

                "write_review" -> {
                    ReviewRoute(
                        campsite = currentCampsite,
                        entrantId = entrantId,
                        entrantName = entrantName,
                        onBack = { activeSubScreen = "all_reviews" },
                        onCampsiteUpdated = { updatedCampsite ->
                            currentCampsite = updatedCampsite
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EntrantWaitlistSection(
    lottery: Lottery?,
    now: Long = System.currentTimeMillis(),
    buttonContent: @Composable (String) -> Unit = { id ->
        JoinWaitlistButton(lotteryId = id)
    }
) {
    val openLottery = lottery?.takeIf {
        it.id.isNotBlank() &&
                RegistrationChecker.isOpen(it, now)
    }

    if (openLottery != null) {
        Spacer(modifier = Modifier.height(16.dp))
        buttonContent(openLottery.id)
    } else {
        //US 01.01.06: say why there's no join button instead of showing nothing
        lotteryMessage(lottery, now)?.let { message ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(message, modifier = Modifier.testTag("lotteryMessage"))
        }
    }
}

fun lotteryMessage(lottery: Lottery?, now: Long): String? {
    if (lottery == null || lottery.id.isBlank()) return "No lottery for this campsite right now."
    return when (LotteryStatusCalculator.status(lottery, now)) {
        LotteryStatus.SCHEDULED ->
            "Registration opens ${Time.format(Time.toLocalDate(lottery.registrationStart))}."
        LotteryStatus.OPEN -> null
        LotteryStatus.CLOSED -> "Registration has closed. Winners will be drawn soon."
        LotteryStatus.DRAWN -> "Winners have been drawn for this lottery."
    }
}
