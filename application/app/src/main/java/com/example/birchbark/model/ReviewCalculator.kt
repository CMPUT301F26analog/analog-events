package com.example.birchbark.model

/**
 * Works out a campsite's new rating totals when an entrant gives, changes or
 * removes their rating. It's plain maths with no Firestore, so it can be unit
 * tested, and [ReviewRepository] runs it inside a transaction so two entrants
 * rating at the same moment can't overwrite each other.
 *
 * The campsite stores the total of all ratings and how many there are, and
 * [Campsite.averageRating] divides them.
 */
object RatingCalculator {

    /**
     * Gives the campsite with its totals updated for one entrant's rating.
     * The entrant's old rating (if they had one) is taken out and the new one
     * (if they have one) is put in, so changing a rating never counts it twice.
     *
     * @param campsite the campsite as it is stored now.
     * @param oldRating the entrant's rating before this change, or null if they had none.
     * @param newRating the entrant's rating after this change, or null if they have none
     * @return a copy of [campsite] with the new [Campsite.ratingTotal] and [Campsite.ratingCount].
     *   They never go below 0.
     */
    fun update(campsite: Campsite, oldRating: Int?, newRating: Int?): Campsite {
        var total = campsite.ratingTotal
        var count = campsite.ratingCount

        if (oldRating != null) {
            total -= oldRating
            count -= 1
        }
        if (newRating != null) {
            total += newRating
            count += 1
        }

        // See [TBD 3] in LLM.md
        return campsite.copy(
            ratingTotal = total.coerceAtLeast(0),
            ratingCount = count.coerceAtLeast(0)
        )
    }
}
