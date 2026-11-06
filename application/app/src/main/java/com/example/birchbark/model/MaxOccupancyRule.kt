package com.example.birchbark.model

/**
 * US 02.01.02. Max occupancy is required and has to be at least 1, since a
 * campsite that fits nobody can't be booked.
 */
object MaxOccupancyRule : CampsiteRule {
    override val message = "Max occupancy must be at least 1"

    override fun isValid(campsite: Campsite): Boolean =
        campsite.maxOccupancy >= 1
}