package com.example.birchbark

import com.example.birchbark.model.CampsiteImage

/**
 * Contains search and list-update rules for the admin Images screen.
 *
 * Keeping these rules separate from Firebase lets them be tested with
 * ordinary unit tests.
 * References.md prompt #90
 */
internal object AdminImageRules {

    /**
     * Checks whether an associated campsite or profile name matches a search.
     *
     * A blank query matches any image. Otherwise, the trimmed query is matched
     * against [name] without regard to letter case.
     *
     * @param name the campsite or profile name associated with the image, or
     * `null` if no name is available.
     * @param query the text entered in the search field.
     * @return `true` if the query matches the name or is blank; otherwise,
     * `false`.
     */
    fun matchesSearch(name: String?, query: String): Boolean {
        val trimmedQuery = query.trim()
        return trimmedQuery.isBlank() ||
                name?.contains(trimmedQuery, ignoreCase = true) == true
    }

    /**
     * Removes an image from the displayed list after its deletion succeeds.
     *
     * @param images the current list of campsite images.
     * @param imageId the ID of the image to remove.
     * @return the list without the image whose ID matches [imageId].
     */
    fun removeDeletedImage(
        images: List<CampsiteImage>,
        imageId: String
    ): List<CampsiteImage> {
        return images.filterNot { it.id == imageId }
    }
}