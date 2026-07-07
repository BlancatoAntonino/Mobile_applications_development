@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package it.polito.mad.cookbookcommunity.ui.components.recipeproposal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.polito.mad.cookbookcommunity.model.recipe.DietaryRestriction
import it.polito.mad.cookbookcommunity.model.recipe.DifficultyLevel
import it.polito.mad.cookbookcommunity.model.recipe.PriceRange
import it.polito.mad.cookbookcommunity.model.recipe.RecipeProposal
import it.polito.mad.cookbookcommunity.model.recipe.RecipeType

@Composable
fun RecipeProposalCard(
    recipe: RecipeProposal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            RecipeCardImage(imageUri = recipe.imageUri)

            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = recipe.title.ifBlank { "Untitled recipe" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (recipe.description.isNotBlank()) {
                    Text(
                        text = recipe.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompactInfoChip(
                        label = recipe.priceRange,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    )
                    CompactInfoChip(label = recipe.recipeType)
                    CompactInfoChip(
                        label = "${recipe.cookTimeMinutes} min",
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    )
                    CompactInfoChip(
                        label = recipe.difficulty,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.SignalCellularAlt,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    )
                }

                if (recipe.dietaryRestrictions.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        recipe.dietaryRestrictions.take(3).forEach { restriction ->
                            RestrictionChip(label = restriction)
                        }
                        if (recipe.dietaryRestrictions.size > 3) {
                            RestrictionChip(label = "+${recipe.dietaryRestrictions.size - 3}")
                        }
                    }
                }

                val ingredientPreview = recipe.ingredients
                    .take(3)
                    .joinToString(", ") { it.name }

                if (ingredientPreview.isNotBlank()) {
                    Text(
                        text = ingredientPreview,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun RecipeCardImage(
    imageUri: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        RecipeImageContent(
            imageUri = imageUri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun CompactInfoChip(
    label: String,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun RestrictionChip(label: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.tertiaryContainer
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            maxLines = 1
        )
    }
}

fun PriceRange.displayName(): String = when (this) {
    PriceRange.LOW -> "Low cost"
    PriceRange.MEDIUM -> "Medium cost"
    PriceRange.HIGH -> "High cost"
}



fun RecipeType.displayName(): String {
    return name.replace('_', ' ')
        .lowercase()
        .replaceFirstChar { it.uppercase() }
}

fun DietaryRestriction.displayName(): String {
    return name.replace('_', ' ')
        .lowercase()
        .replaceFirstChar { it.uppercase() }
}
