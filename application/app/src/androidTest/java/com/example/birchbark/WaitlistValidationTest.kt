
package com.example.birchbark

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.birchbark.model.Lottery
import com.example.birchbark.model.RegistrationChecker
import com.example.birchbark.model.WaitlistChecker
import com.example.birchbark.model.WaitlistRepository
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WaitlistValidationTest {

    private val repository = WaitlistRepository()

    private fun assertInvalidJoin(lotteryId: String, userId: String) {
        var failure: Exception? = null

        repository.joinWaitlist(
            lotteryId = lotteryId,
            userId = userId,
            onResult = { fail("Invalid IDs must not allow joining") },
            onFailure = { failure = it }
        )

        assertTrue(failure is IllegalArgumentException)
    }

    private fun assertInvalidLeave(lotteryId: String, userId: String) {
        var failure: Exception? = null

        repository.leaveWaitlist(
            lotteryId = lotteryId,
            userId = userId,
            onResult = { fail("Invalid IDs must not allow leaving") },
            onFailure = { failure = it }
        )

        assertTrue(failure is IllegalArgumentException)
    }

    @Test
    fun joinRejectsMissingLotteryId() {
        assertInvalidJoin("", "test-user")
    }

    @Test
    fun joinRejectsMissingUserId() {
        assertInvalidJoin("test-lottery", "")
    }

    @Test
    fun leaveRejectsMissingLotteryId() {
        assertInvalidLeave("", "test-user")
    }

    @Test
    fun leaveRejectsMissingUserId() {
        assertInvalidLeave("test-lottery", "")
    }

    @Test
    fun registrationOpenDuringValidPeriod() {
        val lottery = Lottery(
            registrationStart = 1000L,
            registrationEnd = 2000L
        )

        assertTrue(RegistrationChecker.isOpen(lottery, 1500L))
    }

    @Test
    fun registrationClosedBeforeStart() {
        val lottery = Lottery(
            registrationStart = 1000L,
            registrationEnd = 2000L
        )

        assertFalse(RegistrationChecker.isOpen(lottery, 999L))
    }

    @Test
    fun registrationClosedAtEnd() {
        val lottery = Lottery(
            registrationStart = 1000L,
            registrationEnd = 2000L
        )

        assertFalse(RegistrationChecker.isOpen(lottery, 2000L))
    }

    @Test
    fun drawnLotteryRejectsRegistration() {
        val lottery = Lottery(
            registrationStart = 1000L,
            registrationEnd = 2000L,
            drawn = true
        )

        assertFalse(RegistrationChecker.isOpen(lottery, 1500L))
    }

    @Test
    fun fullWaitlistRejectsNewEntrants() {
        val lottery = Lottery(waitlistCapacity = 1)

        assertTrue(WaitlistChecker.isFull(lottery, 1))
        assertFalse(WaitlistChecker.isFull(lottery, 0))
    }

    @Test
    fun unlimitedWaitlistNeverFull() {
        val lottery = Lottery(waitlistCapacity = null)

        assertFalse(WaitlistChecker.isFull(lottery, 1000))
    }
}
