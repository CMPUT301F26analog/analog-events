package com.example.birchbark.model

/**
 * One validation rule for a campsite's details. Each rule is its own object
 * (like [CampsiteNameRule]), and [CampsiteValidator] runs all of them before saving.
 */
interface CampsiteRule {
    /** Error message shown to the owner when this rule fails. */
    val message: String

    /**
     * Checks one campsite against this rule.
     *
     * @param campsite the campsite to check.
     * @return true if the campsite passes this rule, false otherwise.
     */
    fun isValid(campsite: Campsite): Boolean
}