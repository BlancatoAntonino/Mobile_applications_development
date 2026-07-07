package it.polito.mad.cookbookcommunity.data.favorite

import it.polito.mad.cookbookcommunity.model.collection.FavoriteCollection
import kotlinx.coroutines.flow.Flow

interface FavoriteCollectionRepository {
    fun observeCollectionsByUser(userId: String): Flow<List<FavoriteCollection>>
    fun observeCollectionById(collectionId: String): Flow<FavoriteCollection?>
    suspend fun createCollection(ownerId: String, name: String): String

    suspend fun renameCollection(collectionId: String, newName: String)
    suspend fun deleteCollection(collectionId: String)

    suspend fun ensureSystemCollections(ownerId: String)

    //fun getTriedRecipesCollection(ownerId: String): Flow<FavoriteCollection?>
}