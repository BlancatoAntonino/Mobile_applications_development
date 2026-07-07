package it.polito.mad.cookbookcommunity.data.repository

import it.polito.mad.cookbookcommunity.model.review.TriedRecipeLog
import kotlinx.coroutines.flow.Flow

interface TriedRecipeRepository {
    fun getDiaryEntriesByUser(userId: String): Flow<List<TriedRecipeLog>>

    fun getDiaryEntryById(entryId: String): Flow<TriedRecipeLog?>

    fun getDiaryEntryForRecipe(
        userId: String,
        recipeId: String
    ): Flow<TriedRecipeLog?>

    suspend fun addDiaryEntry(entry: TriedRecipeLog)

    suspend fun updateDiaryEntry(entry: TriedRecipeLog)

    suspend fun upsertDiaryEntry(entry: TriedRecipeLog)

    suspend fun linkReviewToDiaryEntry(
        userId: String,
        recipeId: String,
        reviewId: String,
        recipeTitle: String,
        recipeImageUri: String,
        cookedAt: Long
    )

    suspend fun deleteDiaryEntry(entryId: String)

    suspend fun deleteDiaryEntriesByRecipe(recipeId: String)

    fun getTriedRecipesByUser(userId: String): Flow<List<TriedRecipeLog>> =
        getDiaryEntriesByUser(userId)

    suspend fun addTriedRecipe(log: TriedRecipeLog) {
        addDiaryEntry(log)
    }

    suspend fun deleteTriedRecipe(logId: String) {
        deleteDiaryEntry(logId)
    }

    suspend fun deleteTriedRecipesByRecipe(recipeId: String) {
        deleteDiaryEntriesByRecipe(recipeId)
    }
}