package com.example.birchbark.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the campsite rules, validator, rating and search.
 * Covers US 02.01.02, US 01.01.04 and US 01.01.01.
 */
class CampsiteTest {

    /** A campsite that passes every rule, tests change one field at a time. */
    private val valid = Campsite(
        ownerId = "owner123",
        name = "Whistlers Campground",
        location = "Jasper National Park",
        description = "Big sites near the river, has showers and fire pits",
        maxOccupancy = 6,
        tags = listOf("riverside", "pet friendly")
    )

    // US 02.01.02: name is required

    @Test
    fun nameValid_whenSet() {
        assertTrue(CampsiteNameRule.isValid(valid))
    }

    @Test
    fun nameInvalid_whenEmpty() {
        val campsite = valid.copy(name = "")
        assertFalse(CampsiteNameRule.isValid(campsite))
    }

    @Test
    fun nameInvalid_whenOnlySpaces() {
        val campsite = valid.copy(name = "   ")
        assertFalse(CampsiteNameRule.isValid(campsite))
    }

    // US 02.01.02: location is required

    @Test
    fun locationValid_whenSet() {
        assertTrue(CampsiteLocationRule.isValid(valid))
    }

    @Test
    fun locationInvalid_whenEmpty() {
        val campsite = valid.copy(location = "")
        assertFalse(CampsiteLocationRule.isValid(campsite))
    }

    @Test
    fun locationInvalid_whenOnlySpaces() {
        val campsite = valid.copy(location = "   ")
        assertFalse(CampsiteLocationRule.isValid(campsite))
    }

    // US 02.01.02: max occupancy is required

    @Test
    fun maxOccupancyValid_whenOne() {
        val campsite = valid.copy(maxOccupancy = 1)
        assertTrue(MaxOccupancyRule.isValid(campsite))
    }

    @Test
    fun maxOccupancyInvalid_whenZero() {
        val campsite = valid.copy(maxOccupancy = 0)
        assertFalse(MaxOccupancyRule.isValid(campsite))
    }

    @Test
    fun maxOccupancyInvalid_whenNegative() {
        val campsite = valid.copy(maxOccupancy = -2)
        assertFalse(MaxOccupancyRule.isValid(campsite))
    }

    // US 02.01.02: image is optional, so the campsite saves fine without one

    @Test
    fun validatorPasses_whenNoImage() {
        val campsite = valid.copy(imageUrl = null)
        assertTrue(CampsiteValidator.validate(campsite).isEmpty())
    }

    @Test
    fun validatorPasses_whenNoMapLocation() {
        val campsite = valid.copy(latitude = null, longitude = null)
        assertTrue(CampsiteValidator.validate(campsite).isEmpty())
    }

    // Validator shows every problem at once

    @Test
    fun validatorReturnsAllErrors_whenEverythingMissing() {
        val errors = CampsiteValidator.validate(Campsite())
        assertEquals(3, errors.size)
        assertTrue(errors.contains(CampsiteNameRule.message))
        assertTrue(errors.contains(CampsiteLocationRule.message))
        assertTrue(errors.contains(MaxOccupancyRule.message))
    }

    @Test
    fun validatorReturnsOneError_whenOnlyNameMissing() {
        val errors = CampsiteValidator.validate(valid.copy(name = ""))
        assertEquals(listOf(CampsiteNameRule.message), errors)
    }

    // US 01.01.04: no ratings yet shows no ratings instead of a number

    @Test
    fun averageRatingNull_whenNoRatings() {
        assertNull(valid.averageRating())
    }

    @Test
    fun averageRating_isTotalOverCount() {
        val campsite = valid.copy(ratingTotal = 9, ratingCount = 2)
        assertEquals(4.5, campsite.averageRating()!!, 0.0001)
    }

    // US 01.01.01: campsites with a matching name or location show up

    @Test
    fun searchMatches_onName() {
        assertTrue(valid.matchesSearch("whistlers"))
    }

    @Test
    fun searchMatches_onLocation() {
        assertTrue(valid.matchesSearch("Jasper"))
    }

    @Test
    fun searchDoesNotMatch_whenNothingMatches() {
        assertFalse(valid.matchesSearch("Banff"))
    }

    @Test
    fun searchMatchesEverything_whenEmpty() {
        assertTrue(valid.matchesSearch("  "))
    }
}