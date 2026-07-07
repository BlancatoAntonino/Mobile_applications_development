package it.polito.mad.cookbookcommunity.data.favorite

import it.polito.mad.cookbookcommunity.model.collection.Favorite
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun observeFavoritesByUser(userId: String): Flow<List<Favorite>>

    suspend fun saveFavorite(favorite: Favorite)

    suspend fun removeFavorite(favoriteId: String)

    suspend fun isFavorite(userId: String, recipeId: String): Boolean

    suspend fun getFavoriteId(userId: String, recipeId: String): String?

    suspend fun removeRecipeFromFavorites(userId: String, recipeId: String)

    suspend fun removeRecipeFromAllFavorites(recipeId: String)
}