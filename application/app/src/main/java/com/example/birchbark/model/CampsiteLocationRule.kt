package com.example.birchbark.model

/**
 * US 02.01.02. Location is required, so the owner gets an error if it's missing.
 * A location that's only spaces counts as missing too.
 */
object CampsiteLocationRule : CampsiteRule {
    override val message = "Location is required"

    override fun isValid(campsite: Campsite): Boolean =
        campsite.location.isNotBlank()
}