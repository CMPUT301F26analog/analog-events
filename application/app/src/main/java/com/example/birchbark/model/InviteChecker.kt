package com.example.birchbark.model

import com.example.birchbark.util.Time

/**
 * US 02.04.08: decides which campsites an owner can invite into their lottery.
 * Kept away from the screen so it can be unit tested.
 *
 * Outstanding issues. None.
 */
object InviteChecker {
    /**
     * Checks if a campsite can be invited into a lottery. The lottery has to be
     * saved, not drawn yet, and still open for registration. The campsite has
     * to belong to a different owner (their own campsites are added on the
     * lottery form), and can't already be in this lottery.
     *
     * @param campsite the campsite to check.
     * @param lottery the lottery it would be invited to.
     * @return true if the owner can send an invite for it.
     */
    fun canInvite(campsite: Campsite, lottery: Lottery): Boolean {
        return lottery.id.isNotBlank() &&
                !lottery.drawn &&
                Time.now() < lottery.registrationEnd &&
                campsite.id.isNotBlank() &&
                campsite.ownerId.isNotBlank() &&
                campsite.ownerId != lottery.ownerId &&
                campsite.id !in lottery.campsiteIds
    }

    /**
     * Picks the campsites that can be invited, and only the ones whose name or
     * location has the search text in it.
     *
     * @param campsites every campsite in the app.
     * @param lottery the lottery to invite them to.
     * @param search what the owner typed, blank shows all of them.
     * @return the campsites the owner can invite, in the same order.
     */
    fun invitable(campsites: List<Campsite>, lottery: Lottery, search: String): List<Campsite> {
        return campsites.filter { site ->
            canInvite(site, lottery) && site.matchesSearch(search)
        }
    }
}