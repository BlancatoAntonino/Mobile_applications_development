package it.polito.mad.cookbookcommunity.ui.components.recipeproposal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun RecipeDerivationCard(
    originalRecipeTitle: String,
    originalRecipeUri: String,
    originalAuthorName: String,
    originalAuthorPhotoUrl: String,
    adaptationNote: String,
    isOriginalRecipeAvailable: Boolean,
    isOriginalRecipeLoading: Boolean,
    isOriginalAuthorAvailable: Boolean,
    onOriginalRecipeClick: () -> Unit,
    onOriginalAuthorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayRecipeTitle = originalRecipeTitle.ifBlank { "Original recipe" }
    val displayAuthorName = originalAuthorName.ifBlank { "Original author" }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 1.dp
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )

                if (isOriginalRecipeLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            if (!isOriginalAuthorAvailable && !isOriginalRecipeLoading) {
                Text(
                    text = "Original recipe no longer available.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .clickable(
                        enabled = isOriginalRecipeAvailable,
                        onClick = onOriginalRecipeClick
                    )
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .width(96.dp)
                        .height(72.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    RecipeImageContent(
                        imageUri = originalRecipeUri,
                        contentDescription = "Original recipe cover image",
                        modifier = Modifier,
                        placeholderText = "Original recipe"
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Original recipe",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    Text(
                        text = displayRecipeTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    Text(
                        text = if (isOriginalRecipeAvailable) {
                            "Open original recipe"
                        } else {
                            "Stored derivation details"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                    alpha = 0.18f
                )
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OriginalAuthorAvatar(
                    photoUrl = originalAuthorPhotoUrl
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Original author",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    Text(
                        text = displayAuthorName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                TextButton(
                    enabled = isOriginalAuthorAvailable,
                    onClick = onOriginalAuthorClick
                ) {
                    Text("View profile")
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                    alpha = 0.18f
                )
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Your adaptation",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Text(
                    text = adaptationNote.ifBlank {
                        "No adaptation note available."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun OriginalAuthorAvatar(
    photoUrl: String
) {
    if (photoUrl.isBlank()) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Original author",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    } else {
        AsyncImage(
            model = photoUrl,
            contentDescription = "Original author profile image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
    }
}