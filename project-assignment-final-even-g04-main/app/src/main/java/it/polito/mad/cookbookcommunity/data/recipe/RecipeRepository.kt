package it.polito.mad.cookbookcommunity.data.recipe

import it.polito.mad.cookbookcommunity.model.recipe.RecipeProposal
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {

    fun getAllRecipeProposals(): Flow<List<RecipeProposal>>
    fun getOwnedRecipeProposals(ownerId: String): Flow<List<RecipeProposal>>
    fun getRecipeProposalById(recipeId: String): Flow<RecipeProposal?>
    suspend fun addRecipeProposal(recipe: RecipeProposal)
    suspend fun updateRecipeProposal(recipe: RecipeProposal)
    suspend fun deleteRecipeProposal(recipeId: String)
}