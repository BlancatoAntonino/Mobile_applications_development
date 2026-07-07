package it.polito.mad.cookbookcommunity.data.favorite

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import it.polito.mad.cookbookcommunity.data.firestore.FirestoreCollections
import it.polito.mad.cookbookcommunity.data.firestore.FirestoreCollections.Favorite as FavoriteFirestore
import it.polito.mad.cookbookcommunity.model.collection.Favorite
import it.polito.mad.cookbookcommunity.model.collection.FavoriteCollection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreFavoriteRepository : FavoriteRepository {
    private val db = Firebase.firestore

    private val collection
        get() = db.collection(FirestoreCollections.FAVORITES)

    override fun observeFavoritesByUser(userId: String): Flow<List<Favorite>> =
        collection
            .whereEqualTo(FavoriteFirestore.USER_ID, userId)
            .snapshots()
            .map { snapshot -> snapshot.toObjects<Favorite>() }

    override suspend fun saveFavorite(favorite: Favorite) {
        val safeId = favorite.id.ifBlank {
            Favorite.idFor(favorite.userId, favorite.recipeId, favorite.collectionName)
        }
        collection
            .document(safeId)
            .set(favorite.copy(id = safeId).toFirestoreMap())
            .await()
    }

    override suspend fun removeFavorite(favoriteId: String) {
        collection
            .document(favoriteId)
            .delete()
            .await()
    }

    override suspend fun isFavorite(userId: String, recipeId: String): Boolean {
        val defaultId = Favorite.idFor(userId, recipeId, FavoriteCollection.SAVED_RECIPES_NAME)
        val defaultDoc = collection.document(defaultId).get().await()

        if (defaultDoc.exists()) {
            return true
        }

        val result = collection
            .whereEqualTo(FavoriteFirestore.USER_ID, userId)
            .whereEqualTo(FavoriteFirestore.RECIPE_ID, recipeId)
            .limit(1)
            .get()
            .await()
        return !result.isEmpty
    }

    override suspend fun getFavoriteId(userId: String, recipeId: String): String? {
        val defaultId = Favorite.idFor(userId, recipeId, FavoriteCollection.SAVED_RECIPES_NAME)
        val defaultDoc = collection.document(defaultId).get().await()

        if (defaultDoc.exists()) {
            return defaultId
        }

        val result = collection
            .whereEqualTo(FavoriteFirestore.USER_ID, userId)
            .whereEqualTo(FavoriteFirestore.RECIPE_ID, recipeId)
            .limit(1)
            .get()
            .await()
        return result.documents.firstOrNull()?.id
    }

    override suspend fun removeRecipeFromFavorites(userId: String, recipeId: String) {
        val defaultId = Favorite.idFor(userId, recipeId, FavoriteCollection.SAVED_RECIPES_NAME)
        val defaultDoc = collection.document(defaultId).get().await()

        if (defaultDoc.exists()) {
            defaultDoc.reference.delete().await()
        }

        val result = collection
            .whereEqualTo(FavoriteFirestore.USER_ID, userId)
            .whereEqualTo(FavoriteFirestore.RECIPE_ID, recipeId)
            .get()
            .await()

        for (doc in result.documents) {
            doc.reference.delete().await()
        }
    }

    override suspend fun removeRecipeFromAllFavorites(recipeId: String) {
        val result = collection
            .whereEqualTo(FavoriteFirestore.RECIPE_ID, recipeId)
            .get()
            .await()

        for (doc in result.documents) {
            doc.reference.delete().await()
        }
    }

    private fun Favorite.toFirestoreMap(): Map<String, Any?> = mapOf(
        FavoriteFirestore.ID to id,
        FavoriteFirestore.USER_ID to userId,
        FavoriteFirestore.RECIPE_ID to recipeId,
        FavoriteFirestore.RECIPE_TITLE to recipeTitle,
        FavoriteFirestore.RECIPE_IMAGE_URI to recipeImageUri,
        FavoriteFirestore.RECIPE_OWNER_ID to recipeOwnerId,
        FavoriteFirestore.COLLECTION_ID to collectionId,
        FavoriteFirestore.COLLECTION_NAME to collectionName,
        FavoriteFirestore.SAVED_AT to savedAt
    )
}