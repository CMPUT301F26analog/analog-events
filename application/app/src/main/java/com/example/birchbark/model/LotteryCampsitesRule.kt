package com.example.birchbark.model

// See #TBD1 in LLM.md

/**
 * US 02.04.02: a lottery has to have at least one campsite in it. A lottery
 * can have more than one campsite (a multi campsite lottery), but never none.
 *
 * Works the same way as Akshat's other lottery rules, [LotteryValidator]
 * runs it before saving.
 *
 * Outstanding issues. None.
 */
object LotteryCampsitesRule : LotteryRule {
    /** Shown to the owner when this rule fails. */
    override val message = "Pick at least one campsite for the lottery"

    /**
     * Checks the rule above.
     *
     * @param lottery the lottery to check.
     * @return true if [Lottery.campsiteIds] has at least one campsite, false otherwise.
     */
    override fun isValid(lottery: Lottery): Boolean {
        return lottery.campsiteIds.isNotEmpty()
    }
}