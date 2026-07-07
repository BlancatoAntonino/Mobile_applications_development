package it.polito.mad.cookbookcommunity.data.seed

import it.polito.mad.cookbookcommunity.model.recipe.RecipeProposal
import it.polito.mad.cookbookcommunity.model.review.Review
import it.polito.mad.cookbookcommunity.session.SessionManager

object SeedReviews {
    fun buildFor(
        recipeOwnedBy101: RecipeProposal,
        recipeOwnedByAnotherUser: RecipeProposal
    ): List<Review> {
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L

        return listOf(
            Review(
                id = "seed_review_for_user101_recipe",
                recipeId = recipeOwnedBy101.id,
                authorId = "202",
                rating = 5,
                title = "Clear and easy to reproduce",
                text = "I tried this recipe yesterday for lunch and the result was excellent. The steps are clear and the dish is easy to reproduce.",
                photoUri = null,
                createdAt = now - 2 * oneDayMillis,
                updatedAt = now - 2 * oneDayMillis
            ),
            Review(
                id = "seed_review_for_non_owner_recipe",
                recipeId = recipeOwnedByAnotherUser.id,
                authorId = SessionManager.CURRENT_LOGGED_IN_USER_ID,
                rating = 4,
                title = "Good quick meal",
                text = "Good recipe and very useful for a quick meal. I slightly reduced the amount of spices and it worked actually better for my personal taste.",
                photoUri = null,
                createdAt = now - oneDayMillis,
                updatedAt = now - oneDayMillis
            )
        )
    }
}