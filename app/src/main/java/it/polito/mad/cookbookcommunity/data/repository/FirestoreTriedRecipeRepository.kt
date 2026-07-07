package it.polito.mad.cookbookcommunity.data.repository

import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.snapshots
import it.polito.mad.cookbookcommunity.data.firestore.FirestoreCollections
import it.polito.mad.cookbookcommunity.model.review.CookingResult
import it.polito.mad.cookbookcommunity.data.firestore.FirestoreCollections.TriedRecipe as TriedRecipeFirestore
import it.polito.mad.cookbookcommunity.model.review.TriedRecipeLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlin.collections.sortedByDescending

class FirestoreTriedRecipeRepository : TriedRecipeRepository {
    private val db = Firebase.firestore

    private val collection
        get() = db.collection(FirestoreCollections.TRIED_RECIPES)

    override fun getDiaryEntriesByUser(userId: String): Flow<List<TriedRecipeLog>> =
        collection
            .whereEqualTo(TriedRecipeFirestore.USER_ID, userId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents
                    .mapNotNull { document -> document.toTriedRecipeLog() }
                    .sortedByDescending { it.cookedAt }
            }

    override fun getDiaryEntryById(entryId: String): Flow<TriedRecipeLog?> =
        collection
            .document(entryId)
            .snapshots()
            .map { document -> document.toTriedRecipeLog() }

    override fun getDiaryEntryForRecipe(
        userId: String,
        recipeId: String
    ): Flow<TriedRecipeLog?> =
        collection
            .whereEqualTo(TriedRecipeFirestore.USER_ID, userId)
            .whereEqualTo(TriedRecipeFirestore.RECIPE_ID, recipeId)
            .limit(1)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.firstOrNull()?.toTriedRecipeLog()
            }

    override suspend fun addDiaryEntry(entry: TriedRecipeLog) {
        upsertDiaryEntry(entry)
    }

    override suspend fun updateDiaryEntry(entry: TriedRecipeLog) {
        validateTriedRecipeLog(entry)

        val now = System.currentTimeMillis()
        val normalizedEntry = entry.normalizedForWrite(updatedAt = now)

        collection
            .document(normalizedEntry.id)
            .set(normalizedEntry.toFirestoreMap(), SetOptions.merge())
            .await()
    }

    override suspend fun upsertDiaryEntry(entry: TriedRecipeLog) {
        validateTriedRecipeLog(entry)

        val existingEntryDocument = collection
            .whereEqualTo(TriedRecipeFirestore.USER_ID, entry.userId)
            .whereEqualTo(TriedRecipeFirestore.RECIPE_ID, entry.recipeId)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()

        val now = System.currentTimeMillis()
        val existingEntry = existingEntryDocument?.toTriedRecipeLog()

        val normalizedEntry = entry.copy(
            id = existingEntry?.id ?: existingEntryDocument?.id ?: entry.id,
            createdAt = existingEntry?.createdAt ?: entry.createdAt,
            updatedAt = now
        ).normalizedForWrite(updatedAt = now)

        collection
            .document(normalizedEntry.id)
            .set(normalizedEntry.toFirestoreMap(), SetOptions.merge())
            .await()
    }

    override suspend fun linkReviewToDiaryEntry(
        userId: String,
        recipeId: String,
        reviewId: String,
        recipeTitle: String,
        recipeImageUri: String,
        cookedAt: Long
    ) {
        require(userId.isNotBlank()) {
            "A review link must belong to a user."
        }

        require(recipeId.isNotBlank()) {
            "A review link must belong to a recipe."
        }

        require(reviewId.isNotBlank()) {
            "A review link must reference a review."
        }

        val existingDocument = collection
            .whereEqualTo(TriedRecipeFirestore.USER_ID, userId)
            .whereEqualTo(TriedRecipeFirestore.RECIPE_ID, recipeId)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()

        val now = System.currentTimeMillis()

        if (existingDocument == null) {
            val newDocument = collection.document()

            val newEntry = TriedRecipeLog(
                id = newDocument.id,
                recipeId = recipeId,
                userId = userId,
                reviewId = reviewId,
                cookedAt = cookedAt,
                recipeTitle = recipeTitle,
                recipeImageUri = recipeImageUri,
                createdAt = now,
                updatedAt = now
            ).normalizedForWrite(updatedAt = now)

            newDocument
                .set(newEntry.toFirestoreMap())
                .await()
        } else {
            val existingEntry = existingDocument.toTriedRecipeLog()
                ?: TriedRecipeLog(
                    id = existingDocument.id,
                    recipeId = recipeId,
                    userId = userId,
                    cookedAt = cookedAt,
                    recipeTitle = recipeTitle,
                    recipeImageUri = recipeImageUri,
                    createdAt = now,
                    updatedAt = now
                )

            val linkedEntry = existingEntry.copy(
                reviewId = reviewId,
                recipeTitle = existingEntry.recipeTitle.ifBlank {
                    recipeTitle
                },
                recipeImageUri = existingEntry.recipeImageUri.ifBlank {
                    recipeImageUri
                },
                updatedAt = now
            ).normalizedForWrite(updatedAt = now)

            existingDocument.reference
                .set(
                    linkedEntry.toFirestoreMap(),
                    SetOptions.merge()
                )
                .await()
        }
    }

    override suspend fun deleteDiaryEntry(entryId: String) {
        collection
            .document(entryId)
            .delete()
            .await()
    }

    override suspend fun deleteDiaryEntriesByRecipe(recipeId: String) {
        val logs = collection
            .whereEqualTo(TriedRecipeFirestore.RECIPE_ID, recipeId)
            .get()
            .await()

        for (doc in logs.documents) {
            doc.reference.delete().await()
        }
    }

    private fun TriedRecipeLog.toFirestoreMap(): Map<String, Any?> = mapOf(
        TriedRecipeFirestore.ID to id,
        TriedRecipeFirestore.RECIPE_ID to recipeId,
        TriedRecipeFirestore.USER_ID to userId,
        TriedRecipeFirestore.REVIEW_ID to reviewId,
        TriedRecipeFirestore.COOKED_AT to cookedAt,
        TriedRecipeFirestore.RESULT to result.name,
        TriedRecipeFirestore.MODIFICATIONS to modifications,
        TriedRecipeFirestore.PERSONAL_NOTE to personalNote,
        TriedRecipeFirestore.FINAL_PHOTO_URI to finalPhotoUri,
        TriedRecipeFirestore.WOULD_COOK_AGAIN to wouldCookAgain,
        TriedRecipeFirestore.RECIPE_TITLE to recipeTitle,
        TriedRecipeFirestore.RECIPE_IMAGE_URI to recipeImageUri,
        TriedRecipeFirestore.CREATED_AT to createdAt,
        TriedRecipeFirestore.UPDATED_AT to updatedAt,
        TriedRecipeFirestore.NOTES to notes
    )

    private fun TriedRecipeLog.normalizedForWrite(updatedAt: Long): TriedRecipeLog =
        copy(
            modifications = modifications.trim(),
            personalNote = personalNote.trim().ifBlank { notes.orEmpty().trim() },
            finalPhotoUri = finalPhotoUri.trim(),
            recipeTitle = recipeTitle.trim(),
            recipeImageUri = recipeImageUri.trim(),
            updatedAt = updatedAt,
            notes = notes ?: personalNote.trim().takeIf { it.isNotBlank() }
        )

    private fun DocumentSnapshot.toTriedRecipeLog(): TriedRecipeLog? {
        if (!exists()) return null

        val recipeId = getString(TriedRecipeFirestore.RECIPE_ID).orEmpty()
        val userId = getString(TriedRecipeFirestore.USER_ID).orEmpty()

        if (recipeId.isBlank() || userId.isBlank()) return null

        val now = System.currentTimeMillis()
        val cookedAt = getLong(TriedRecipeFirestore.COOKED_AT) ?: now
        val notes = getString(TriedRecipeFirestore.NOTES)

        return TriedRecipeLog(
            id = getString(TriedRecipeFirestore.ID).orEmpty().ifBlank { id },
            recipeId = recipeId,
            userId = userId,
            reviewId = getString(TriedRecipeFirestore.REVIEW_ID)?.takeIf { it.isNotBlank() },
            cookedAt = cookedAt,
            result = readCookingResult(),
            modifications = getString(TriedRecipeFirestore.MODIFICATIONS).orEmpty(),
            personalNote = getString(TriedRecipeFirestore.PERSONAL_NOTE).orEmpty()
                .ifBlank { notes.orEmpty() },
            finalPhotoUri = getString(TriedRecipeFirestore.FINAL_PHOTO_URI).orEmpty(),
            wouldCookAgain = getBoolean(TriedRecipeFirestore.WOULD_COOK_AGAIN) ?: true,
            recipeTitle = getString(TriedRecipeFirestore.RECIPE_TITLE).orEmpty(),
            recipeImageUri = getString(TriedRecipeFirestore.RECIPE_IMAGE_URI).orEmpty(),
            createdAt = getLong(TriedRecipeFirestore.CREATED_AT) ?: cookedAt,
            updatedAt = getLong(TriedRecipeFirestore.UPDATED_AT) ?: cookedAt,
            notes = notes
        )
    }

    private fun DocumentSnapshot.readCookingResult(): CookingResult {
        val rawValue = getString(TriedRecipeFirestore.RESULT) ?: return CookingResult.GOOD

        return runCatching {
            CookingResult.valueOf(rawValue)
        }.getOrDefault(CookingResult.GOOD)
    }

    private fun validateTriedRecipeLog(log: TriedRecipeLog) {
        require(log.id.isNotBlank()) { "A diary entry must have an id." }
        require(log.recipeId.isNotBlank()) { "A diary entry must be linked to a recipe." }
        require(log.userId.isNotBlank()) { "A diary entry must be linked to a user." }
        require(log.cookedAt > 0L) { "A diary entry must have a valid cooked date." }
    }
}
