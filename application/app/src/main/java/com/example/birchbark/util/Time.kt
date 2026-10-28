package com.example.birchbark.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * All date/time conversions for the app. Firestore stores dates as Long
 * (epoch millis); screens and calculations use LocalDate. Convert only here.
 */
object Time {

    private val displayFormat = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)

    /** Current time in epoch millis. */
    fun now(): Long {
        return System.currentTimeMillis()
    }

    /** Midnight at the start of [date]. Use for the start of a period. */
    fun startOfDay(date: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Long {
        return date.atStartOfDay(zone).toInstant().toEpochMilli()
    }

    /** Midnight at the start of the NEXT day. Use for the end of a period, so the whole end date counts. */
    fun endOfDay(date: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Long {
        return startOfDay(date.plusDays(1), zone)
    }

    /** The calendar date a start time falls on. */
    fun toLocalDate(millis: Long, zone: ZoneId = ZoneId.systemDefault()): LocalDate {
        return Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
    }

    /** The last full day of a period, given its end time from endOfDay(). */
    fun lastDayOf(endMillis: Long, zone: ZoneId = ZoneId.systemDefault()): LocalDate {
        return toLocalDate(endMillis - 1, zone)
    }

    /** Formats a date for screens, e.g. "Nov 2, 2026". */
    fun format(date: LocalDate): String {
        return date.format(displayFormat)
    }
}