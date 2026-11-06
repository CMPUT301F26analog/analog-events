
package com.example.birchbark

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.birchbark.model.Campsite
import com.example.birchbark.ui.owner.CampsiteDetailsContent
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OwnerWaitlistNavigationTest {

    @get:Rule
    val rule = createComposeRule()

    private val campsite = Campsite(
        id = "test-campsite",
        name = "Test Campground",
        location = "Jasper",
        maxOccupancy = 4
    )

    @Test
    fun waitlistButtonAppearsWhenLotteryExists() {
        rule.setContent {
            CampsiteDetailsContent(
                campsite = campsite,
                onEditClick = {},
                onBack = {},
                canViewWaitlist = true
            )
        }

        rule.onNodeWithTag("viewWaitlistButton")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun waitlistButtonHiddenWhenNoLottery() {
        rule.setContent {
            CampsiteDetailsContent(
                campsite = campsite,
                onEditClick = {},
                onBack = {},
                canViewWaitlist = false
            )
        }

        rule.onNodeWithTag("viewWaitlistButton")
            .assertDoesNotExist()
    }

    @Test
    fun clickingWaitlistButtonTriggersCallback() {
        var clicked = false

        rule.setContent {
            CampsiteDetailsContent(
                campsite = campsite,
                onEditClick = {},
                onBack = {},
                canViewWaitlist = true,
                onViewWaitlist = {
                    clicked = true
                }
            )
        }

        rule.onNodeWithTag("viewWaitlistButton")
            .performScrollTo()
            .performClick()

        rule.runOnIdle {
            assertTrue(clicked)
        }
    }
}
