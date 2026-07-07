package it.polito.mad.cookbookcommunity.navigation.graphs

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import it.polito.mad.cookbookcommunity.data.AppContainer
import it.polito.mad.cookbookcommunity.navigation.AppNavigation
import it.polito.mad.cookbookcommunity.navigation.CollectionDetailNavRoute
import it.polito.mad.cookbookcommunity.navigation.DiaryRoute
import it.polito.mad.cookbookcommunity.navigation.OtherProfileRoute
import it.polito.mad.cookbookcommunity.navigation.OwnProfileRoute
import it.polito.mad.cookbookcommunity.navigation.SaveToCollectionDialogRoute
import it.polito.mad.cookbookcommunity.navigation.SavedCollectionsRoute
import it.polito.mad.cookbookcommunity.navigation.TriedRecipesRoute
import it.polito.mad.cookbookcommunity.session.SessionManager
import it.polito.mad.cookbookcommunity.ui.profile.CollectionDetailRoute
import it.polito.mad.cookbookcommunity.ui.profile.ProfileRoute
import it.polito.mad.cookbookcommunity.ui.profile.SaveToCollectionRoute
import it.polito.mad.cookbookcommunity.ui.profile.SavedRoute
import it.polito.mad.cookbookcommunity.ui.profile.TriedRecipesScreenRoute

internal fun NavGraphBuilder.profileGraph(
    appNavigation: AppNavigation,
    appContainer: AppContainer,
    onProfileEditingChanged: (Boolean) -> Unit,
    onSignOutClick: () -> Unit,
    onFeedback: (String) -> Unit,
) {
    val authRepository = appContainer.authRepository
    val userProfileRepository = appContainer.userProfileRepository
    val recipeRepository = appContainer.recipeRepository
    val triedRecipeRepository = appContainer.triedRecipeRepository
    val favoriteRepository = appContainer.favoriteRepository
    val favoriteCollectionRepository = appContainer.favoriteCollectionRepository

    composable<OwnProfileRoute> {
        val currentAuthUser by authRepository.currentUser.collectAsStateWithLifecycle()
        val userId = currentAuthUser?.uid ?: return@composable

        ProfileRoute(
            profileUserId = userId,
            viewerUserId = userId,
            userProfileRepository = userProfileRepository,
            onExitProfile = {
                appNavigation.navigateBack()
            },
            myProfile = true,
            onEditingChanged = { isEditing ->
                onProfileEditingChanged(isEditing)
            },
            onMyRecipesClick = { ownerId ->
                appNavigation.navigateToRecipeListOwned(
                    ownerId,
                    true,
                    "My"
                )
            },
            onTriedRecipesClick = { ownerId ->
                appNavigation.navigateToDiary(ownerId)
            },
            onSavedClick = {
                appNavigation.navigateToSavedCollections()
            },
            onSignOutClick = onSignOutClick
        )
    }

    composable<OtherProfileRoute> { backStackEntry ->
        val args = backStackEntry.toRoute<OtherProfileRoute>()

        ProfileRoute(
            profileUserId = args.userId,
            viewerUserId = SessionManager.CURRENT_LOGGED_IN_USER_ID,
            userProfileRepository = userProfileRepository,
            onExitProfile = {
                appNavigation.navigateBack()
            },
            myProfile = false,
            onMyRecipesClick = { ownerId ->
                appNavigation.navigateToRecipeListOwned(
                    ownerId,
                    false,
                    "Their"
                )
            }
        )
    }

    composable<TriedRecipesRoute> { backStackEntry ->
        val args = backStackEntry.toRoute<TriedRecipesRoute>()
        val currentAuthUser by authRepository.currentUser.collectAsStateWithLifecycle()
        val currentUserId = currentAuthUser?.uid ?: return@composable

        if (args.userId != currentUserId) {
            LaunchedEffect(args.userId, currentUserId) {
                onFeedback("You can only open your own diary.")
                appNavigation.navigateBack()
            }
            return@composable
        }

        TriedRecipesScreenRoute(
            userId = currentUserId,
            triedRecipeRepository = triedRecipeRepository,
            recipeRepository = recipeRepository,
            onBackClick = {
                appNavigation.navigateBack()
            },
            onDiaryEntryClick = { recipeId, entryId ->
                appNavigation.navigateToAddDiaryEntry(
                    recipeId = recipeId,
                    entryId = entryId
                )
            },
            onFeedback = onFeedback
        )
    }

    composable<DiaryRoute> { backstackEntry ->
        val args = backstackEntry.toRoute<DiaryRoute>()
        val currentAuthUser by authRepository.currentUser.collectAsStateWithLifecycle()
        val currentUserId = currentAuthUser?.uid ?: return@composable

        if (args.userId != currentUserId) {
            LaunchedEffect(args.userId, currentUserId) {
                onFeedback("You can only open your own diary.")
                appNavigation.navigateBack()
            }
            return@composable
        }

        TriedRecipesScreenRoute(
            userId = currentUserId,
            triedRecipeRepository = triedRecipeRepository,
            recipeRepository = recipeRepository,
            onBackClick = {
                appNavigation.navigateBack()
            },
            onDiaryEntryClick = { recipeId, entryId ->
                appNavigation.navigateToAddDiaryEntry(
                    recipeId = recipeId,
                    entryId = entryId
                )
            },
            onFeedback = onFeedback
        )
    }

    composable<SavedCollectionsRoute> {
        SavedRoute(
            ownerId = SessionManager.CURRENT_LOGGED_IN_USER_ID,
            repository = favoriteCollectionRepository,
            onCollectionClick = { collectionId, collectionName ->
                appNavigation.navigateToCollectionDetail(
                    collectionId = collectionId,
                    collectionName = collectionName
                )
            }
        )
    }

    composable<CollectionDetailNavRoute> { backStackEntry ->
        val args = backStackEntry.toRoute<CollectionDetailNavRoute>()

        CollectionDetailRoute(
            collectionId = args.collectionId,
            collectionName = args.collectionName,
            repository = favoriteCollectionRepository,
            favoriteRepository = favoriteRepository,
            recipeProposalRepository = recipeRepository,
            onBackClick = {
                appNavigation.navigateBack()
            },
            onRecipeClick = { recipeId ->
                appNavigation.navigateToRecipeDetail(recipeId, false)
            }
        )
    }

    dialog<SaveToCollectionDialogRoute> { backStackEntry ->
        val args = backStackEntry.toRoute<SaveToCollectionDialogRoute>()

        SaveToCollectionRoute(
            ownerId = SessionManager.CURRENT_LOGGED_IN_USER_ID,
            recipeId = args.recipeId,
            repository = favoriteCollectionRepository,
            favoriteRepository = favoriteRepository,
            recipeRepository = recipeRepository,
            onDismiss = {
                appNavigation.navigateBack()
            },
            onFeedback = { message ->
                onFeedback(message)
            }
        )
    }
}