package com.example.birchbark.ui.owner

import com.example.birchbark.model.Lottery
import com.example.birchbark.model.LotteryStatus
import com.example.birchbark.model.LotteryStatusCalculator
import com.example.birchbark.util.Time

/**
 * US 02.04.01. Turns a campsite's lotteries into the line shown on the campsite
 * details screen, like "Lottery open until Nov 10, 2026". Kept out of the
 * screen so it can be unit tested without Compose.
 *
 * Outstanding issues. If a campsite somehow has more than one active lottery,
 * only the one that starts last is shown.
 */

/**
 * Picks the lottery that hasn't been drawn yet, if there is one.
 *
 * @param lotteries every lottery that includes the campsite.
 * @param now the current time in epoch millis.
 * @return the active lottery that starts last, or null if they're all drawn or there are none.
 */
fun currentLottery(lotteries: List<Lottery>, now: Long): Lottery? {
    val active = lotteries.filter { LotteryStatusCalculator.status(it, now) != LotteryStatus.DRAWN }
    return active.maxByOrNull { it.registrationStart }
}

/**
 * Gives the text for the Lottery row on the campsite details screen.
 *
 * @param lotteries every lottery that includes the campsite.
 * @param now the current time in epoch millis.
 * @return what to show, like "No lottery yet" or "Lottery open until Nov 10, 2026".
 */
fun lotteryStatusText(lotteries: List<Lottery>, now: Long): String {
    val lottery = currentLottery(lotteries, now)
    if (lottery == null) {
        return if (lotteries.isEmpty()) "No lottery yet" else "Winners drawn"
    }
    return when (LotteryStatusCalculator.status(lottery, now)) {
        LotteryStatus.SCHEDULED -> "Lottery opens " + Time.format(Time.toLocalDate(lottery.registrationStart))
        LotteryStatus.OPEN -> "Lottery open until " + Time.format(Time.lastDayOf(lottery.registrationEnd))
        LotteryStatus.CLOSED -> "Registration closed, waiting for the draw"
        LotteryStatus.DRAWN -> "Winners drawn"
    }
}