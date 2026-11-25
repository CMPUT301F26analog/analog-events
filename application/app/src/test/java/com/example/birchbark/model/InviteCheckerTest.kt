package com.example.birchbark.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [InviteChecker]. Covers US 02.04.08 (multi-owner lottery),
 * which campsites an owner can invite into their lottery.
 */
class InviteCheckerTest {

    //lottery made by owner A for one of their campsites
    private val lottery = Lottery(id = "L1", ownerId = "ownerA", campsiteIds = listOf("a1"))

    private val ownCampsite = Campsite(id = "a2", ownerId = "ownerA", name = "Whistlers Campground", location = "Jasper")
    private val otherCampsite = Campsite(id = "b1", ownerId = "ownerB", name = "Two Jack Lakeside", location = "Banff")
    private val alreadyIn = Campsite(id = "a1", ownerId = "ownerB", name = "Wapiti Campground", location = "Jasper")
    private val noOwner = Campsite(id = "c1", ownerId = "", name = "Tunnel Mountain", location = "Banff")

    @Test
    fun otherOwnersCampsite_canBeInvited() {
        assertTrue(InviteChecker.canInvite(otherCampsite, lottery))
    }

    @Test
    fun ownCampsite_cantBeInvited() {
        assertFalse(InviteChecker.canInvite(ownCampsite, lottery))
    }

    @Test
    fun campsiteAlreadyInLottery_cantBeInvited() {
        assertFalse(InviteChecker.canInvite(alreadyIn, lottery))
    }

    @Test
    fun campsiteWithNoOwner_cantBeInvited() {
        assertFalse(InviteChecker.canInvite(noOwner, lottery))
    }

    @Test
    fun invitable_blankSearch_showsOnlyInvitable() {
        val all = listOf(ownCampsite, otherCampsite, alreadyIn, noOwner)
        assertEquals(listOf(otherCampsite), InviteChecker.invitable(all, lottery, ""))
    }

    @Test
    fun invitable_searchMatchesNameOrLocation() {
        val tunnel = Campsite(id = "b2", ownerId = "ownerB", name = "Tunnel Mountain", location = "Banff")
        val all = listOf(otherCampsite, tunnel)
        assertEquals(listOf(tunnel), InviteChecker.invitable(all, lottery, "tunnel"))
        assertEquals(listOf(otherCampsite, tunnel), InviteChecker.invitable(all, lottery, "banff"))
    }
}