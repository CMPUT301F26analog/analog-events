package com.example.birchbark.model

// See #048 in LLM.md

/**
 * US 01.03.01, 01.03.02: builds the win and lose notifications for a lottery
 * draw. Pure logic with no Firestore, so it can be unit tested; the draw in
 * ReservationRepository saves the result.
 */
object DrawNotifications {

    /**
     * Builds a draw notification's document ID. One per lottery and entrant,
     * so running the draw twice overwrites instead of sending twice.
     *
     * @param lotteryId the lottery's document ID.
     * @param entrantId the entrant's FID.
     * @return "<lotteryId>_<entrantId>_draw".
     */
    fun buildId(lotteryId: String, entrantId: String): String {
        return "${lotteryId}_${entrantId}_draw"
    }

    /**
     * Builds one notification for every entrant on the waitlist: winners get
     * [NotificationType.LOTTERY_WON], everyone else [NotificationType.LOTTERY_LOST].
     * Entrants who opted out (US 01.10.01) get nothing.
     *
     * @param lottery the lottery that was drawn. Its owner is the sender.
     * @param entrantIds FIDs of everyone on the waitlist.
     * @param winnerIds FIDs of the winners, from [LotteryDraw.drawWinners].
     * @param optedOut FIDs of entrants who turned notifications off.
     * @param now when the draw happened, in epoch ms.
     * @return the notifications to save, in the same order as [entrantIds].
     */
    fun build(
        lottery: Lottery,
        entrantIds: List<String>,
        winnerIds: List<String>,
        optedOut: Set<String>,
        now: Long
    ): List<AppNotification> {
        val winners = winnerIds.toSet()
        return entrantIds
            .filter { it !in optedOut }
            .map { entrantId ->
                val won = entrantId in winners
                AppNotification(
                    id = buildId(lottery.id, entrantId),
                    recipientId = entrantId,
                    senderId = lottery.ownerId,
                    type = if (won) NotificationType.LOTTERY_WON else NotificationType.LOTTERY_LOST,
                    title = if (won) "You won the lottery!" else "Lottery results are in",
                    body = if (won) {
                        "You were picked in the campsite lottery. Open My Reservations to choose your dates."
                    } else {
                        "You weren't picked this time. You can join other campsite lotteries any time."
                    },
                    lotteryId = lottery.id,
                    createdAt = now,
                )
            }
    }
}
