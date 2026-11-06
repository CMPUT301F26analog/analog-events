
package com.example.birchbark

import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.birchbark.model.Lottery
import com.example.birchbark.ui.entrant.EntrantWaitlistSection
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EntrantWaitlistSectionTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val now = 10_000L

    private fun lottery(
        id: String = "lottery1",
        start: Long = 9_000L,
        end: Long = 11_000L,
        drawn: Boolean = false
    ) = Lottery(
        id = id,
        registrationStart = start,
        registrationEnd = end,
        drawn = drawn
    )

    private fun showSection(lottery: Lottery?) {
        composeRule.setContent {
            EntrantWaitlistSection(
                lottery = lottery,
                now = now,
                buttonContent = { id ->
                    Text(
                        text = "Join lottery $id",
                        modifier = Modifier.testTag("joinAction")
                    )
                }
            )
        }
    }

    @Test
    fun openLottery_showsJoinAction() {
        showSection(lottery())

        composeRule.onNodeWithTag("joinAction")
            .assertIsDisplayed()
    }

    @Test
    fun noLottery_hidesJoinAction() {
        showSection(null)

        composeRule.onNodeWithTag("joinAction")
            .assertDoesNotExist()
    }

    @Test
    fun scheduledLottery_hidesJoinAction() {
        showSection(lottery(start = 12_000L, end = 15_000L))

        composeRule.onNodeWithTag("joinAction")
            .assertDoesNotExist()
    }

    @Test
    fun closedLottery_hidesJoinAction() {
        showSection(lottery(start = 5_000L, end = 9_000L))

        composeRule.onNodeWithTag("joinAction")
            .assertDoesNotExist()
    }

    @Test
    fun drawnLottery_hidesJoinAction() {
        showSection(lottery(drawn = true))

        composeRule.onNodeWithTag("joinAction")
            .assertDoesNotExist()
    }

    @Test
    fun blankLotteryId_hidesJoinAction() {
        showSection(lottery(id = ""))

        composeRule.onNodeWithTag("joinAction")
            .assertDoesNotExist()
    }
}
