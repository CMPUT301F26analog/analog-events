package com.example.birchbark.ui.screens

// See [4] in LLM.md, used to help write Test for EditLotteryScreen.kt

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import com.example.birchbark.model.Lottery
import com.example.birchbark.ui.theme.BirchBarkTheme
import com.example.birchbark.util.Time
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class EditLotteryScreenTest {

    @get:Rule val rule = createComposeRule()

    private fun lottery(drawn: Boolean = false) = Lottery(
        id = "L1", campsiteIds = listOf("1"), ownerId = "owner",
        registrationStart = Time.startOfDay(LocalDate.of(2026, 11, 2)),
        registrationEnd = Time.endOfDay(LocalDate.of(2026, 11, 9)),
        bookingStart = Time.startOfDay(LocalDate.of(2026, 11, 15)),
        bookingEnd = Time.endOfDay(LocalDate.of(2026, 11, 20)),
        numWinners = 3, waitlistCapacity = 10, drawn = drawn
    )

    private fun show(
        lottery: Lottery?,
        loading: Boolean = false,
        errors: List<String> = emptyList(),
        success: String? = null,
        onSave: (Lottery) -> Unit = {}
    ) = rule.setContent {
        BirchBarkTheme {
            EditLotteryScreen("Jasper National Park", lottery, loading, errors, success, onSave, onBack = {})
        }
    }

    // ---------- the four states ----------

    @Test fun loading_showsLoadingText() {
        show(null, loading = true)
        rule.onNodeWithText("Loading...").assertExists()
    }

    @Test fun noLottery_saysSo() {
        show(null)
        rule.onNodeWithText("This campsite doesn't have a lottery yet.").assertExists()
    }

    @Test fun editableLottery_showsItsValuesAndAnEnabledSaveButton() {
        show(lottery())
        rule.onNodeWithText("Lottery for Jasper National Park").assertExists()
        rule.onNodeWithText("2026-11-02").assertExists()
        rule.onNodeWithText("2026-11-09").assertExists()
        rule.onNodeWithText("2026-11-15").assertExists()
        rule.onNodeWithText("2026-11-20").assertExists()
        rule.onNodeWithText("Save changes").performScrollTo().assertIsEnabled()
    }

    @Test fun drawnLottery_isLockedWithAnExplanation() {
        show(lottery(drawn = true))
        rule.onNodeWithText("This lottery has already been drawn, so it can't be edited.").assertExists()
        rule.onNodeWithText("Save changes").performScrollTo().assertIsNotEnabled()
    }

    // ---------- saving ----------

    @Test fun savingWithoutChanges_sendsTheSameLottery() {
        var saved: Lottery? = null
        show(lottery(), onSave = { saved = it })
        rule.onNodeWithText("Save changes").performScrollTo().performClick()
        assertEquals(lottery(), saved)
    }

    @Test fun changingTheWinners_sendsTheNewNumber() {
        var saved: Lottery? = null
        show(lottery(), onSave = { saved = it })
        rule.onNodeWithText("Number of winners").performScrollTo().performTextReplacement("5")
        rule.onNodeWithText("Save changes").performScrollTo().performClick()
        assertEquals(5, saved?.numWinners)
    }

    @Test fun clearingTheWaitingListLimit_sendsNoLimit() {
        var saved: Lottery? = null
        show(lottery(), onSave = { saved = it })
        rule.onNodeWithText("Waiting list limit (blank = no limit)").performScrollTo().performTextReplacement("")
        rule.onNodeWithText("Save changes").performScrollTo().performClick()
        assertNotNull(saved)
        assertNull(saved?.waitlistCapacity)
    }

    @Test fun aBadDate_showsAMessageAndSavesNothing() {
        var saved: Lottery? = null
        show(lottery(), onSave = { saved = it })
        rule.onNodeWithText("Registration start").performScrollTo().performTextReplacement("Nov 2")
        rule.onNodeWithText("Save changes").performScrollTo().performClick()
        rule.onNodeWithText("Registration start must look like 2026-11-15").assertExists()
        assertNull(saved)
    }

    @Test fun aBadNumber_showsAMessageAndSavesNothing() {
        var saved: Lottery? = null
        show(lottery(), onSave = { saved = it })
        rule.onNodeWithText("Number of winners").performScrollTo().performTextReplacement("many")
        rule.onNodeWithText("Save changes").performScrollTo().performClick()
        rule.onNodeWithText("Number of winners must be a whole number").assertExists()
        assertNull(saved)
    }

    // ---------- messages from the route ----------

    @Test fun ruleMessagesFromTheRepository_areShown() {
        show(lottery(), errors = listOf("Number of winners must be at least 1"))
        rule.onNodeWithText("Number of winners must be at least 1").assertExists()
    }

    @Test fun successMessage_isShown() {
        show(lottery(), success = "Saved. Entrants will see the changes.")
        rule.onNodeWithText("Saved. Entrants will see the changes.").assertExists()
    }
}
