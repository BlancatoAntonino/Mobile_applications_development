package it.polito.mad.cookbookcommunity.ui.recipeProposal

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.polito.mad.cookbookcommunity.model.recipe.IngredientItem
import it.polito.mad.cookbookcommunity.model.recipe.IngredientUnit
import it.polito.mad.cookbookcommunity.model.recipe.InstructionStep
import it.polito.mad.cookbookcommunity.ui.components.recipeproposal.RecipeDerivationCard
import it.polito.mad.cookbookcommunity.ui.components.recipeproposal.RecipeImageContent
import it.polito.mad.cookbookcommunity.viewmodel.RecipeProposalDisplayState
import it.polito.mad.cookbookcommunity.viewmodel.RecipeProposalUiEvent

@Composable
fun RecipeProposalScreen(
    uiState: RecipeProposalDisplayState,
    onEvent: (RecipeProposalUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onAuthorProfileClick: (String) -> Unit = {},
    onOriginalRecipeClick: (String) -> Unit = {},
    onOriginalAuthorClick: (String) -> Unit = {},
    onSeeAllReviewsClick: () -> Unit = {},
    reviewPreviews: List<ReviewPreviewUiState> = emptyList(),
    tipPreviews: List<TipPreviewUiState> = emptyList(),
    reviewActionLabel: String = "Add review",
    onAddReviewClick: () -> Unit = {},
    onAddTipClick: () -> Unit = {},
    onDuplicateClick: () -> Unit = {},
    diaryActionLabel: String = "Add to diary",
    onAddDiaryClick: () -> Unit = {}
) {
    if (uiState.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { onEvent(RecipeProposalUiEvent.DeleteDismissed) },
            title = { Text("Delete recipe") },
            text = { Text("Are you sure you want to delete this recipe? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { onEvent(RecipeProposalUiEvent.DeleteConfirmed) }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(RecipeProposalUiEvent.DeleteDismissed) }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            RecipeNavigationRow(onBackClick = onBackClick)
        }

        item {
            RecipeImageSection(imageUri = uiState.imageUri)
        }

        item {
            RecipeTitleSection(
                title = uiState.title,
                description = uiState.description
            )
        }

        item {
            RecipeAuthorSection(
                ownerId = uiState.ownerId,
                authorDisplayName = uiState.authorDisplayName,
                onAuthorProfileClick = onAuthorProfileClick
            )
        }

        if (!uiState.originalRecipeId.isNullOrBlank()) {
            item {
                RecipeDerivationCard(
                    originalRecipeTitle = uiState.originalRecipeTitle,
                    originalRecipeUri = uiState.originalRecipeImageUri,
                    originalAuthorName = uiState.originalAuthorDisplayName,
                    originalAuthorPhotoUrl = uiState.originalAuthorPhotoUrl,
                    adaptationNote = uiState.adaptationNote,
                    isOriginalRecipeAvailable = uiState.isOriginalRecipeAvailable,
                    isOriginalRecipeLoading = uiState.isOriginalRecipeLoading,
                    isOriginalAuthorAvailable = uiState.originalAuthorId.isNotBlank(),
                    onOriginalRecipeClick = {
                        uiState.originalRecipeId
                            ?.takeIf { it.isNotBlank() }
                            ?.let(onOriginalRecipeClick)
                    },
                    onOriginalAuthorClick = {
                        uiState.originalAuthorId
                            .takeIf { it.isNotBlank() }
                            ?.let(onOriginalAuthorClick)
                    }
                )
            }
        }

        item {
            RecipeInfoRow(uiState = uiState)
        }

        item {
            RecipeActionsRow(
                canEdit = uiState.canEdit,
                canDelete = uiState.canDelete,
                canSave = uiState.canSave,
                canDuplicate = uiState.canDuplicate,
                onEditClick = { onEvent(RecipeProposalUiEvent.EditRequested) },
                onDeleteClick = { onEvent(RecipeProposalUiEvent.DeleteRequested) },
                onSaveClick = { onEvent(RecipeProposalUiEvent.SaveRequested) },
                onDuplicateClick = onDuplicateClick
            )
        }

        item {
            DiaryActionButton(
                label = diaryActionLabel,
                onClick = onAddDiaryClick
            )
        }

        item {
            IngredientsSection(ingredients = uiState.ingredients)
        }

        item {
            StepsSection(steps = uiState.steps)
        }

        if (uiState.showReviewsSection) {
            item {
                CommunityReviewsSection(
                    reviewPreviews = reviewPreviews,
                    isOwnerView = uiState.isOwnerView,
                    reviewActionLabel = reviewActionLabel,
                    onAddReviewClick = onAddReviewClick,
                    onOpenReviewsClick = onSeeAllReviewsClick
                )
            }
        }

        if (uiState.showTipsSection) {
            item {
                CommunityTipsSection(
                    tipPreviews = tipPreviews,
                    isOwnerView = uiState.isOwnerView,
                    onAddTipClick = onAddTipClick
                )
            }
        }
    }
}

@Composable
private fun RecipeNavigationRow(
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }
    }
}

@Composable
private fun RecipeImageSection(
    imageUri: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        RecipeImageContent(
            imageUri = imageUri,
            contentDescription = "Recipe cover image",
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun RecipeTitleSection(
    title: String,
    description: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        if (description.isNotBlank()) {
            Text(
                text = description,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RecipeAuthorSection(
    ownerId: String,
    authorDisplayName: String,
    onAuthorProfileClick: (String) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Author",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = authorDisplayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            OutlinedButton(
                onClick = { onAuthorProfileClick(ownerId) }
            ) {
                Text("View profile")
            }
        }
    }
}

@Composable
private fun RecipeInfoRow(uiState: RecipeProposalDisplayState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InfoCard(
                modifier = Modifier.weight(1f),
                label = "Time",
                value = uiState.cookingTimeText,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            InfoCard(
                modifier = Modifier.weight(1f),
                label = "Difficulty",
                value = uiState.difficultyText,
                icon = {
                    Icon(
                        imageVector = Icons.Default.SignalCellularAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            InfoCard(
                modifier = Modifier.weight(1f),
                label = "Price",
                value = uiState.priceRangeText,
                icon = {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InfoCard(
                modifier = Modifier.weight(1f),
                label = "Portions",
                value = uiState.servingsText,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            InfoCard(
                modifier = Modifier.weight(1f),
                label = "Calories",
                value = uiState.caloriesText,
                icon = {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun InfoCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: @Composable () -> Unit
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            icon()

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DiaryActionButton(
    label: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.MenuBook,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(label)
    }
}

@Composable
private fun RecipeActionsRow(
    canEdit: Boolean,
    canDelete: Boolean,
    canSave: Boolean,
    canDuplicate: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onSaveClick: () -> Unit,
    onDuplicateClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (canEdit) {
            OutlinedButton(
                onClick = onEditClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit")
            }
        }

        if (canDelete) {
            OutlinedButton(
                onClick = onDeleteClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete")
            }
        }

        if (canSave) {
            OutlinedButton(
                onClick = onSaveClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text("Save")
            }
        }

        if (canDuplicate) {
            OutlinedButton(
                onClick = onDuplicateClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text("Duplicate")
            }
        }
    }
}

@Composable
private fun IngredientsSection(ingredients: List<IngredientItem>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Ingredients List",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        ingredients.forEachIndexed { index, ingredient ->
            val quantityText = when {
                ingredient.unit == IngredientUnit.TO_TASTE.name -> "to taste"
                ingredient.quantity != null -> "${ingredient.quantity} ${ingredient.unit.lowercase()}"
                else -> "q.b."
            }

            Text(
                text = "• ${ingredient.name} — $quantityText",
                style = MaterialTheme.typography.bodyLarge
            )

            if (index != ingredients.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StepsSection(steps: List<InstructionStep>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Steps",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        steps.forEach { step ->
            Text(
                text = "${step.stepNumber}. ${step.text}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

data class ReviewPreviewUiState(
    val id: String,
    val authorLabel: String,
    val rating: Int,
    val title: String,
    val text: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isCurrentUserReview: Boolean
)

data class TipPreviewUiState(
    val id: String,
    val authorLabel: String,
    val text: String
)

private val CommunityActionButtonWidth = 144.dp

@Composable
private fun PrimaryCommunityActionButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.width(CommunityActionButtonWidth)
    ) {
        Text(text)
    }
}

@Composable
private fun SecondaryCommunityActionButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.width(CommunityActionButtonWidth)
    ) {
        Text(text)
    }
}

@Composable
private fun CommunityReviewsSection(
    reviewPreviews: List<ReviewPreviewUiState>,
    isOwnerView: Boolean,
    reviewActionLabel: String,
    onAddReviewClick: () -> Unit,
    onOpenReviewsClick: () -> Unit
) {
    val hasReviews = reviewPreviews.isNotEmpty()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CommunitySectionHeader(
                title = "Reviews",
                subtitle = "Ratings and feedback from people who tried this recipe."
            ) {
                if (isOwnerView) {
                    PrimaryCommunityActionButton(
                        text = "Open reviews",
                        onClick = onOpenReviewsClick,
                        enabled = hasReviews
                    )
                } else {
                    PrimaryCommunityActionButton(
                        text = reviewActionLabel,
                        onClick = onAddReviewClick
                    )

                    SecondaryCommunityActionButton(
                        text = "Open reviews",
                        onClick = onOpenReviewsClick,
                        enabled = hasReviews
                    )
                }
            }

            if (hasReviews) {
                reviewPreviews.forEach { preview ->
                    ReviewPreviewCard(preview = preview)
                }
            } else {
                Text(
                    text = if (isOwnerView) {
                        "No reviews yet. Community feedback will appear here after users try your recipe."
                    } else {
                        "No reviews yet. Be the first to share your cooking experience."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CommunityTipsSection(
    tipPreviews: List<TipPreviewUiState>,
    isOwnerView: Boolean,
    onAddTipClick: () -> Unit
) {
    val hasTips = tipPreviews.isNotEmpty()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CommunitySectionHeader(
                title = "Tips",
                subtitle = "Practical suggestions, variations and do's or don'ts shared by the community."
            ) {
                if (!isOwnerView) {
                    PrimaryCommunityActionButton(
                        text = "Add tip",
                        onClick = onAddTipClick
                    )
                }
            }

            if (hasTips) {
                tipPreviews.forEach { preview ->
                    TipPreviewCard(preview = preview)
                }
            } else {
                Text(
                    text = if (isOwnerView) {
                        "No tips yet. Community suggestions and practical variants will appear here once users share them."
                    } else {
                        "No tips yet. Share a useful variation or a practical cooking suggestion."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CommunitySectionHeader(
    title: String,
    subtitle: String,
    actions: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            actions()
        }
    }
}

@Composable
private fun ReviewPreviewCard(
    preview: ReviewPreviewUiState
) {
    val isEdited = preview.updatedAt > preview.createdAt + 60_000L
    val temporalLabel = formatReviewTime(
        timestamp = if (isEdited) preview.updatedAt else preview.createdAt
    )

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (preview.isCurrentUserReview) 3.dp else 1.dp
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (preview.isCurrentUserReview) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = preview.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                RatingStars(rating = preview.rating)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = preview.authorLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "• $temporalLabel",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isEdited) {
                    Text(
                        text = "• Edited",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (preview.isCurrentUserReview) {
                Text(
                    text = "Your review",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            if (preview.text.isNotBlank()) {
                Text(
                    text = preview.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatReviewTime(timestamp: Long): String {
    return DateUtils.getRelativeTimeSpanString(
        timestamp,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    ).toString()
}

@Composable
private fun TipPreviewCard(
    preview: TipPreviewUiState
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = preview.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = preview.authorLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RatingStars(
    rating: Int
) {
    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
        (1..5).forEach { star ->
            Text(
                text = if (star <= rating) "★" else "☆",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}