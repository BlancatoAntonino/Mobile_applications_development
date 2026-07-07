package it.polito.mad.cookbookcommunity.data.review

import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import it.polito.mad.cookbookcommunity.data.firestore.FirestoreCollections
import it.polito.mad.cookbookcommunity.data.firestore.FirestoreCollections.Recipe as RecipeFirestore
import it.polito.mad.cookbookcommunity.data.firestore.FirestoreCollections.Review as ReviewFirestore
import it.polito.mad.cookbookcommunity.data.notification.FirestoreNotificationRepository
import it.polito.mad.cookbookcommunity.data.notification.NotificationRepository
import it.polito.mad.cookbookcommunity.model.notification.AppNotification
import it.polito.mad.cookbookcommunity.model.notification.NotificationType
import it.polito.mad.cookbookcommunity.model.review.Review
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await


class FirestoreReviewRepository(
    private val notificationRepository: NotificationRepository = FirestoreNotificationRepository()
) : ReviewRepository {
    private val db = Firebase.firestore

    private val collection
        get() = db.collection(FirestoreCollections.REVIEWS)

    private val recipesCollection
        get() = db.collection(FirestoreCollections.RECIPES)

    override fun getReviewsByRecipe(recipeId: String): Flow<List<Review>> =
        collection
            .whereEqualTo(ReviewFirestore.RECIPE_ID, recipeId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects<Review>()
                    .sortedByDescending { review -> review.createdAt }
            }

    override fun getReviewsByAuthor(authorId: String): Flow<List<Review>> =
        collection
            .whereEqualTo(ReviewFirestore.AUTHOR_ID, authorId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects<Review>()
                    .sortedByDescending { review -> review.createdAt }
            }

    override fun getAverageRating(recipeId: String): Flow<Double?> =
        getReviewsByRecipe(recipeId)
            .map { reviews ->
                if (reviews.isEmpty())
                    null
                else
                    reviews.map { it.rating }.average()
            }

    override suspend fun addReview(review: Review) {
        val timestamp = System.currentTimeMillis()
        val recipeSnapshot = recipesCollection.document(review.recipeId).get().await()
        val recipeOwnerId = recipeSnapshot.getString(RecipeFirestore.OWNER_ID).orEmpty()
            .ifBlank { review.recipeOwnerId }
        val recipeTitle = recipeSnapshot.getString(RecipeFirestore.TITLE).orEmpty()
            .ifBlank { review.recipeTitle }

        val reviewWithTimestamp = review.copy(
            recipeTitle = recipeTitle,
            recipeOwnerId = recipeOwnerId,
            createdAt = timestamp,
            updatedAt = timestamp
        )
        collection
            .document(review.id)
            .set(reviewWithTimestamp.toFirestoreMap())
            .await()

        runCatching {
            updateRecipeValues(review.recipeId)
        }

        runCatching {
            createReviewReceivedNotification(reviewWithTimestamp)
        }
    }

    override suspend fun updateReview(review: Review) {
        val updated = review.copy(updatedAt = System.currentTimeMillis())

        collection
            .document(updated.id)
            .set(updated.toFirestoreMap())
            .await()

        runCatching {
            updateRecipeValues(updated.recipeId)
        }
    }

    override suspend fun deleteReview(reviewId: String) {
        val snapshot = collection.document(reviewId).get().await()
        val recipeId = snapshot.getString(ReviewFirestore.RECIPE_ID)

        collection
            .document(reviewId)
            .delete()
            .await()

        if(!recipeId.isNullOrBlank()) {
            runCatching {
                updateRecipeValues(recipeId)
            }
        }
    }

    override suspend fun deleteReviewsByRecipe(recipeId: String) {
        val reviews = collection
            .whereEqualTo(ReviewFirestore.RECIPE_ID, recipeId)
            .get()
            .await()

        for (doc in reviews.documents) {
            doc.reference.delete().await()
        }

        recipesCollection.document(recipeId)
            .update(
                FirestoreCollections.Recipe.AVERAGE_RATING, 0.0,
                FirestoreCollections.Recipe.REVIEW_COUNT, 0
            )
            .await()
    }

    private suspend fun updateRecipeValues(recipeId: String) {
        val reviews = collection
            .whereEqualTo(ReviewFirestore.RECIPE_ID, recipeId)
            .get()
            .await()

        val ratings = reviews.documents.mapNotNull { it.getLong(ReviewFirestore.RATING)?.toInt() }
        val count = ratings.size
        val average = if (count > 0) ratings.average() else 0.0

        recipesCollection.document(recipeId)
            .update(
                FirestoreCollections.Recipe.AVERAGE_RATING, average,
                FirestoreCollections.Recipe.REVIEW_COUNT, count
            )
            .await()
    }

    private fun Review.toFirestoreMap(): Map<String, Any?> = mapOf(
        ReviewFirestore.ID to id,
        ReviewFirestore.RECIPE_ID to recipeId,
        ReviewFirestore.RECIPE_TITLE to recipeTitle,
        ReviewFirestore.RECIPE_OWNER_ID to recipeOwnerId,
        ReviewFirestore.AUTHOR_ID to authorId,
        ReviewFirestore.AUTHOR_DISPLAY_NAME to authorDisplayName,
        ReviewFirestore.AUTHOR_PHOTO_URL to authorPhotoUrl,
        ReviewFirestore.RATING to rating,
        ReviewFirestore.TITLE to title,
        ReviewFirestore.TEXT to text,
        ReviewFirestore.PHOTO_URI to photoUri,
        ReviewFirestore.CREATED_AT to createdAt,
        ReviewFirestore.UPDATED_AT to updatedAt
    )

    private suspend fun createReviewReceivedNotification(review: Review) {
        val recipientUserId = review.recipeOwnerId.takeIf { it.isNotBlank() && it != "unknown" }
            ?: return

        if (recipientUserId == review.authorId) return

        val actorName = review.authorDisplayName.ifBlank { "Someone" }
        val recipeTitle = review.recipeTitle.ifBlank { "your recipe" }

        notificationRepository.createNotification(
            AppNotification(
                recipientUserId = recipientUserId,
                actorUserId = review.authorId,
                actorDisplayName = actorName,
                type = NotificationType.REVIEW_RECEIVED,
                title = "New review on your recipe",
                message = "$actorName reviewed $recipeTitle.",
                recipeId = review.recipeId,
                reviewId = review.id
            )
        )
    }
}
