package com.example.birchbark.ui.entrant

/**
 * The tabs along the bottom of the entrant screens, from the mockup's bottom
 * navigation (Home, Browse, Bookmarks, Profile).
 *
 * @property label text under the icon.
 * @property icon the symbol used as the icon.
 */
enum class EntrantTab(val label: String, val icon: String) {
    HOME("Home", "⌂"),
    BROWSE("Browse", "⌕"),

    SCAN("Scan", "▦"),
    BOOKMARKS("Bookmarks", "☆"),
    MESSAGES("Messages", "✉"),
    PROFILE("Profile", "⚙")
}