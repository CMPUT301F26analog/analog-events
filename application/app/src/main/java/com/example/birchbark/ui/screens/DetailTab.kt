package com.example.birchbark.ui.screens

/**
 * The informational categories shown on the campsite details screen.
 *
 * @property label Text on the tab.
 * @property emptyText Shown when the owner left that section blank.
 */
enum class DetailTab(val label: String, val emptyText: String) {
    DESCRIPTION("Description", "No description provided."),
    RULES("Rules", "No rules provided."),
    POLICY("Cancellation", "No cancellation policy provided.")
}