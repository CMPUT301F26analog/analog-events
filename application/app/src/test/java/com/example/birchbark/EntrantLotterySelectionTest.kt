
package com.example.birchbark

import com.example.birchbark.model.Lottery
import com.example.birchbark.ui.entrant.selectOpenLottery
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EntrantLotterySelectionTest {

    private val now = 10_000L

    private fun lottery(
        id: String,
        start: Long,
        end: Long,
        drawn: Boolean = false
    ) = Lottery(
        id = id,
        campsiteIds = listOf("c1"),
        registrationStart = start,
        registrationEnd = end,
        drawn = drawn
    )

    @Test
    fun noLotteries_returnsNull() {
        assertNull(selectOpenLottery(emptyList(), now))
    }

    @Test
    fun openLottery_isSelected() {
        val open = lottery("open", 9_000L, 11_000L)

        assertEquals(
            "open",
            selectOpenLottery(listOf(open), now)?.id
        )
    }

    @Test
    fun scheduledLottery_isNotSelected() {
        val scheduled = lottery("future", 12_000L, 15_000L)

        assertNull(selectOpenLottery(listOf(scheduled), now))
    }

    @Test
    fun closedLottery_isNotSelected() {
        val closed = lottery("closed", 5_000L, 9_000L)

        assertNull(selectOpenLottery(listOf(closed), now))
    }

    @Test
    fun drawnLottery_isNotSelected() {
        val drawn = lottery(
            "drawn",
            9_000L,
            11_000L,
            drawn = true
        )

        assertNull(selectOpenLottery(listOf(drawn), now))
    }

    @Test
    fun blankLotteryId_isNotSelected() {
        val invalid = lottery("", 9_000L, 11_000L)

        assertNull(selectOpenLottery(listOf(invalid), now))
    }

    @Test
    fun openLottery_preferredOverNewerScheduledLottery() {
        val open = lottery("open", 8_000L, 11_000L)
        val future = lottery("future", 12_000L, 15_000L)

        assertEquals(
            "open",
            selectOpenLottery(listOf(open, future), now)?.id
        )
    }

    @Test
    fun multipleOpenLotteries_selectsMostRecentStart() {
        val older = lottery("older", 5_000L, 11_000L)
        val newer = lottery("newer", 9_000L, 12_000L)

        assertEquals(
            "newer",
            selectOpenLottery(listOf(older, newer), now)?.id
        )
    }

    @Test
    fun registrationEnd_isExclusive() {
        val ending = lottery("ending", 5_000L, 10_000L)

        assertNull(selectOpenLottery(listOf(ending), now))
    }
}
