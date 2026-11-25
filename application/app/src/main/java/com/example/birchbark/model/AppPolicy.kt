package com.example.birchbark.model

/**
 * US 01.11.01 (issue #24): the app policy and guidelines an entrant can read
 * from the menu. It's plain data in code so the screen and the tests use the
 * same copy. To add or change a section, edit [sections]; the screen shows
 * every section in order.
 *
 * Outstanding issues. The wording is a first draft for the team to review.
 * It's written in code instead of Firestore, so changing it
 * means building a new version of the app.
 */
object AppPolicy {
    /** Heading shown at the top of the policy screen. */
    const val TITLE = "App Policy and Guidelines"

    /** Every part of the policy, in the order it's shown. */
    val sections: List<PolicySection> = listOf(
        PolicySection(
            title = "Using BirchBark",
            body = "BirchBark helps you join lotteries for campsites that fill up fast. " +
                    "Please use it honestly, and only to book stays you really plan to take."
        ),
        PolicySection(
            title = "How the lottery works",
            body = "Joining a waiting list is free and doesn't guarantee a spot. " +
                    "When registration ends, winners are drawn at random. " +
                    "If you win, you can reserve the dates offered, or decline if your plans changed."
        ),
        PolicySection(
            title = "Reservations and cancellations",
            body = "Each campsite shows its own rules and cancellation policy. " +
                    "Read them before you confirm. You can cancel a reservation from your list " +
                    "of reservations. An owner may reject a reservation, for example because of " +
                    "a safety warning, and you will be told."
        ),
        PolicySection(
            title = "Respect and conduct",
            body = "Be respectful to owners and other campers, and follow the campsite's rules " +
                    "during your stay. Don't make false reservations or send abusive messages."
        ),
        PolicySection(
            title = "Notifications",
            body = "We send notifications when you win or lose a lottery and when a reservation " +
                    "changes. You can opt out of notifications at any time."
        ),
        PolicySection(
            title = "Your information",
            body = "Your profile (name, email, phone number, and photo) is only used to run your " +
                    "reservations. The app knows you by your device, so you don't need a password. " +
                    "You can update or delete your profile at any time."
        ),
        PolicySection(
            title = "Photos",
            body = "Only upload images you are allowed to share. Administrators can remove " +
                    "images that break these guidelines."
        ),
        PolicySection(
            title = "Breaking the guidelines",
            body = "Administrators can remove campsites, profiles, images and campsite owners " +
                    "that break this policy."
        )
    )
}