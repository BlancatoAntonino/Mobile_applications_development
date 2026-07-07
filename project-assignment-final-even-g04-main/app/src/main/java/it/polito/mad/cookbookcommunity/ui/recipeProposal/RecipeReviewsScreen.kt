package it.polito.mad.cookbookcommunity.ui.recipeProposal

import android.text.format.DateUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.polito.mad.cookbookcommunity.data.recipe.RecipeRepository
import it.polito.mad.cookbookcommunity.data.review.ReviewRepository
import it.polito.mad.cookbookcommunity.model.review.Review
import it.polito.mad.cookbookcommunity.session.SessionManager
import it.polito.mad.cookbookcommunity.ui.components.recipeproposal.RecipeImageContent

private data class RecipeReviewUiState(
    val id: String,
    val title: String,
    val author: String,
    val rating: Int,
    val text: String,
    val photoUri: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val isCurrentUserReview: Boolean
)

@Composable
fun RecipeReviewsRoute(
    recipeId: String,
    recipeRepository: RecipeRepository,
    reviewRepository: ReviewRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recipe by recipeRepository
        .getRecipeProposalById(recipeId)
        .collectAsStateWithLifecycle(initialValue = null)

    val reviews by reviewRepository
        .getReviewsByRecipe(recipeId)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val reviewUiStates = reviews
        .sortedWith(
            compareByDescending<Review> { review ->
                review.authorId == SessionManager.CURRENT_LOGGED_IN_USER_ID
            }.thenByDescending { review ->
                review.updatedAt
            }
        )
        .map { review ->
            review.toRecipeReviewUiState(
                authorLabel = review.authorDisplayName.ifBlank { "User ${review.authorId}" }
            )
        }

    RecipeReviewsScreen(
        recipeTitle = recipe?.title ?: "Recipe reviews",
        reviews = reviewUiStates,
        onBackClick = onBackClick,
        modifier = modifier
    )
}

private fun Review.toRecipeReviewUiState(
    authorLabel: String
): RecipeReviewUiState {
    return RecipeReviewUiState(
        id = id,
        title = title.ifBlank { "Community review" },
        author = authorLabel.toNicknameLabel(),
        rating = rating.coerceIn(0, 5),
        text = text,
        photoUri = photoUri,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isCurrentUserReview = authorId == SessionManager.CURRENT_LOGGED_IN_USER_ID
    )
}

private fun String.toNicknameLabel(): String {
    val cleanLabel = trim().removePrefix("@")
    return if (cleanLabel.isBlank()) "@unknown" else "@$cleanLabel"
}

@Composable
private fun RecipeReviewsScreen(
    recipeTitle: String,
    reviews: List<RecipeReviewUiState>,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val averageRating = reviews
        .takeIf { it.isNotEmpty() }
        ?.map { it.rating }
        ?.average()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            RecipeReviewsTitle(
                recipeTitle = recipeTitle,
                averageRating = averageRating,
                reviewCount = reviews.size,
                onBackClick = onBackClick
            )
        }

        if (reviews.isEmpty()) {
            item {
                EmptyReviewsCard()
            }
        } else {
            items(
                items = reviews,
                key = { review -> review.id }
            ) { review ->
                ReviewCard(review = review)
            }
        }
    }
}

@Composable
private fun EmptyReviewsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = "No reviews yet for this recipe.",
            modifier = Modifier.padding(18.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RecipeReviewsTitle(
    recipeTitle: String,
    averageRating: Double?,
    reviewCount: Int,
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

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "$recipeTitle reviews",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = averageRating?.let {
                    "%.1f/5 - $reviewCount ${if (reviewCount == 1) "review" else "reviews"}".format(it)
                } ?: "No rating yet",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReviewCard(
    review: RecipeReviewUiState,
    modifier: Modifier = Modifier
) {
    val isEdited = review.updatedAt > review.createdAt + 60_000L
    val temporalLabel = formatReviewTime(
        timestamp = if (isEdited) review.updatedAt else review.createdAt
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (review.isCurrentUserReview) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ReviewAvatar(author = review.author)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = review.title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    RatingStars(rating = review.rating)
                }

                Text(
                    text = "${review.author} • $temporalLabel${if (isEdited) " • Edited" else ""}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (review.isCurrentUserReview) {
                    Text(
                        text = "Your review",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = review.text,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            ReviewPhoto(photoUri = review.photoUri)
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
private fun ReviewAvatar(author: String) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = author
                .removePrefix("@")
                .firstOrNull()
                ?.uppercase()
                ?: "?",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun RatingStars(rating: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(5) { index ->
            Icon(
                imageVector = if (index < rating) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ReviewPhoto(photoUri: String?) {
    Box(
        modifier = Modifier
            .width(64.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center
    ) {
        if (photoUri == null) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = "No review photo",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            RecipeImageContent(
                imageUri = photoUri,
                contentDescription = "Review photo",
                modifier = Modifier.fillMaxSize(),
                placeholderText = "Review photo"
            )
        }
    }
}