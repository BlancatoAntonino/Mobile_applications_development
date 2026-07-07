package it.polito.mad.cookbookcommunity.data.repository

import it.polito.mad.cookbookcommunity.model.recipe.RecipeProposal
import kotlinx.coroutines.flow.Flow

interface RecipeProposalRepository {
    fun getAllRecipeProposals(): Flow<List<RecipeProposal>>

    fun getOwnedRecipeProposals(ownerId: String): Flow<List<RecipeProposal>>

    fun getRecipeProposalById(id: String): Flow<RecipeProposal?>

    suspend fun addRecipeProposal(recipe: RecipeProposal)

    suspend fun updateRecipeProposal(recipe: RecipeProposal)

    suspend fun deleteRecipeProposal(id: String)
}