package com.example.birchbark.model

/**
 * US 02.01.02. Name is required, so the owner gets an error if it's missing.
 * A name that's only spaces counts as missing too.
 */
object CampsiteNameRule : CampsiteRule {
    override val message = "Name is required"

    override fun isValid(campsite: Campsite): Boolean =
        campsite.name.isNotBlank()
}