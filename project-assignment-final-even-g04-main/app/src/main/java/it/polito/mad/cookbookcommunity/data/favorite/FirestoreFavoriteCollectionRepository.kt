package it.polito.mad.cookbookcommunity.data.favorite

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import it.polito.mad.cookbookcommunity.data.favorite.FavoriteCollectionRepository
import it.polito.mad.cookbookcommunity.data.firestore.FirestoreCollections
import it.polito.mad.cookbookcommunity.data.firestore.FirestoreCollections.FavoriteCollection as FavColFirestore
import it.polito.mad.cookbookcommunity.model.collection.FavoriteCollection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreFavoriteCollectionRepository : FavoriteCollectionRepository {
    private val db = Firebase.firestore

    private val collection
        get() = db.collection(FirestoreCollections.FAVORITE_COLLECTIONS)

    override fun observeCollectionsByUser(userId: String): Flow<List<FavoriteCollection>> =
        collection
            .whereEqualTo(FavColFirestore.OWNER_ID, userId)
            .snapshots()
            .map { snapshot -> snapshot.toObjects<FavoriteCollection>() }

    override fun observeCollectionById(collectionId: String): Flow<FavoriteCollection?> =
        collection
            .document(collectionId)
            .snapshots()
            .map { snapshot -> snapshot.toObject<FavoriteCollection>() }

    override suspend fun createCollection(ownerId: String, name: String): String {
        val newCollection = FavoriteCollection(
            ownerId = ownerId,
            name = name,
            isSystem = false
        )

        collection
            .document(newCollection.id)
            .set(newCollection.toFirestoreMap())
            .await()

        return newCollection.id
    }

    /* override suspend fun addRecipeToCollection(collectionId: String, recipeId: String) {
        collection
            .document(collectionId)
            .update(
                "recipeIds", FieldValue.arrayUnion(recipeId),
                "updatedAt", System.currentTimeMillis()
            )
            .await()
    }

    override suspend fun removeRecipeFromCollection(collectionId: String, recipeId: String) {
        collection
            .document(collectionId)
            .update(
                "recipeIds", FieldValue.arrayRemove(recipeId),
                "updatedAt", System.currentTimeMillis()
            )
            .await()
    }

    override suspend fun removeRecipeFromAllCollections(recipeId: String) {
        val collections = collection.get().await()
        for (doc in collections.documents) {
            val coll = doc.toObject<FavoriteCollection>() ?: continue
            if (coll.recipeIds.contains(recipeId)) {
                removeRecipeFromCollection(coll.id, recipeId)
            }
        }
    } */

    override suspend fun renameCollection(collectionId: String, newName: String) {
        collection
            .document(collectionId)
            .update(
                FavColFirestore.NAME, newName,
                FavColFirestore.UPDATED_AT, System.currentTimeMillis()
            )
            .await()
    }

    override suspend fun deleteCollection(collectionId: String) {
        collection
            .document(collectionId)
            .delete()
            .await()
    }

    override suspend fun ensureSystemCollections(ownerId: String) {
        val savedRecipesId = FavoriteCollection.savedRecipesId(ownerId)

        val existingDoc = collection.document(savedRecipesId).get().await()
        if (!existingDoc.exists()) {
            val systemCollection = FavoriteCollection(
                id       = savedRecipesId,
                ownerId  = ownerId,
                name     = FavoriteCollection.SAVED_RECIPES_NAME,
                isSystem = true
            )
            collection
                .document(savedRecipesId)
                .set(systemCollection.toFirestoreMap())
                .await()
        }
    }

    /* override fun getTriedRecipesCollection(ownerId: String): Flow<FavoriteCollection?> =
        collection
            .whereEqualTo("ownerId", ownerId)
            .whereEqualTo("name", FavoriteCollection.TRIED_RECIPES_NAME)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.firstOrNull()?.toObject<FavoriteCollection>()
            }
     */

    private fun FavoriteCollection.toFirestoreMap(): Map<String, Any?> = mapOf(
        FavColFirestore.ID         to id,
        FavColFirestore.OWNER_ID   to ownerId,
        FavColFirestore.NAME       to name,
        FavColFirestore.CREATED_AT to createdAt,
        FavColFirestore.UPDATED_AT to updatedAt,
        FavColFirestore.IS_SYSTEM  to isSystem
    )
}