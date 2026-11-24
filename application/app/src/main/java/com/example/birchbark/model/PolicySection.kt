package com.example.birchbark.model

/**
 * One titled part of the app policy, like "Respect and conduct".
 *
 * @property title short heading shown above the text.
 * @property body the policy text under the heading.
 */
data class PolicySection(
    val title: String,
    val body: String
)
