package it.polito.mad.cookbookcommunity.viewmodel

import it.polito.mad.cookbookcommunity.model.recipe.RecipeProposal
import it.polito.mad.cookbookcommunity.model.recipe.IngredientItem
import it.polito.mad.cookbookcommunity.model.recipe.InstructionStep

data class RecipeProposalDisplayState(
    val ownerId: String,
    val authorDisplayName: String,
    val title: String,
    val description: String,
    val cookingTimeText: String,
    val difficultyText: String,
    val priceRangeText: String,
    val servingsText: String,
    val caloriesText: String,
    val imageUri: String,
    val ingredients: List<IngredientItem>,
    val steps: List<InstructionStep>,
    val recipeTypeText: String?,
    val cuisineTypeText: String?,
    val dietaryRestrictions: List<String>,
    val isOwnerView: Boolean,
    val canEdit: Boolean,
    val canDelete: Boolean,
    val canSave: Boolean,
    val canDuplicate: Boolean,
    val canAddReview: Boolean,
    val showReviewsSection: Boolean,
    val showTipsSection: Boolean,

    val originalRecipeId: String?,
    val originalRecipeTitle: String,
    val originalRecipeImageUri: String,
    val originalAuthorId: String,
    val originalAuthorDisplayName: String,
    val originalAuthorPhotoUrl: String,
    val adaptationNote: String,
    val isOriginalRecipeAvailable: Boolean,
    val isOriginalRecipeLoading: Boolean,

    val showDeleteConfirm: Boolean = false
)
fun RecipeProposal.toDisplayState(
    isOwnerView: Boolean = true,
    showDeleteConfirm: Boolean = false,
    authorDisplayName: String? = null,
    originalRecipe: RecipeProposal? = null,
    isOriginalRecipeLoading: Boolean = false
): RecipeProposalDisplayState {
    val resolvedOriginalRecipeTitle = originalRecipe?.title
        ?.takeIf { it.isNotBlank() }
        ?: this.originalRecipeTitle

    val resolvedOriginalRecipeImageUri = originalRecipe?.imageUri
        ?.takeIf { it.isNotBlank() }
        ?: this.originalRecipeImageUri

    val resolvedOriginalAuthorId = originalRecipe?.ownerId
        ?.takeIf { it.isNotBlank() }
        ?: this.originalAuthorId

    val resolvedOriginalAuthorDisplayName = originalRecipe?.ownerDisplayName
        ?.takeIf { it.isNotBlank() }
        ?: this.originalAuthorDisplayName

    val resolvedOriginalAuthorPhotoUrl = originalRecipe?.ownerPhotoUrl
        ?.takeIf { it.isNotBlank() }
        ?: this.originalAuthorPhotoUrl

    return RecipeProposalDisplayState(
        ownerId = ownerId,
        authorDisplayName = authorDisplayName?.takeIf { it.isNotBlank() } ?: "User $ownerId",
        title = title,
        description = description,
        servingsText = "$servings ${if (servings == 1) "portion" else "portions"}",
        priceRangeText = priceRange.displayPriceRange(),
        difficultyText = difficulty.displayDifficulty(),
        cookingTimeText = cookTimeMinutes.formatMinutes(),
        caloriesText = calories.formatCalories(),
        imageUri = imageUri,
        ingredients = ingredients,
        steps = steps,
        recipeTypeText = recipeType.replace('_', ' ')
            .lowercase()
            .replaceFirstChar { it.uppercase() },
        cuisineTypeText = cuisineType?.replace('_', ' ')
            ?.lowercase()
            ?.replaceFirstChar { it.uppercase() },
        dietaryRestrictions = dietaryRestrictions.map { restriction ->
            restriction.replace('_', ' ')
                .lowercase()
                .replaceFirstChar { it.uppercase() }
        },
        isOwnerView = isOwnerView,
        canEdit = isOwnerView,
        canDelete = isOwnerView,
        canSave = !isOwnerView,
        canDuplicate = !isOwnerView,
        canAddReview = !isOwnerView,
        showReviewsSection = true,
        showTipsSection = true,
        originalRecipeId = this.originalRecipeId,
        originalRecipeTitle = resolvedOriginalRecipeTitle,
        originalRecipeImageUri = resolvedOriginalRecipeImageUri,
        originalAuthorId = resolvedOriginalAuthorId,
        originalAuthorDisplayName = resolvedOriginalAuthorDisplayName,
        originalAuthorPhotoUrl = resolvedOriginalAuthorPhotoUrl,
        adaptationNote = adaptationNote,
        isOriginalRecipeAvailable = originalRecipe != null,
        isOriginalRecipeLoading = isOriginalRecipeLoading,
        showDeleteConfirm = showDeleteConfirm
    )
}

private fun String.displayDifficulty(): String = when (this) {
    "EASY" -> "Easy"
    "MEDIUM" -> "Medium"
    "HARD" -> "Hard"
    else -> this
}

private fun String.displayPriceRange(): String = when (this) {
    "LOW" -> "€"
    "MEDIUM" -> "€€"
    "HIGH" -> "€€€"
    else -> this
}

private fun Int.formatMinutes(): String {
    if (this <= 0) return "—"
    val h = this / 60
    val m = this % 60
    return when {
        h == 0 -> "$m min"
        m == 0 -> "${h}h"
        else   -> "${h}h ${m}min"
    }
}

private fun Int?.formatCalories(): String {
    return this?.takeIf { it > 0 }?.let { "$it kcal" } ?: "—"
}
