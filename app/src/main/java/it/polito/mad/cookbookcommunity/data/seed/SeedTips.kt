package it.polito.mad.cookbookcommunity.data.seed

import it.polito.mad.cookbookcommunity.model.recipe.RecipeProposal
import it.polito.mad.cookbookcommunity.model.review.Tip
import it.polito.mad.cookbookcommunity.session.SessionManager

object SeedTips {
    fun buildFor(
        recipeOwnedBy101: RecipeProposal,
        recipeOwnedByAnotherUser: RecipeProposal
    ): List<Tip> {
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L

        return listOf(
            Tip(
                id = "seed_tip_for_user101_recipe_1",
                recipeId = recipeOwnedBy101.id,
                authorId = "202",
                text = "Prepare all ingredients on the table before starting.",
                createdAt = now - 2 * oneDayMillis,
                updatedAt = now - 2 * oneDayMillis
            ),
            Tip(
                id = "seed_tip_for_user101_recipe_2",
                recipeId = recipeOwnedBy101.id,
                authorId = "303",
                text = "Add fresh herbs at the end to keep the flavour brighter.",
                createdAt = now - 2 * oneDayMillis + 1,
                updatedAt = now - 2 * oneDayMillis + 1
            ),
            Tip(
                id = "seed_tip_for_non_owner_recipe_1",
                recipeId = recipeOwnedByAnotherUser.id,
                authorId = SessionManager.CURRENT_LOGGED_IN_USER_ID,
                text = "Taste the sauce before serving.",
                createdAt = now - oneDayMillis,
                updatedAt = now - oneDayMillis
            ),
            Tip(
                id = "seed_tip_for_non_owner_recipe_2",
                recipeId = recipeOwnedByAnotherUser.id,
                authorId = SessionManager.CURRENT_LOGGED_IN_USER_ID,
                text = "Reduce the spices if you prefer a milder version.",
                createdAt = now - oneDayMillis + 1,
                updatedAt = now - oneDayMillis + 1
            )
        )
    }
}