package com.example.birchbark

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.birchbark.model.Campsite
import com.example.birchbark.model.Lottery
import com.example.birchbark.model.LotteryInvite
import com.example.birchbark.ui.owner.InviteCampsiteContent
import com.example.birchbark.ui.owner.LotteryInvitesContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Intent tests for lotteries shared between owners.
 * Covers US 02.04.08 (multi-owner lottery).
 *
 * Uses the drawing parts of the invite screens with fake data, so these don't need Firestore.
 */
@RunWith(AndroidJUnit4::class)
class MultiOwnerLotteryTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val lottery = Lottery(id = "L1", ownerId = "ownerA", campsiteIds = listOf("a1"))
    private val whistlers = Campsite(id = "a1", ownerId = "ownerA", name = "Whistlers Campground", location = "Jasper")
    private val twoJack = Campsite(id = "b1", ownerId = "ownerB", name = "Two Jack Lakeside", location = "Banff")
    private val tunnel = Campsite(id = "c1", ownerId = "ownerC", name = "Tunnel Mountain", location = "Banff")

    private val invite = LotteryInvite(id = "i1", lotteryId = "L1", fromOwnerId = "ownerA", toOwnerId = "ownerB", campsiteId = "b1", campsiteName = "Two Jack Lakeside")

    /**
     * Shows the invite screen for owner A's lottery.
     *
     * @param sentIds campsites already invited.
     * @param onInvite gets the campsite when Invite is tapped.
     */
    private fun showInviteScreen(sentIds: Set<String> = setOf(), onInvite: (Campsite) -> Unit = {}) {
        composeRule.setContent {
            InviteCampsiteContent(
                campsites = listOf(whistlers, twoJack, tunnel),
                lottery = lottery,
                sentIds = sentIds,
                message = null,
                onInvite = onInvite,
                onBack = {}
            )
        }
    }

    @Test
    fun inviteScreen_showsOnlyOtherOwnersCampsites() {
        showInviteScreen()
        composeRule.onNodeWithText("Two Jack Lakeside").assertExists()
        composeRule.onNodeWithText("Tunnel Mountain").assertExists()
        composeRule.onNodeWithText("Whistlers Campground").assertDoesNotExist()
    }

    @Test
    fun inviteScreen_searchFiltersList() {
        showInviteScreen()
        composeRule.onNodeWithTag("inviteSearch").performTextInput("tunnel")
        composeRule.onNodeWithText("Tunnel Mountain").assertExists()
        composeRule.onNodeWithText("Two Jack Lakeside").assertDoesNotExist()
    }

    @Test
    fun inviteScreen_tappingInviteSendsThatCampsite() {
        var invited: Campsite? = null
        showInviteScreen(onInvite = { invited = it })
        composeRule.onNodeWithTag("inviteButton_b1").performClick()
        assertEquals("b1", invited?.id)
    }

    @Test
    fun inviteScreen_sentInviteCantBeSentAgain() {
        showInviteScreen(sentIds = setOf("b1"))
        composeRule.onNodeWithTag("inviteButton_b1").assertIsNotEnabled()
        composeRule.onNodeWithText("Invite sent").assertExists()
    }

    @Test
    fun invitesScreen_showsInviteWithButtons() {
        composeRule.setContent {
            LotteryInvitesContent(invites = listOf(invite), message = null, onAccept = {}, onDecline = {}, onBack = {})
        }
        composeRule.onNodeWithText("Two Jack Lakeside was invited to a lottery").assertExists()
        composeRule.onNodeWithTag("acceptInvite_i1").assertExists()
        composeRule.onNodeWithTag("declineInvite_i1").assertExists()
    }

    @Test
    fun invitesScreen_acceptAndDeclineCallBack() {
        var accepted: LotteryInvite? = null
        var declined: LotteryInvite? = null
        composeRule.setContent {
            LotteryInvitesContent(
                invites = listOf(invite),
                message = null,
                onAccept = { accepted = it },
                onDecline = { declined = it },
                onBack = {}
            )
        }
        composeRule.onNodeWithTag("acceptInvite_i1").performClick()
        composeRule.onNodeWithTag("declineInvite_i1").performClick()
        assertEquals("i1", accepted?.id)
        assertEquals("i1", declined?.id)
    }

    @Test
    fun invitesScreen_emptyShowsMessage() {
        composeRule.setContent {
            LotteryInvitesContent(invites = listOf(), message = null, onAccept = {}, onDecline = {}, onBack = {})
        }
        composeRule.onNodeWithTag("noInvites").assertExists()
    }
}