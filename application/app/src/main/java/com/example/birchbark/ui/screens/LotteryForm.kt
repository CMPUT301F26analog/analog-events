package com.example.birchbark.ui.screens

import com.example.birchbark.model.Lottery
import com.example.birchbark.util.Time
import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * US 02.04.06 Update lottery listing
 */

// See [TBD] in LLM.md, used for help organizing and starting up the user story

data class LotteryFormResult(val lottery: Lottery?, val errors: List<String>)

data class LotteryForm(
    val registrationStart: String = "",
    val registrationEnd: String = "",
    val bookingStart: String = "",
    val bookingEnd: String = "",
    val numWinners: String = "",
    val waitlistCapacity: String = ""
) {
    fun applyTo(lottery: Lottery): LotteryFormResult {
        val errors = mutableListOf<String>()

        val regStart = readDate(registrationStart, "Registration start", errors)
        val regEnd = readDate(registrationEnd, "Registration end", errors)
        val bookStart = readDate(bookingStart, "Booking start", errors)
        val bookEnd = readDate(bookingEnd, "Booking end", errors)

        val winners = numWinners.trim().toIntOrNull()
        if (winners == null) errors.add("Number of winners must be a whole number")

        val capacityText = waitlistCapacity.trim()
        val capacity = capacityText.toIntOrNull()
        if (capacityText.isNotEmpty() && capacity == null) {
            errors.add("Waitlist capacity must be a whole number, or blank for no limit")
        }

        if (errors.isNotEmpty() || regStart == null || regEnd == null ||
            bookStart == null || bookEnd == null || winners == null
        ) {
            return LotteryFormResult(null, errors)
        }

        val updated = lottery.copy(
            registrationStart = Time.startOfDay(regStart),
            registrationEnd = Time.endOfDay(regEnd),
            bookingStart = Time.startOfDay(bookStart),
            bookingEnd = Time.endOfDay(bookEnd),
            numWinners = winners,
            waitlistCapacity = capacity
        )
        return LotteryFormResult(updated, emptyList())
    }

    private fun readDate(text: String, label: String, errors: MutableList<String>): LocalDate? {
        return try {
            LocalDate.parse(text.trim())
        } catch (e: DateTimeParseException) {
            errors.add("$label must look like 2026-11-15")
            null
        }
    }
}

/** Extension function to convert a saved Lottery into a LotteryForm */
fun Lottery.toForm(): LotteryForm = LotteryForm(
    registrationStart = Time.toLocalDate(this.registrationStart).toString(),
    registrationEnd = Time.lastDayOf(this.registrationEnd).toString(),
    bookingStart = Time.toLocalDate(this.bookingStart).toString(),
    bookingEnd = Time.lastDayOf(this.bookingEnd).toString(),
    numWinners = this.numWinners.toString(),
    waitlistCapacity = this.waitlistCapacity?.toString() ?: ""
)