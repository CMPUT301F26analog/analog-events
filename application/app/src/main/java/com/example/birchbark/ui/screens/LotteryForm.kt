package com.example.birchbark.ui.screens

import com.example.birchbark.model.Lottery
import com.example.birchbark.util.Time
import java.time.LocalDate
import java.time.format.DateTimeParseException

// See [3] in LLM.md, used for help organizing and starting up the user story

/**
 * US 02.04.06 Update lottery listing
 *
 * The form only READS what the owner typed (dates and whole numbers). The rules about those values
 * (registration ends after it starts, booking after registration, at least one winner, waiting list limit)
 * are Akshat's LotteryValidator, which LotteryRepository.updateLottery runs before saving. Keeping them in one
 * place means the form and the repository can never disagree.
 */

/**
 * The result of reading the form.
 *
 * @property lottery the updated lottery if every field could be read, otherwise null.
 * @property errors messages for every field that couldn't be read.
 */
data class LotteryFormResult(val lottery: Lottery?, val errors: List<String>)

/**
 * This class just holds the raw text values from the input fields on the screen.
 * Everything starts as a String because that is what users type into the text boxes.
 */
data class LotteryForm(
    val registrationStart: String = "",
    val registrationEnd: String = "",
    val bookingStart: String = "",
    val bookingEnd: String = "",
    val numWinners: String = "",
    val waitlistCapacity: String = ""   // blank = no limit
) {
    /**
     * Turns the typed text into dates and numbers and copies them onto [lottery]
     * (its id, owner, campsites and drawn flag stay as they were).
     *
     * @param lottery The original lottery data that we want to update.
     * @return A LotteryFormResult containing either the updated data or a list of error messages.
     */
    fun applyTo(lottery: Lottery): LotteryFormResult {
        val errors = mutableListOf<String>()

        // 1. DATE PARSING & VALIDATION - Get text inputs into dates
        val regStart = readDate(registrationStart, "Registration start", errors)
        val regEnd = readDate(registrationEnd, "Registration end", errors)
        val bookStart = readDate(bookingStart, "Booking start", errors)
        val bookEnd = readDate(bookingEnd, "Booking end", errors)

        // 2. CAPACITY & NUMERIC VALIDATION - Make sure winners & capacity is a whole number
        val winners = numWinners.trim().toIntOrNull()
        if (winners == null) {
            errors.add("Number of winners must be a whole number")
        }
        val capacityText = waitlistCapacity.trim()
        val capacity = capacityText.toIntOrNull()
        if (capacityText.isNotEmpty() && capacity == null) {
            errors.add("Waitlist capacity must be a whole number, or blank for no limit")
        }

        // 3. PAYLOAD RETURN DEFINITION - Make sure there's no basic syntax formatting errors
        if (errors.isNotEmpty() || regStart == null || regEnd == null ||
            bookStart == null || bookEnd == null || winners == null
        ) {
            return LotteryFormResult(null, errors)
        }

        // Everything is parsed, so we can return it correctly
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

    // Helper function to correctly parse the date
    private fun readDate(text: String, label: String, errors: MutableList<String>): LocalDate? {
        return try {
            LocalDate.parse(text.trim())
        } catch (e: DateTimeParseException) {
            errors.add("$label must look like 2026-11-15")
            null
        }
    }
}

/** Extension helper to initialize inputs when editing existing lotteries */
fun Lottery.toForm(): LotteryForm = LotteryForm(
    registrationStart = Time.toLocalDate(this.registrationStart).toString(),
    registrationEnd = Time.lastDayOf(this.registrationEnd).toString(),
    bookingStart = Time.toLocalDate(this.bookingStart).toString(),
    bookingEnd = Time.lastDayOf(this.bookingEnd).toString(),
    numWinners = this.numWinners.toString(),
    waitlistCapacity = this.waitlistCapacity?.toString() ?: ""
)