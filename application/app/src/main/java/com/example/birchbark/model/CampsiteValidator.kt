package com.example.birchbark.model

/**
 * Runs every [CampsiteRule] against a campsite. Call this before saving.
 * To add a rule, create a new CampsiteRule object and add it to [rules].
 */
object CampsiteValidator {
    /** All rules a campsite must pass before it can be saved. */
    private val rules = listOf(
        CampsiteNameRule,
        CampsiteLocationRule,
        MaxOccupancyRule
    )

    /**
     * Checks a campsite against every rule.
     *
     * @param campsite the campsite to check.
     * @return the message of every rule that failed, so the owner sees all
     *   problems at once. An empty list means the campsite is valid and safe to save.
     */
    fun validate(campsite: Campsite): List<String> {
        val errors = mutableListOf<String>()
        for (rule in rules) {
            if (!rule.isValid(campsite)) {
                errors.add(rule.message)
            }
        }
        return errors
    }
}