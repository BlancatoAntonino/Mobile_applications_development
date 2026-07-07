package it.polito.mad.cookbookcommunity.data.recipe

import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import it.polito.mad.cookbookcommunity.data.firestore.FirestoreCollections
import it.polito.mad.cookbookcommunity.data.firestore.FirestoreCollections.Recipe
import it.polito.mad.cookbookcommunity.data.notification.FirestoreNotificationRepository
import it.polito.mad.cookbookcommunity.data.notification.NotificationRepository
import it.polito.mad.cookbookcommunity.model.notification.AppNotification
import it.polito.mad.cookbookcommunity.model.notification.NotificationType
import it.polito.mad.cookbookcommunity.model.recipe.IngredientItem
import it.polito.mad.cookbookcommunity.model.recipe.InstructionStep
import it.polito.mad.cookbookcommunity.model.recipe.RecipeProposal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreRecipeRepository(
    private val notificationRepository: NotificationRepository = FirestoreNotificationRepository()
) : RecipeRepository {
    private val db = Firebase.firestore

    private val collection
        get() = db.collection(FirestoreCollections.RECIPES)

    private val usersCollection
        get() = db.collection(FirestoreCollections.USERS)

    override fun getAllRecipeProposals(): Flow<List<RecipeProposal>> =
        collection
            .orderBy(FirestoreCollections.Recipe.CREATED_AT, Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot -> snapshot.toObjects<RecipeProposal>() }

    override fun getOwnedRecipeProposals(ownerId: String): Flow<List<RecipeProposal>> =
        collection
            .whereEqualTo(Recipe.OWNER_ID, ownerId)
            .snapshots()
            .map { snapshot ->
                snapshot
                    .toObjects<RecipeProposal>()
                    .sortedByDescending { recipe -> recipe.updatedAt }
            }

    override fun getRecipeProposalById(recipeId: String): Flow<RecipeProposal?> =
        collection
            .document(recipeId)
            .snapshots()
            .map { snapshot -> snapshot.toObject<RecipeProposal>() }

    override suspend fun addRecipeProposal(recipe: RecipeProposal) {
        collection
            .document(recipe.id)
            .set(recipe.toFirestoreMap())
            .await()

        val duplicateRecipientId = createDuplicatedRecipeNotification(recipe)
        createRecommendedRecipeNotifications(
            recipe = recipe,
            excludedUserIds = setOfNotNull(recipe.ownerId, duplicateRecipientId)
        )
    }

    override suspend fun updateRecipeProposal(recipe: RecipeProposal) {
        val updated = recipe.copy(updatedAt = System.currentTimeMillis())
        collection
            .document(updated.id)
            .set(updated.toFirestoreMap())
            .await()
    }

    override suspend fun deleteRecipeProposal(recipeId: String) {
        collection
            .document(recipeId)
            .delete()
            .await()
    }

    private fun RecipeProposal.toFirestoreMap(): Map<String, Any?> = mapOf(
        Recipe.ID to id,
        Recipe.OWNER_ID to ownerId,
        Recipe.OWNER_DISPLAY_NAME to ownerDisplayName,
        Recipe.OWNER_PHOTO_URL to ownerPhotoUrl,
        Recipe.TITLE to title,
        Recipe.DESCRIPTION to description,
        Recipe.IMAGE_URI to imageUri,
        Recipe.SERVINGS to servings,
        Recipe.PRICE_RANGE to priceRange,
        Recipe.DIFFICULTY to difficulty,
        Recipe.COOK_TIME_MINUTES to cookTimeMinutes,
        Recipe.CALORIES to calories,
        Recipe.RECIPE_TYPE to recipeType,
        Recipe.CUISINE_TYPE to cuisineType,
        Recipe.DIETARY_RESTRICTION to dietaryRestrictions,
        Recipe.INGREDIENTS to ingredients.map { it.toMap() },
        Recipe.STEPS to steps.map { it.toMap() },

        Recipe.ORIGINAL_RECIPE_ID to originalRecipeId,
        Recipe.ORIGINAL_RECIPE_TITLE to originalRecipeTitle,
        Recipe.ORIGINAL_RECIPE_IMAGE_URI to originalRecipeImageUri,
        Recipe.ORIGINAL_AUTHOR_ID to originalAuthorId,
        Recipe.ORIGINAL_AUTHOR_DISPLAY_NAME to originalAuthorDisplayName,
        Recipe.ORIGINAL_AUTHOR_PHOTO_URL to originalAuthorPhotoUrl,
        Recipe.ADAPTATION_NOTE to adaptationNote,

        Recipe.CREATED_AT to createdAt,
        Recipe.UPDATED_AT to updatedAt,
        Recipe.AVERAGE_RATING to averageRating,
        Recipe.REVIEW_COUNT to reviewCount
    )

    private fun IngredientItem.toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "name" to name,
        "quantity" to quantity,
        "unit" to unit,
        "note" to note,
        "optional" to optional,
    )

    private fun InstructionStep.toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "stepNumber" to stepNumber,
        "text" to text,
    )

    private suspend fun createDuplicatedRecipeNotification(recipe: RecipeProposal): String? {
        val sourceRecipeId = recipe.originalRecipeId ?: return null
        val sourceRecipe = collection.document(sourceRecipeId).get().await()
        val recipientUserId = sourceRecipe.getString(Recipe.OWNER_ID)?.takeIf { it.isNotBlank() }
            ?: return null

        if (recipientUserId == recipe.ownerId) return null

        val actorName = recipe.ownerDisplayName.ifBlank { "Someone" }
        val sourceTitle = sourceRecipe.getString(Recipe.TITLE).orEmpty().ifBlank { "your recipe" }

        notificationRepository.createNotification(
            AppNotification(
                recipientUserId = recipientUserId,
                actorUserId = recipe.ownerId,
                actorDisplayName = actorName,
                type = NotificationType.RECIPE_DUPLICATED,
                title = "Recipe adapted",
                message = "$actorName adapted $sourceTitle.",
                recipeId = recipe.id
            )
        )

        return recipientUserId
    }

    private suspend fun createRecommendedRecipeNotifications(
        recipe: RecipeProposal,
        excludedUserIds: Set<String>
    ) {
        val users = usersCollection.get().await()
        val actorName = recipe.ownerDisplayName.ifBlank { "Someone" }

        users.documents
            .filter { document ->
                val userId = document.getString(FirestoreCollections.User.ID).orEmpty()
                    .ifBlank { document.id }

                userId !in excludedUserIds && document.matchesRecipePreferences(recipe)
            }
            .forEach { document ->
                val recipientUserId = document.getString(FirestoreCollections.User.ID).orEmpty()
                    .ifBlank { document.id }

                notificationRepository.createNotification(
                    AppNotification(
                        recipientUserId = recipientUserId,
                        actorUserId = recipe.ownerId,
                        actorDisplayName = actorName,
                        type = NotificationType.RECOMMENDED_RECIPE,
                        title = "Recipe you might like",
                        message = "$actorName published ${recipe.title}.",
                        recipeId = recipe.id
                    )
                )
            }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.matchesRecipePreferences(
        recipe: RecipeProposal
    ): Boolean {
        val favoriteCuisines = getStringList(FirestoreCollections.User.FAVORITE_CUISINES)
        val dietaryRestrictions = getStringList(FirestoreCollections.User.DIETARY_RESTRICTIONS)
        val preferredRecipeTypes = getStringList("preferredRecipeTypes")

        val matchesDietaryRestriction =
            recipe.dietaryRestrictions.any { restriction -> restriction in dietaryRestrictions }

        val matchesRecipeType = recipe.recipeType in preferredRecipeTypes

        val matchesCuisine =
            recipe.cuisineType?.let { cuisine -> cuisine in favoriteCuisines } == true

        return matchesDietaryRestriction || matchesRecipeType || matchesCuisine
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.getStringList(field: String): List<String> {
        return (get(field) as? List<*>)
            ?.mapNotNull { value -> value as? String }
            ?: emptyList()
    }
}
