package it.polito.mad.cookbookcommunity.ui.recipeProposal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import it.polito.mad.cookbookcommunity.data.recipe.RecipeRepository
import it.polito.mad.cookbookcommunity.data.review.ReviewRepository
import it.polito.mad.cookbookcommunity.data.repository.TipRepository
import it.polito.mad.cookbookcommunity.data.repository.TriedRecipeRepository
import it.polito.mad.cookbookcommunity.data.user.UserRepository
import it.polito.mad.cookbookcommunity.domain.usecase.DeleteRecipeProposalUseCase
import it.polito.mad.cookbookcommunity.model.review.Review
import it.polito.mad.cookbookcommunity.model.review.Tip
import it.polito.mad.cookbookcommunity.session.SessionManager
import it.polito.mad.cookbookcommunity.ui.dialogs.SuccessDialog
import it.polito.mad.cookbookcommunity.viewmodel.RecipeEditorViewModel
import it.polito.mad.cookbookcommunity.viewmodel.RecipeProposalUiEvent
import it.polito.mad.cookbookcommunity.viewmodel.toDisplayState
import kotlinx.coroutines.flow.flowOf

@Composable
fun RecipeProposalRoute(
    recipeId: String,
    repository: RecipeRepository,
    userProfileRepository: UserRepository,
    reviewRepository: ReviewRepository,
    tipRepository: TipRepository,
    triedRecipeRepository: TriedRecipeRepository,
    deleteRecipeProposalUseCase: DeleteRecipeProposalUseCase,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    openInEditMode: Boolean = false,
    onOpenInEditModeConsumed: () -> Unit = {},
    onDuplicateClick: (String) -> Unit = {},
    onAuthorProfileClick: (String) -> Unit = {},
    onOriginalRecipeClick: (String) -> Unit = {},
    onOriginalAuthorClick: (String) -> Unit = {},
    onSaveToCollectionClick: (recipeId: String) -> Unit = {},
    onAddReviewClick: (recipeId: String, reviewId: String?) -> Unit = { _, _ -> },
    onAddTipClick: (recipeId: String) -> Unit = {},
    onAddDiaryClick: (recipeId: String, entryId: String?) -> Unit = { _, _ -> },
    onSeeAllReviewsClick: (recipeId: String) -> Unit = {}
) {
    val viewModel: RecipeEditorViewModel = viewModel(
        key = "recipe_$recipeId",
        factory = viewModelFactory {
            initializer {
                RecipeEditorViewModel(
                    recipeRepository = repository,
                    userProfileRepository = userProfileRepository,
                    deleteRecipeProposalUseCase = deleteRecipeProposalUseCase
                )
            }
        }
    )

    LaunchedEffect(recipeId) {
        viewModel.onEvent(RecipeProposalUiEvent.LoadRecipe(recipeId))
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val communityReviews by reviewRepository
        .getReviewsByRecipe(recipeId)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val communityTips by tipRepository
        .getTipsByRecipe(recipeId)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val authenticatedUserId = SessionManager.authenticatedUserIdOrNull()
    val authenticatedUser = SessionManager.currentUser.value

    val diaryEntryFlow = remember(authenticatedUserId, recipeId) {
        authenticatedUserId?.let { userId ->
            triedRecipeRepository.getDiaryEntryForRecipe(
                userId = userId,
                recipeId = recipeId
            )
        } ?: flowOf(null)
    }

    val existingDiaryEntry by diaryEntryFlow.collectAsStateWithLifecycle(
        initialValue = null
    )

    val authorIds = remember(communityReviews, communityTips) {
        (communityReviews.map { review -> review.authorId } +
                communityTips.map { tip -> tip.authorId })
            .filter { authorId -> authorId.isNotBlank() }
            .distinct()
    }

    var authorNicknames by remember {
        mutableStateOf<Map<String, String>>(emptyMap())
    }

    LaunchedEffect(authorIds, authenticatedUserId) {
        authorNicknames = authorIds.associateWith { authorId ->
            val profile = userProfileRepository.getUserById(authorId)

            profile?.nickname?.trim()?.takeIf { it.isNotBlank() }
                ?: profile?.fullname?.trim()?.takeIf { it.isNotBlank() }
                ?: if (authorId == authenticatedUserId) {
                    authenticatedUser?.displayName?.trim()?.takeIf { it.isNotBlank() }
                        ?: authenticatedUser?.email
                            ?.substringBefore("@")
                            ?.trim()
                            ?.takeIf { it.isNotBlank() }
                } else {
                    null
                }
                ?: "User $authorId"
        }
    }

    val currentUserReview = authenticatedUserId?.let { currentUserId ->
        communityReviews.firstOrNull { review ->
            review.authorId == currentUserId
        }
    }

    LaunchedEffect(
        openInEditMode,
        uiState.recipe?.id,
        uiState.isEditing
    ) {
        if (
            openInEditMode &&
            uiState.recipe?.id == recipeId &&
            !uiState.isEditing
        ) {
            viewModel.onEvent(RecipeProposalUiEvent.EditRequested)
            onOpenInEditModeConsumed()
        }
    }

    LaunchedEffect(uiState.navigateToSaveCollection) {
        if (uiState.navigateToSaveCollection) {
            val id = uiState.recipe?.id ?: return@LaunchedEffect
            onSaveToCollectionClick(id)
            viewModel.onEvent(RecipeProposalUiEvent.SaveCollectionNavigationConsumed)
        }
    }

    LaunchedEffect(uiState.navigateBack) {
        if (uiState.navigateBack) {
            onBackClick()
        }
    }

    val successMessage = uiState.successMessage

    if (successMessage != null) {
        SuccessDialog(
            title = "Operation successful",
            message = successMessage,
            onDismiss = {
                viewModel.onEvent(RecipeProposalUiEvent.DismissSuccessMessage)
            }
        )
    }

    if (uiState.showDiscardEditConfirm) {
        AlertDialog(
            onDismissRequest = {
                viewModel.onEvent(RecipeProposalUiEvent.DiscardEditDismissed)
            },
            title = {
                Text("Discard changes?")
            },
            text = {
                Text("You have unsaved changes. If you leave now, your edits will be lost.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(RecipeProposalUiEvent.DiscardEditConfirmed)
                    }
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(RecipeProposalUiEvent.DiscardEditDismissed)
                    }
                ) {
                    Text("Keep editing")
                }
            }
        )
    }

    BackHandler {
        when {
            uiState.showDeleteConfirm -> {
                viewModel.onEvent(RecipeProposalUiEvent.DeleteDismissed)
            }

            uiState.showDiscardEditConfirm -> {
                viewModel.onEvent(RecipeProposalUiEvent.DiscardEditDismissed)
            }

            uiState.isEditing -> {
                viewModel.onEvent(RecipeProposalUiEvent.CancelEditRequested)
            }

            else -> {
                onBackClick()
            }
        }
    }

    val recipe = uiState.recipe

    when {
        recipe == null -> {
            RecipeProposalRoutePlaceholder(
                title = "Loading...",
                body="Loading recipe...",
                modifier = modifier
            )
        }

        uiState.isEditing -> {
            val editState = uiState.editState

            if (editState == null) {
                RecipeProposalRoutePlaceholder(
                    title = "Edit state missing",
                    body = "The route entered EDIT mode without an editState.",
                    modifier = modifier
                )
            } else {
                EditProposalScreen(
                    uiState = editState,
                    onEvent = viewModel::onEvent,
                    modifier = modifier
                )
            }
        }

        else -> {
            RecipeProposalScreen(
                uiState = recipe.toDisplayState(
                    isOwnerView = uiState.isOwner,
                    showDeleteConfirm = uiState.showDeleteConfirm,
                    authorDisplayName = uiState.authorDisplayName,
                    originalRecipe = uiState.originalRecipe,
                    isOriginalRecipeLoading = uiState.isOriginalRecipeLoading
                ),
                onEvent = viewModel::onEvent,
                onBackClick = onBackClick,
                modifier = modifier,
                onAuthorProfileClick = onAuthorProfileClick,
                onOriginalRecipeClick = onOriginalRecipeClick,
                onOriginalAuthorClick = onOriginalAuthorClick,
                onSeeAllReviewsClick = {
                    onSeeAllReviewsClick(recipe.id)
                },
                onDuplicateClick = {
                    onDuplicateClick(recipe.id)
                },
                diaryActionLabel = if (existingDiaryEntry == null) {
                    "Add to diary"
                } else {
                    "Edit diary note"
                },
                onAddDiaryClick = {
                    onAddDiaryClick(
                        recipe.id,
                        existingDiaryEntry?.id
                    )
                },
                reviewPreviews = communityReviews
                    .sortedWith(
                        compareByDescending<Review> { review ->
                            authenticatedUserId != null && review.authorId == authenticatedUserId
                        }.thenByDescending { review ->
                            review.updatedAt
                        }
                    )
                    .take(3)
                    .map { review ->
                        review.toPreviewUiState(
                            authorLabel = review.authorDisplayName.ifBlank {
                                authorNicknames[review.authorId]
                                    ?: "User ${review.authorId}"
                            },
                            isCurrentUserReview = authenticatedUserId != null &&
                                    review.authorId == authenticatedUserId
                        )
                    },
                tipPreviews = communityTips
                    .map { tip ->
                        tip.toTipPreviewUiState(
                            authorLabel = authorNicknames[tip.authorId]
                                ?.takeIf { it.isNotBlank() }
                                ?: "User ${tip.authorId}"
                        )
                    },
                reviewActionLabel = if (currentUserReview == null) {
                    "Add review"
                } else {
                    "Edit review"
                },
                onAddReviewClick = {
                    onAddReviewClick(recipe.id, currentUserReview?.id)
                },
                onAddTipClick = {
                    onAddTipClick(recipe.id)
                }
            )
        }
    }
}

private fun Review.toPreviewUiState(
    authorLabel: String,
    isCurrentUserReview: Boolean
): ReviewPreviewUiState {
    return ReviewPreviewUiState(
        id = id,
        authorLabel = authorLabel.toNicknameLabel(),
        rating = rating.coerceIn(0, 5),
        title = title.ifBlank { text.toReviewPreviewTitle() },
        text = text.toReviewPreviewBody(),
        createdAt = createdAt,
        updatedAt = updatedAt,
        isCurrentUserReview = isCurrentUserReview
    )
}

private fun Tip.toTipPreviewUiState(
    authorLabel: String
): TipPreviewUiState {
    return TipPreviewUiState(
        id = id,
        authorLabel = authorLabel.toNicknameLabel(),
        text = text.toTipPreviewBody()
    )
}

private fun String.toNicknameLabel(): String {
    val cleanLabel = trim().removePrefix("@")
    return if (cleanLabel.isBlank()) "@unknown" else "@$cleanLabel"
}

private fun String.toReviewPreviewTitle(): String {
    val compact = trim()
        .lineSequence()
        .firstOrNull { it.isNotBlank() }
        ?.trim()
        .orEmpty()

    if (compact.isBlank()) return "Community review"

    val sentenceEnd = compact.indexOfAny(charArrayOf('.', '!', '?'))

    val rawTitle = if (sentenceEnd in 1..55) {
        compact.take(sentenceEnd + 1)
    } else {
        compact.take(48)
    }

    return if (rawTitle.length < compact.length && !rawTitle.endsWith(".")) {
        "$rawTitle..."
    } else {
        rawTitle
    }
}

private fun String.toReviewPreviewBody(): String {
    val compact = trim().replace(Regex("\\s+"), " ")
    return if (compact.length <= 110) {
        compact
    } else {
        "${compact.take(107)}..."
    }
}

private fun String.toTipPreviewBody(): String {
    val compact = trim().replace(Regex("\\s+"), " ")
    return if (compact.length <= 120) {
        compact
    } else {
        "${compact.take(117)}..."
    }
}

@Composable
private fun RecipeProposalRoutePlaceholder(
    title: String,
    body: String,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.headlineSmall)
            Text(text = body, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
