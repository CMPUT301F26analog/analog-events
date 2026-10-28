package com.example.birchbark.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

/**
 * Unit tests for Time conversions. Uses a fixed time zone so results
 * are the same on every machine.
 */
class TimeTest {

    private val zone = ZoneId.of("America/Edmonton")

    @Test
    fun startOfDay_convertsBackToSameDate() {
        val date = LocalDate.of(2026, 11, 2)
        val milis = Time.startOfDay(date, zone)
        val result = Time.toLocalDate(milis,zone)
        assertEquals(date, result)
    }

    @Test
    fun endOfDay_keepsWholeLastDay() {
        val date = LocalDate.of(2026, 11, 10)
        val endMillis = Time.endOfDay(date, zone)
        val result = Time.lastDayOf(endMillis, zone)
        assertEquals(date, result)
    }

    @Test
    fun format_showsMonthDayYear() {
        val date = LocalDate.of(2026, 11, 2)
        assertEquals("Nov 2, 2026", Time.format(date))
    }
}