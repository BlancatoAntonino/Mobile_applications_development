package it.polito.mad.cookbookcommunity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.polito.mad.cookbookcommunity.data.recipe.RecipeRepository
import it.polito.mad.cookbookcommunity.data.review.ReviewRepository
import it.polito.mad.cookbookcommunity.data.repository.TriedRecipeRepository
import it.polito.mad.cookbookcommunity.data.user.UserRepository
import it.polito.mad.cookbookcommunity.model.review.CookingResult
import it.polito.mad.cookbookcommunity.model.review.Review
import it.polito.mad.cookbookcommunity.model.review.TriedRecipeLog
import it.polito.mad.cookbookcommunity.session.SessionManager
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val REVIEW_PHOTO_ASSET_URIS = listOf(
    "file:///android_asset/pasta.jpg",
    "file:///android_asset/pancakes.jpg",
    "file:///android_asset/curry.jpg",
    "file:///android_asset/lasagna.jpg",
    "file:///android_asset/greek_salad.jpg",
    "file:///android_asset/brownies.jpg",
    "file:///android_asset/hummus.jpg",
    "file:///android_asset/avocado_toast.jpg"
)

data class AddReviewUiState(
    val recipeId: String,
    val recipeTitle: String = "Recipe",
    val recipeImageUri: String = "",
    val isLoadingRecipe: Boolean = true,
    val recipeExists: Boolean = false,
    val isOwnRecipe: Boolean = false,
    val editingReviewId: String? = null,
    val existingReviewCreatedAt: Long? = null,
    val rating: Int = 0,
    val reviewTitle: String = "",
    val reviewText: String = "",
    val photoUri: String = "",
    val isPublishing: Boolean = false,
    val errorMessage: String? = null,
    val isPublished: Boolean = false
) {
    val isEditing: Boolean
        get() = editingReviewId != null

    val reviewBlockedMessage: String?
        get() = when {
            isLoadingRecipe -> null
            !recipeExists -> "Recipe not found. You cannot publish a review."
            isOwnRecipe -> "You cannot review your own recipe."
            else -> null
        }

    val canReviewRecipe: Boolean
        get() = reviewBlockedMessage == null && !isLoadingRecipe

    val canPublish: Boolean
        get() = canReviewRecipe &&
                rating in 1..5 &&
                reviewTitle.isNotBlank() &&
                reviewText.isNotBlank() &&
                !isPublishing
}

class AddReviewViewModel(
    private val recipeId: String,
    private val initialReviewId: String?,
    private val recipeRepository: RecipeRepository,
    private val reviewRepository: ReviewRepository,
    private val triedRecipeRepository: TriedRecipeRepository,
    private val userProfileRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddReviewUiState(recipeId = recipeId))
    val uiState: StateFlow<AddReviewUiState> = _uiState.asStateFlow()

    private var hasPrefilledExistingReview = false

    init {
        observeRecipeSummary()
        observeExistingReviewIfPresent()
    }

    fun updateRating(rating: Int) {
        _uiState.update { current ->
            current.copy(
                rating = rating.coerceIn(0, 5),
                errorMessage = null
            )
        }
    }

    fun updateReviewTitle(title: String) {
        _uiState.update { current ->
            current.copy(
                reviewTitle = title,
                errorMessage = null
            )
        }
    }

    fun updateReviewText(text: String) {
        _uiState.update { current ->
            current.copy(
                reviewText = text,
                errorMessage = null
            )
        }
    }

    fun selectNextPhotoAsset() {
        _uiState.update { current ->
            if (!current.canReviewRecipe) {
                current
            } else {
                val currentIndex = REVIEW_PHOTO_ASSET_URIS.indexOf(current.photoUri)
                val nextIndex =
                    if (currentIndex == -1) 0
                    else (currentIndex + 1) % REVIEW_PHOTO_ASSET_URIS.size

                current.copy(photoUri = REVIEW_PHOTO_ASSET_URIS[nextIndex])
            }
        }
    }

    fun publishReview() {
        val current = _uiState.value

        val currentUserId = SessionManager.authenticatedUserIdOrNull()

        if (currentUserId == null) {
            _uiState.update {
                it.copy(errorMessage = "Sign in to publish a review.")
            }
            return
        }

        current.reviewBlockedMessage?.let { message ->
            _uiState.update {
                it.copy(errorMessage = message)
            }
            return
        }

        if (current.rating !in 1..5) {
            _uiState.update {
                it.copy(errorMessage = "Select a rating from 1 to 5 stars.")
            }
            return
        }

        if (current.reviewTitle.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Write a short title before saving.")
            }
            return
        }

        if (current.reviewText.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Write a short review before saving.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isPublishing = true, errorMessage = null)
            }

            try {
                val now = System.currentTimeMillis()
                val existingReviewId = current.editingReviewId

                val currentUser = SessionManager.currentUser.value
                val currentUserProfile = userProfileRepository.getUserById(currentUserId)

                val authorDisplayName =
                    currentUserProfile?.nickname?.trim()?.takeIf { it.isNotBlank() }
                        ?: currentUserProfile?.fullname?.trim()?.takeIf { it.isNotBlank() }
                        ?: currentUser?.displayName?.trim()?.takeIf { it.isNotBlank() }
                        ?: currentUser?.email?.substringBefore("@")?.trim()?.takeIf { it.isNotBlank() }
                        ?: "User"

                val authorPhotoUrl =
                    currentUserProfile?.profileImage?.value?.takeIf { it.isNotBlank() }
                        ?: currentUser?.photoUrl.orEmpty()

                val review = Review(
                    id = existingReviewId ?: UUID.randomUUID().toString(),
                    recipeId = recipeId,
                    recipeTitle = current.recipeTitle,
                    recipeOwnerId = "unknown",
                    authorId = currentUserId,
                    authorDisplayName = authorDisplayName,
                    authorPhotoUrl = authorPhotoUrl,
                    rating = current.rating,
                    title = current.reviewTitle.trim(),
                    text = current.reviewText.trim(),
                    photoUri = current.photoUri.trim().takeIf { it.isNotBlank() },
                    createdAt = current.existingReviewCreatedAt ?: now,
                    updatedAt = now
                )

                if (existingReviewId == null) {
                    reviewRepository.addReview(review)
                } else {
                    reviewRepository.updateReview(review)
                }

                runCatching {
                    triedRecipeRepository.linkReviewToDiaryEntry(
                        userId = currentUserId,
                        recipeId = recipeId,
                        reviewId = review.id,
                        recipeTitle = current.recipeTitle,
                        recipeImageUri = current.recipeImageUri,
                        cookedAt = review.createdAt
                    )
                }

                _uiState.update {
                    it.copy(
                        isPublishing = false,
                        isPublished = true
                    )
                }
            } catch (error: Throwable) {
                _uiState.update {
                    it.copy(
                        isPublishing = false,
                        errorMessage = error.message ?: "Unable to save the review."
                    )
                }
            }
        }
    }

    private fun observeRecipeSummary() {
        viewModelScope.launch {
            recipeRepository.getRecipeProposalById(recipeId).collect { recipe ->
                _uiState.update { current ->
                    if (recipe == null) {
                        current.copy(
                            recipeTitle = "Recipe not found",
                            recipeImageUri = "",
                            isLoadingRecipe = false,
                            recipeExists = false,
                            isOwnRecipe = false
                        )
                    } else {
                        current.copy(
                            recipeTitle = recipe.title,
                            recipeImageUri = recipe.imageUri,
                            isLoadingRecipe = false,
                            recipeExists = true,
                            isOwnRecipe = recipe.ownerId == SessionManager.authenticatedUserIdOrNull()
                        )
                    }
                }
            }
        }
    }

    private fun observeExistingReviewIfPresent() {
        viewModelScope.launch {
            reviewRepository.getReviewsByRecipe(recipeId).collect { reviews ->
                if (hasPrefilledExistingReview) return@collect

                val currentUserId = SessionManager.authenticatedUserIdOrNull()
                    ?: return@collect
                val reviewToEdit = if (initialReviewId != null) {
                    reviews.firstOrNull { review ->
                        review.id == initialReviewId && review.authorId == currentUserId
                    }
                } else {
                    reviews.firstOrNull { review ->
                        review.authorId == currentUserId
                    }
                }

                if (reviewToEdit != null) {
                    hasPrefilledExistingReview = true
                    _uiState.update { current ->
                        current.copy(
                            editingReviewId = reviewToEdit.id,
                            existingReviewCreatedAt = reviewToEdit.createdAt,
                            rating = reviewToEdit.rating,
                            reviewTitle = reviewToEdit.title,
                            reviewText = reviewToEdit.text,
                            photoUri = reviewToEdit.photoUri.orEmpty(),
                            errorMessage = null
                        )
                    }
                }
            }
        }
    }
}