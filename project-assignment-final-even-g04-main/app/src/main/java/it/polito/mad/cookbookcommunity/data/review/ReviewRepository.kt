package it.polito.mad.cookbookcommunity.data.review

import it.polito.mad.cookbookcommunity.model.review.Review
import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    fun getReviewsByRecipe(recipeId: String): Flow<List<Review>>

    fun getReviewsByAuthor(authorId: String): Flow<List<Review>>

    fun getAverageRating(recipeId: String): Flow<Double?>

    suspend fun addReview(review: Review)

    suspend fun updateReview(review: Review)

    suspend fun deleteReview(reviewId: String)

    suspend fun deleteReviewsByRecipe(recipeId: String)
}