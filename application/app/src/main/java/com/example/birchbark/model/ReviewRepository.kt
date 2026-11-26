package com.example.birchbark.model

import com.example.birchbark.util.Time
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query

// See [TBD 4] in LLM.md

/**
 * Saves and loads [Review] objects, and keeps the campsite's overall rating up
 * to date. Reviews live in the "reviews" subcollection of their campsite:
 * campsites/{campsiteId}/reviews/{entrantId}.
 *
 * Firestore calls are asynchronous, so no function here returns a result
 * directly. Each one reports back later through a [SaveCallback] or [LoadCallback].
 *
 * Outstanding issues. Nothing checks the entrant is who they say they are, and
 * entrants can change a campsite's rating fields, so both need Firestore security
 * rules (An entrant can only write their own review, and the only campsite fields
 * an entrant can change are ratingTotal and ratingCount). A transaction needs a
 * network connection, so rating while offline fails instead of waiting.
 */
class ReviewRepository {

    //created on first use, so checking an invalid review never needs Firebase (and can be unit tested)
    private val db by lazy { FirebaseFirestore.getInstance() }

    /** Receives the result of [saveReview]. Exactly one function is called. */
    interface SaveCallback {
        /**
         * The review was saved and the campsite's rating was updated.
         * @param review the saved review.
         * @param campsite the campsite with its new rating totals, so the screens can show the new overall rating.
         */
        fun onSuccess(review: Review, campsite: Campsite)

        /**
         * The review broke a rule, so nothing was saved.
         * @param errors messages to show the entrant, one per problem.
         */
        fun onInvalid(errors: List<String>)

        /**
         * Firestore failed, e.g. no network, no permission, or the campsite was deleted.
         * Nothing was saved.
         * @param error the exception Firestore reported.
         */
        fun onError(error: Exception)
    }

    /** Receives the result of [getReview]. Exactly one function is called. */
    interface LoadCallback {
        /**
         * The entrant's review was found.
         * @param review the loaded review.
         */
        fun onLoaded(review: Review)

        /** The entrant hasn't reviewed this campsite yet. */
        fun onNotFound()

        /**
         * Firestore failed, e.g. no network or no permission.
         * @param error the exception Firestore reported.
         */
        fun onError(error: Exception)
    }

    /** Receives the result of [getAllReviews]. Exactly one function is called. */
    interface LoadAllCallback {
        fun onLoaded(reviews: List<Review>)
        fun onError(error: Exception)
    }

    /**
     * Loads all reviews for a campsite, ordered newest first.
     *
     * @param campsiteId the campsite's ID ([Campsite.id]).
     * @param callback receives the list of reviews or an error.
     */
    fun getAllReviews(campsiteId: String, callback: LoadAllCallback) {
        if (campsiteId.isBlank()) {
            callback.onLoaded(emptyList())
            return
        }

        db.collection(CAMPSITES).document(campsiteId)
            .collection(REVIEWS)
            .orderBy("postedAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val reviews = try {
                    snapshot.documents.mapNotNull { it.toObject(Review::class.java) }
                } catch (e: Exception) {
                    callback.onError(e)
                    return@addOnSuccessListener
                }
                callback.onLoaded(reviews)
            }
            .addOnFailureListener { error -> callback.onError(error) }
    }

    /**
     * Loads one entrant's review of a campsite, so the form can show it and
     * the entrant can update it.
     *
     * @param campsiteId the campsite's ID ([Campsite.id]).
     * @param entrantId the entrant's FID ([Review.entrantId]).
     * @param callback receives the review, a not-found result, or an error.
     */
    fun getReview(campsiteId: String, entrantId: String, callback: LoadCallback) {
        //an empty ID isn't a valid Firestore path, and there can't be a review for it anyway
        if (campsiteId.isBlank() || entrantId.isBlank()) {
            callback.onNotFound()
            return
        }

        db.collection(CAMPSITES).document(campsiteId)
            .collection(REVIEWS).document(entrantId).get()
            .addOnSuccessListener { document ->
                val review = try {
                    document.toObject(Review::class.java)
                } catch (e: Exception) {
                    callback.onError(e)
                    return@addOnSuccessListener
                }
                if (review == null) {
                    callback.onNotFound()
                } else {
                    callback.onLoaded(review)
                }
            }
            .addOnFailureListener { error -> callback.onError(error) }
    }

    /**
     * US 01.13.01 and US 01.13.02: saves an entrant's rating and/or review of a campsite, and
     * updates the campsite's overall rating to include it. Runs [ReviewValidator]
     * first and refuses to save an invalid review.
     *
     * If the entrant already reviewed this campsite the old review is replaced
     * and their old rating is taken out of the totals first, so a rating is
     * never counted twice. It all happens in one transaction: either the review
     * and the new totals are both saved, or neither is.
     *
     * @param review the review to save. Its [Review.postedAt] is ignored and replaced.
     * @param callback onSuccess with the saved review and campsite, onInvalid with the
     *   rule messages, or onError if Firestore fails.
     */
    fun saveReview(review: Review, callback: SaveCallback) {
        val cleaned = review.copy(comment = review.comment.trim())

        //checking rules before touching DB
        val errors = ReviewValidator.validate(cleaned)
        if (errors.isNotEmpty()) {
            callback.onInvalid(errors)
            return
        }

        val campsiteRef = db.collection(CAMPSITES).document(cleaned.campsiteId)
        val reviewRef = campsiteRef.collection(REVIEWS).document(cleaned.entrantId)

        db.runTransaction { transaction ->
            //a transaction has to do all its reads before any writes
            val campsite = transaction.get(campsiteRef).toObject(Campsite::class.java)
                ?: throw FirebaseFirestoreException(
                    "Campsite not found",
                    FirebaseFirestoreException.Code.NOT_FOUND
                )
            val oldRating = transaction.get(reviewRef).toObject(Review::class.java)?.rating

            val toSave = cleaned.copy(postedAt = Time.now())
            val updatedCampsite = RatingCalculator.update(campsite, oldRating, toSave.rating)

            transaction.set(reviewRef, toSave)
            //a review with no rating change (e.g. only text) leaves the campsite alone
            if (updatedCampsite.ratingTotal != campsite.ratingTotal ||
                updatedCampsite.ratingCount != campsite.ratingCount
            ) {
                transaction.update(
                    campsiteRef,
                    "ratingTotal", updatedCampsite.ratingTotal,
                    "ratingCount", updatedCampsite.ratingCount
                )
            }

            Pair(toSave, updatedCampsite)
        }
            .addOnSuccessListener { (savedReview, savedCampsite) ->
                callback.onSuccess(savedReview, savedCampsite)
            }
            .addOnFailureListener { error ->
                callback.onError(error)
            }
    }

    private companion object {
        const val CAMPSITES = "campsites"
        const val REVIEWS = "reviews"
    }

    /** Pairs a user's review with the campsite name for their Profile history screen. */
    data class UserReviewItem(
        val review: Review,
        val campsiteName: String
    )

    interface LoadUserReviewsCallback {
        fun onLoaded(items: List<UserReviewItem>)
        fun onError(error: Exception)
    }

    /**
     * Loads all reviews written by a specific entrant across all campsites.
     */
    fun getReviewsByEntrant(entrantId: String, callback: LoadUserReviewsCallback) {
        if (entrantId.isBlank()) {
            callback.onLoaded(emptyList())
            return
        }

        db.collection(CAMPSITES).get()
            .addOnSuccessListener { campsitesSnap ->
                val campsites = campsitesSnap.documents.mapNotNull { doc ->
                    val name = doc.getString("name") ?: "Unknown Campsite"
                    doc.id to name
                }

                if (campsites.isEmpty()) {
                    callback.onLoaded(emptyList())
                    return@addOnSuccessListener
                }

                val results = mutableListOf<UserReviewItem>()
                var remaining = campsites.size

                for ((campsiteId, campsiteName) in campsites) {
                    db.collection(CAMPSITES).document(campsiteId)
                        .collection(REVIEWS).document(entrantId).get()
                        .addOnSuccessListener { reviewDoc ->
                            val review = reviewDoc.toObject(Review::class.java)
                            if (review != null) {
                                results.add(UserReviewItem(review, campsiteName))
                            }
                            remaining--
                            if (remaining == 0) {
                                callback.onLoaded(results.sortedByDescending { it.review.postedAt })
                            }
                        }
                        .addOnFailureListener {
                            remaining--
                            if (remaining == 0) {
                                callback.onLoaded(results.sortedByDescending { it.review.postedAt })
                            }
                        }
                }
            }
            .addOnFailureListener { error -> callback.onError(error) }
    }
}