package com.example.birchbark.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [MapUtils].
 * Covers US 02.01.02 (map location), checking a pin is a real spot and how
 * it's shown as text.
 */
class MapUtilsTest {

    @Test
    fun isValidLocation_realSpot_isValid() {
        //Two Jack Lakeside, Banff
        assertTrue(MapUtils.isValidLocation(51.2349, -115.5051))
    }

    @Test
    fun isValidLocation_noPin_isNotValid() {
        assertFalse(MapUtils.isValidLocation(null, null))
        assertFalse(MapUtils.isValidLocation(51.2349, null))
        assertFalse(MapUtils.isValidLocation(null, -115.5051))
    }

    @Test
    fun isValidLocation_outOfRange_isNotValid() {
        assertFalse(MapUtils.isValidLocation(91.0, 0.0))
        assertFalse(MapUtils.isValidLocation(0.0, -181.0))
    }

    @Test
    fun isValidLocation_edgesOfTheMap_areValid() {
        assertTrue(MapUtils.isValidLocation(90.0, 180.0))
        assertTrue(MapUtils.isValidLocation(-90.0, -180.0))
    }

    @Test
    fun locationText_showsFiveDecimals() {
        assertEquals("51.23490, -115.50510", MapUtils.locationText(51.2349, -115.5051))
    }

    @Test
    fun locationText_noPin_isEmpty() {
        assertEquals("", MapUtils.locationText(null, null))
    }

    @Test
    fun locationText_badPin_isEmpty() {
        assertEquals("", MapUtils.locationText(100.0, 0.0))
    }
}