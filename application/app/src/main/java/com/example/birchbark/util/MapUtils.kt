package com.example.birchbark.util

import java.util.Locale

/**
 * Small helpers for the campsite map location. US 02.01.02 (map location).
 *
 * Kept away from the map itself so they can be unit tested without a phone.
 *
 * Outstanding issues. None.
 */
object MapUtils {

    /** Where the map starts when a campsite has no pin yet, Edmonton. */
    const val DEFAULT_LATITUDE = 53.5461
    const val DEFAULT_LONGITUDE = -113.4938

    /** How zoomed in the map starts with no pin (shows most of Alberta). */
    const val DEFAULT_ZOOM = 6.0

    /** How zoomed in the map starts when there's a pin (shows the area around it). */
    const val PIN_ZOOM = 14.0

    /**
     * Checks a map location is a real spot on Earth.
     *
     * @param latitude north/south, has to be from -90 to 90.
     * @param longitude east/west, has to be from -180 to 180.
     * @return true if both are set and in range.
     */
    fun isValidLocation(latitude: Double?, longitude: Double?): Boolean {
        if (latitude == null || longitude == null) return false
        return latitude in -90.0..90.0 && longitude in -180.0..180.0
    }

    /**
     * The map location as text, like "51.08900, -115.35800". Always uses a
     * dot for decimals so it looks the same on every phone.
     *
     * @param latitude north/south, or null if there's no pin.
     * @param longitude east/west, or null if there's no pin.
     * @return the text, or empty if there's no valid pin.
     */
    fun locationText(latitude: Double?, longitude: Double?): String {
        if (!isValidLocation(latitude, longitude)) return ""
        return String.format(Locale.US, "%.5f, %.5f", latitude, longitude)
    }
}