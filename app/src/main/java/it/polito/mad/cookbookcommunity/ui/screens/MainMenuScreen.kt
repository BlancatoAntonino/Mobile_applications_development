package it.polito.mad.cookbookcommunity.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class MainMenuScreen {
    MENU,
    OWN_PROFILE,
    OTHER_PROFILE,
    AUTHOR_PROFILE,
    RECIPE_DETAILS,
    RECIPES_LIST,
    MY_RECIPES_LIST,
    CREATE_RECIPE,
    RECIPE_PROPOSAL_DUPLICATE
}

@Composable
fun MainEntryScreen(
    onOwnProfileClick: () -> Unit = {},
    onOtherProfileClick: () -> Unit = {},
    onRecipesListClick: () -> Unit = {},
    onMyRecipesListClick: () -> Unit = {},
    onCreateRecipeClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(
            space = 16.dp,
            alignment = Alignment.CenterVertically
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "A Recipe Sharing Application",
            style = MaterialTheme.typography.headlineSmall
        )

        Button(
            onClick = onOwnProfileClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("GoToOwnProfile")
        }

        Button(
            onClick = onOtherProfileClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("GoToOtherProfile")
        }

        Button(
            onClick = onRecipesListClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("RecipesList")
        }

        Button(
            onClick = onMyRecipesListClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("MyRecipeProposalList")
        }

        Button(
            onClick = onCreateRecipeClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("CreateNewRecipeProposal")
        }

    }
}

@Composable
fun RecipeProposalListPlaceholderScreen(
    totalRecipesCount: Int,
    previewTitles: List<String>,
    onBackClick: () -> Unit,
    onOpenFirstRecipeClick: (() -> Unit)? = null
) {
    PlaceholderScaffold(
        title = "RecipeProposalList",
        onBackClick = onBackClick
    ) {
        Text("Temporary placeholder screen")
        Text("Total recipes currently stored: $totalRecipesCount")

        if (onOpenFirstRecipeClick != null) {
            Button(onClick = onOpenFirstRecipeClick) {
                Text("Open first recipe details")
            }
        }

        if (previewTitles.isNotEmpty()) {
            Text(
                text = "Preview titles:",
                style = MaterialTheme.typography.titleMedium
            )
            previewTitles.forEach { title ->
                Text("• $title")
            }
        }
    }
}

@Composable
fun OwnedRecipeProposalListPlaceholderScreen(
    ownedRecipesCount: Int,
    previewTitles: List<String>,
    onBackClick: () -> Unit
) {
    PlaceholderScaffold(
        title = "OwnedRecipeProposalList",
        onBackClick = onBackClick
    ) {
        Text("Temporary placeholder screen")
        Text("Recipes owned by current user: $ownedRecipesCount")

        if (previewTitles.isNotEmpty()) {
            Text(
                text = "Preview titles",
                style = MaterialTheme.typography.titleMedium
            )
            previewTitles.forEach { title ->
                Text("• $title")
            }
        }
    }
}

@Composable
fun NewRecipeProposalPlaceholderScreen(
    currentOwnerId: String,
    totalRecipesCount: Int,
    onBackClick: () -> Unit
) {
    PlaceholderScaffold(
        title = "NewRecipeProposal",
        onBackClick = onBackClick
    ) {
        Text("Temporary placeholder screen")
        Text("New recipes will be assigned to ownerId = $currentOwnerId")
        Text("Current recipes in repository: $totalRecipesCount")
    }
}

@Composable
private fun PlaceholderScaffold(
    title: String,
    onBackClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(
                start = 24.dp,
                end = 24.dp,
                top = 32.dp,
                bottom = 24.dp
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(onClick = onBackClick) {
            Text("Back")
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall
        )

        content()
    }
}
