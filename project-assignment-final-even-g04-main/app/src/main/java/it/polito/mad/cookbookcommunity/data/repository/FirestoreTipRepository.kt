package it.polito.mad.cookbookcommunity.data.repository

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import it.polito.mad.cookbookcommunity.data.firestore.FirestoreCollections
import it.polito.mad.cookbookcommunity.data.firestore.FirestoreCollections.Tip as TipFirestore
import it.polito.mad.cookbookcommunity.model.review.Tip
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreTipRepository : TipRepository {
    private val db = Firebase.firestore

    private val collection
        get() = db.collection(FirestoreCollections.TIPS)

    override fun getTipsByRecipe(recipeId: String): Flow<List<Tip>> =
        collection
            .whereEqualTo(TipFirestore.RECIPE_ID, recipeId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects<Tip>()
                    .sortedByDescending { it.createdAt }
            }

    override fun getTipsByAuthor(authorId: String): Flow<List<Tip>> =
        collection
            .whereEqualTo(TipFirestore.AUTHOR_ID, authorId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects<Tip>()
                    .sortedByDescending { it.createdAt }
            }

    override suspend fun addTip(tip: Tip) {
        validateTip(tip)
        val timestamp = System.currentTimeMillis()
        val newTip = tip.copy(
            createdAt = timestamp,
            updatedAt = timestamp
        )

        collection
            .document(newTip.id)
            .set(newTip.toFirestoreMap())
            .await()
    }

    override suspend fun updateTip(tip: Tip) {
        validateTip(tip)
        val updatedTip = tip.copy(updatedAt = System.currentTimeMillis())

        collection
            .document(updatedTip.id)
            .set(updatedTip.toFirestoreMap())
            .await()
    }

    override suspend fun deleteTip(tipId: String) {
        collection
            .document(tipId)
            .delete()
            .await()
    }

    override suspend fun deleteTipsByRecipe(recipeId: String) {
        val tips = collection
            .whereEqualTo(TipFirestore.RECIPE_ID, recipeId)
            .get()
            .await()

        for (doc in tips.documents) {
            doc.reference.delete().await()
        }
    }

    private fun Tip.toFirestoreMap(): Map<String, Any?> = mapOf(
        TipFirestore.ID to id,
        TipFirestore.RECIPE_ID to recipeId,
        TipFirestore.AUTHOR_ID to authorId,
        TipFirestore.TEXT to text,
        TipFirestore.CREATED_AT to createdAt,
        TipFirestore.UPDATED_AT to updatedAt
    )

    private fun validateTip(tip: Tip) {
        require(tip.recipeId.isNotBlank()) { "A tip must be linked to a recipe." }
        require(tip.authorId.isNotBlank()) { "A tip must have an author." }
        require(tip.text.isNotBlank()) { "A tip must contain text." }
    }
}
