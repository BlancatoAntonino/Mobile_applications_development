package it.polito.mad.cookbookcommunity.ui.review

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil.compose.AsyncImage
import it.polito.mad.cookbookcommunity.data.recipe.RecipeRepository
import it.polito.mad.cookbookcommunity.data.review.ReviewRepository
import it.polito.mad.cookbookcommunity.data.repository.TriedRecipeRepository
import it.polito.mad.cookbookcommunity.data.repository.UserProfileRepository
import it.polito.mad.cookbookcommunity.data.user.UserRepository
import it.polito.mad.cookbookcommunity.viewmodel.AddReviewUiState
import it.polito.mad.cookbookcommunity.viewmodel.AddReviewViewModel

@Composable
fun AddReviewScreenRoute(
    recipeId: String,
    modifier: Modifier = Modifier,
    initialReviewId: String? = null,
    recipeRepository: RecipeRepository,
    reviewRepository: ReviewRepository,
    triedRecipeRepository: TriedRecipeRepository,
    userProfileRepository: UserRepository,
    onBackClick: () -> Unit,
    onPublished: () -> Unit,
    onFeedback: (String) -> Unit = {},
) {
    val viewModel: AddReviewViewModel = viewModel(
        key = "add_review_${recipeId}_${initialReviewId ?: "new"}",
        factory = viewModelFactory {
            initializer {
                AddReviewViewModel(
                    recipeId = recipeId,
                    initialReviewId = initialReviewId,
                    recipeRepository = recipeRepository,
                    reviewRepository = reviewRepository,
                    triedRecipeRepository = triedRecipeRepository,
                    userProfileRepository = userProfileRepository
                )
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isPublished) {
        if (uiState.isPublished) {
            onPublished()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let(onFeedback)
    }

    AddReviewScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onRatingChange = viewModel::updateRating,
        onReviewTitleChange = viewModel::updateReviewTitle,
        onReviewTextChange = viewModel::updateReviewText,
        onPhotoClick = viewModel::selectNextPhotoAsset,
        onPublishClick = viewModel::publishReview,
        modifier = modifier
    )
}

@Composable
private fun AddReviewScreen(
    uiState: AddReviewUiState,
    onBackClick: () -> Unit,
    onRatingChange: (Int) -> Unit,
    onReviewTitleChange: (String) -> Unit,
    onReviewTextChange: (String) -> Unit,
    onPhotoClick: () -> Unit,
    onPublishClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }

            item {
                ReviewingRecipeBanner(
                    recipeTitle = uiState.recipeTitle,
                    recipeImageUri = uiState.recipeImageUri,
                    isLoadingRecipe = uiState.isLoadingRecipe
                )
            }

            uiState.reviewBlockedMessage?.let { message ->
                item {
                    ReviewBlockedMessageCard(message = message)
                }
            }

            item {
                RatingSection(
                    rating = uiState.rating,
                    enabled = uiState.canReviewRecipe,
                    onRatingChange = onRatingChange
                )
            }

            item {
                ReviewTitleSection(
                    reviewTitle = uiState.reviewTitle,
                    enabled = uiState.canReviewRecipe,
                    onReviewTitleChange = onReviewTitleChange
                )
            }

            item {
                ReviewTextSection(
                    reviewText = uiState.reviewText,
                    enabled = uiState.canReviewRecipe,
                    onReviewTextChange = onReviewTextChange
                )
            }

            item {
                PhotoSection(
                    photoUri = uiState.photoUri,
                    enabled = uiState.canReviewRecipe,
                    onPhotoClick = onPhotoClick
                )
            }

            uiState.errorMessage?.let { message ->
                item {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onPublishClick,
                        enabled = uiState.canPublish
                    ) {
                        Text(
                            when {
                                uiState.isPublishing -> "Saving..."
                                uiState.isEditing -> "Save changes"
                                else -> "Publish"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewingRecipeBanner(
    recipeTitle: String,
    recipeImageUri: String,
    isLoadingRecipe: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AsyncImage(
                model = recipeImageUri,
                contentDescription = "Recipe being reviewed",
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "REVIEWING",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = if (isLoadingRecipe) "Loading recipe..." else recipeTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ReviewBlockedMessageCard(
    message: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun RatingSection(
    rating: Int,
    enabled: Boolean,
    onRatingChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "How was your culinary experience?",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            (1..5).forEach { star ->
                TextButton(
                    onClick = { onRatingChange(star) },
                    enabled = enabled
                ) {
                    Text(
                        text = if (star <= rating) "★" else "☆",
                        style = MaterialTheme.typography.headlineLarge,
                        color = if (star <= rating) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewTitleSection(
    reviewTitle: String,
    enabled: Boolean,
    onReviewTitleChange: (String) -> Unit
) {
    OutlinedTextField(
        value = reviewTitle,
        onValueChange = onReviewTitleChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Review title") },
        supportingText = { Text("Required") },
        singleLine = true,
        enabled = enabled
    )
}

@Composable
private fun ReviewTextSection(
    reviewText: String,
    enabled: Boolean,
    onReviewTextChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Write your review",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = reviewText,
            onValueChange = onReviewTextChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Describe the flavors, texture and your overall impression...")
            },
            minLines = 5,
            enabled = enabled
        )
    }
}

@Composable
private fun PhotoSection(
    photoUri: String,
    enabled: Boolean,
    onPhotoClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Add a photo of your dish",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clickable(
                    enabled = enabled,
                    onClick = onPhotoClick
                ),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            if (photoUri.isBlank()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "Tap to select a dish photo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "Review photo preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
                    ) {
                        Text(
                            text = "Tap to change photo",
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
