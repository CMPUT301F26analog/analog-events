package com.example.birchbark.util

// LLM assistance: see doc/LLM.md, Prompt #002 (Akshat)

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * All date/time conversions for the app. Firestore stores dates as Long
 * (epoch millis); screens and calculations use LocalDate. Convert only here.
 *
 * Functions that take a [ZoneId] default to the phone's time zone; pass a
 * fixed zone in tests so results are the same on every machine.
 */
object Time {

    private val displayFormat = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)

    /**
     * Gives the current time.
     *
     * @return the current time in epoch millis.
     */
    fun now(): Long {
        return System.currentTimeMillis()
    }

    /**
     * Converts a date to the moment it begins. Use for the start of a period.
     *
     * @param date the calendar date.
     * @param zone the time zone whose midnight to use.
     * @return midnight at the start of [date], in epoch millis.
     */
    fun startOfDay(date: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Long {
        return date.atStartOfDay(zone).toInstant().toEpochMilli()
    }

    /**
     * Converts a date to the moment it ends. Use for the end of a period, so
     * the whole end date counts (e.g. registration stays open all of Nov 10).
     *
     * @param date the last full day of the period.
     * @param zone the time zone whose midnight to use.
     * @return midnight at the start of the day after [date], in epoch millis.
     */
    fun endOfDay(date: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Long {
        return startOfDay(date.plusDays(1), zone)
    }

    /**
     * Converts a stored time back to a calendar date. Use for start times.
     *
     * @param millis a time in epoch millis, e.g. [com.example.birchbark.model.Lottery.registrationStart].
     * @param zone the time zone to read the date in.
     * @return the calendar date [millis] falls on.
     */
    fun toLocalDate(millis: Long, zone: ZoneId = ZoneId.systemDefault()): LocalDate {
        return Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
    }

    /**
     * Converts a stored end time back to the last day of its period. The
     * reverse of [endOfDay].
     *
     * @param endMillis an end time made by [endOfDay], in epoch millis.
     * @param zone the time zone to read the date in.
     * @return the last full day of the period.
     */
    fun lastDayOf(endMillis: Long, zone: ZoneId = ZoneId.systemDefault()): LocalDate {
        return toLocalDate(endMillis - 1, zone)
    }

    /**
     * Formats a date for showing on screens.
     *
     * @param date the date to format.
     * @return the date as text, e.g. "Nov 2, 2026".
     */
    fun format(date: LocalDate): String {
        return date.format(displayFormat)
    }
}