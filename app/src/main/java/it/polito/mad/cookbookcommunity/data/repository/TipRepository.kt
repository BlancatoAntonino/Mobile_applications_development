package it.polito.mad.cookbookcommunity.data.repository

import it.polito.mad.cookbookcommunity.model.review.Tip
import kotlinx.coroutines.flow.Flow

interface TipRepository {
    fun getTipsByRecipe(recipeId: String): Flow<List<Tip>>

    fun getTipsByAuthor(authorId: String): Flow<List<Tip>>

    suspend fun addTip(tip: Tip)

    suspend fun updateTip(tip: Tip)

    suspend fun deleteTip(tipId: String)

    suspend fun deleteTipsByRecipe(recipeId: String)
}