package it.polito.mad.cookbookcommunity.navigation

import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
object OwnProfileRoute

@Serializable
data class OtherProfileRoute(
    val userId: String
)

@Serializable
object RecipesListRoute

@Serializable
data class RecipeDetailRoute(
    val recipeId: String,
    val fromList: Boolean = false
)

@Serializable
data class RecipeListOwnedRoute(
    val ownerId: String,
    val isOwner: Boolean,
    val ownerName: String
)

@Serializable
object CreateRecipeRoute

@Serializable
data class AdaptRecipeRoute(
    val sourceRecipeId: String
)

@Serializable
object NotificationsRoute

@Serializable
object AuthGraphRoute

@Serializable
object SignUpAccountRoute

@Serializable
object AuthEntryRoute

@Serializable
object AuthEntryPhotoRoleRoute

@Serializable
object AuthEntryPreferencesRoute

@Serializable
data class TriedRecipesRoute(
    val userId: String
)

@Serializable
data class DiaryRoute(
    val userId: String
)

@Serializable
data class AddDiaryEntryRoute(
    val recipeId: String,
    val entryId: String? = null
)

@Serializable
object SavedCollectionsRoute

@Serializable
data class CollectionDetailNavRoute(
    val collectionId: String,
    val collectionName: String
)

@Serializable
data class SaveToCollectionDialogRoute(
    val recipeId: String
)

@Serializable
data class AddReviewRoute(
    val recipeId: String,
    val reviewId: String? = null
)

@Serializable
data class AddTipRoute(
    val recipeId: String
)

@Serializable
data class RecipeReviewsNavRoute(
    val recipeId: String
)
