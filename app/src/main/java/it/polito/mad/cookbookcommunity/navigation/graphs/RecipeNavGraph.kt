package it.polito.mad.cookbookcommunity.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import it.polito.mad.cookbookcommunity.data.AppContainer
import it.polito.mad.cookbookcommunity.navigation.AdaptRecipeRoute
import it.polito.mad.cookbookcommunity.navigation.AddDiaryEntryRoute
import it.polito.mad.cookbookcommunity.navigation.AddReviewRoute
import it.polito.mad.cookbookcommunity.navigation.AddTipRoute
import it.polito.mad.cookbookcommunity.navigation.AppNavigation
import it.polito.mad.cookbookcommunity.navigation.CreateRecipeRoute
import it.polito.mad.cookbookcommunity.navigation.PostLoginDestination
import it.polito.mad.cookbookcommunity.navigation.RecipeDetailRoute
import it.polito.mad.cookbookcommunity.navigation.RecipeListOwnedRoute
import it.polito.mad.cookbookcommunity.navigation.RecipeReviewsNavRoute
import it.polito.mad.cookbookcommunity.navigation.RecipesListRoute
import it.polito.mad.cookbookcommunity.session.SessionManager
import it.polito.mad.cookbookcommunity.ui.diary.AddDiaryEntryScreenRoute
import it.polito.mad.cookbookcommunity.ui.recipeProposal.NewRecipeProposalRoute
import it.polito.mad.cookbookcommunity.ui.recipeProposal.RecipeListRoute
import it.polito.mad.cookbookcommunity.ui.recipeProposal.RecipeProposalRoute
import it.polito.mad.cookbookcommunity.ui.recipeProposal.RecipeReviewsRoute
import it.polito.mad.cookbookcommunity.ui.recipeProposal.list.RecipeProposalListRoute
import it.polito.mad.cookbookcommunity.ui.review.AddReviewScreenRoute
import it.polito.mad.cookbookcommunity.ui.review.AddTipScreenRoute
import it.polito.mad.cookbookcommunity.viewmodel.NewRecipeStep

internal fun NavGraphBuilder.recipeGraph(
    appNavigation: AppNavigation,
    appContainer: AppContainer,
    navigateIfLoggedIn: (PostLoginDestination, () -> Unit) -> Unit,
    onNewRecipeStepChanged: (NewRecipeStep) -> Unit,
    onNewRecipeFormDataChanged: (Boolean) -> Unit,
    onRegisterNewRecipeReset: (() -> Unit) -> Unit,
    onFeedback: (String) -> Unit,
) {
    val recipeRepository = appContainer.recipeRepository
    val userProfileRepository = appContainer.userProfileRepository
    val reviewRepository = appContainer.reviewRepository
    val triedRecipeRepository = appContainer.triedRecipeRepository
    val tipRepository = appContainer.tipRepository
    val deleteRecipeProposalUseCase = appContainer.deleteRecipeProposalUseCase

    composable<RecipesListRoute> {
        RecipeProposalListRoute(
            repository = recipeRepository,
            onBackClick = {
                appNavigation.navigateBack()
            },
            onRecipeClick = { id ->
                appNavigation.navigateToRecipeDetail(id, true)
            },
            showBackButton = false
        )
    }

    composable<RecipeDetailRoute> { backStackEntry ->
        val args = backStackEntry.toRoute<RecipeDetailRoute>()

        RecipeProposalRoute(
            recipeId = args.recipeId,
            repository = recipeRepository,
            userProfileRepository = userProfileRepository,
            reviewRepository = reviewRepository,
            tipRepository = tipRepository,
            triedRecipeRepository = triedRecipeRepository,
            deleteRecipeProposalUseCase = deleteRecipeProposalUseCase,
            onBackClick = {
                appNavigation.navigateBack()
            },
            onDuplicateClick = { sourceRecipeId ->
                navigateIfLoggedIn(PostLoginDestination.PUBLISH) {
                    appNavigation.navigateToAdaptRecipe(sourceRecipeId)
                }
            },
            onAuthorProfileClick = { userId ->
                if (userId == SessionManager.CURRENT_LOGGED_IN_USER_ID) {
                    appNavigation.navigateToOwnProfileFromContent()
                } else {
                    appNavigation.navigateToOtherProfile(userId)
                }
            },
            onOriginalRecipeClick = { originalRecipeId ->
                appNavigation.navigateToOriginalRecipe(originalRecipeId)
            },
            onOriginalAuthorClick = { originalAuthorId ->
                if (originalAuthorId == SessionManager.CURRENT_LOGGED_IN_USER_ID) {
                    appNavigation.navigateToOwnProfileFromContent()
                } else {
                    appNavigation.navigateToOtherProfile(originalAuthorId)
                }
            },
            onSaveToCollectionClick = { recipeId ->
                navigateIfLoggedIn(PostLoginDestination.SAVED) {
                    appNavigation.navigateToSaveToCollection(recipeId)
                }
            },
            onAddReviewClick = { recipeId, reviewId ->
                navigateIfLoggedIn(PostLoginDestination.PROFILE) {
                    appNavigation.navigateToAddReview(recipeId, reviewId)
                }
            },
            onAddTipClick = { recipeId ->
                navigateIfLoggedIn(PostLoginDestination.PROFILE) {
                    appNavigation.navigateToAddTip(recipeId)
                }
            },
            onAddDiaryClick = { recipeId, entryId ->
                navigateIfLoggedIn(PostLoginDestination.PROFILE) {
                    appNavigation.navigateToAddDiaryEntry(
                        recipeId = recipeId,
                        entryId = entryId)
                }
            },
            onSeeAllReviewsClick = { recipeId ->
                appNavigation.navigateToRecipeReviews(recipeId)
            }
        )
    }

    composable<RecipeListOwnedRoute> { backStackEntry ->
        val args = backStackEntry.toRoute<RecipeListOwnedRoute>()

        RecipeListRoute(
            ownerId = args.ownerId,
            isOwnerView = args.isOwner,
            ownerDisplayName = args.ownerName,
            repository = recipeRepository,
            deleteRecipeProposalUseCase = deleteRecipeProposalUseCase,
            onBackClick = {
                appNavigation.navigateBack()
            },
            onRecipeClick = { id ->
                appNavigation.navigateToRecipeDetail(id, false)
            }
        )
    }

    composable<CreateRecipeRoute> {
        NewRecipeProposalRoute(
            repository = recipeRepository,
            onBackClick = {
                appNavigation.navigateBack()
            },
            onPublished = { id ->
                appNavigation.navigateToPublishedRecipeFromCreate(id)
            },
            onStepChanged = { step ->
                onNewRecipeStepChanged(step)
            },
            onFormDataChanged = { hasDraftData ->
                onNewRecipeFormDataChanged(hasDraftData)
            },
            onRegisterReset = { reset ->
                onRegisterNewRecipeReset(reset)
            },
            onFeedback = { message ->
                onFeedback(message)
            }
        )
    }

    composable<AdaptRecipeRoute> { backStackEntry ->
        val args = backStackEntry.toRoute<AdaptRecipeRoute>()

        NewRecipeProposalRoute(
            repository = recipeRepository,
            sourceRecipeId = args.sourceRecipeId,
            onBackClick = {
                appNavigation.navigateBack()
            },
            onPublished = { id ->
                appNavigation.navigateToPublishedRecipeFromAdapt(id)
            },
            onStepChanged = { step ->
                onNewRecipeStepChanged(step)
            },
            onFormDataChanged = { hasDraftData ->
                onNewRecipeFormDataChanged(hasDraftData)
            },
            onRegisterReset = { reset ->
                onRegisterNewRecipeReset(reset)
            },
            onFeedback = { message ->
                onFeedback(message)
            }
        )
    }

    composable<AddReviewRoute> { backStackEntry ->
        val args = backStackEntry.toRoute<AddReviewRoute>()

        AddReviewScreenRoute(
            recipeId = args.recipeId,
            initialReviewId = args.reviewId,
            recipeRepository = recipeRepository,
            reviewRepository = reviewRepository,
            triedRecipeRepository = triedRecipeRepository,
            userProfileRepository = userProfileRepository,
            onBackClick = {
                appNavigation.navigateBack()
            },
            onPublished = {
                onFeedback("Review published.")
                appNavigation.navigateBack()
            },
            onFeedback = { message ->
                onFeedback(message)
            }
        )
    }

    composable<AddTipRoute> { backStackEntry ->
        val args = backStackEntry.toRoute<AddTipRoute>()

        AddTipScreenRoute(
            recipeId = args.recipeId,
            recipeRepository = recipeRepository,
            tipRepository = tipRepository,
            onBackClick = {
                appNavigation.navigateBack()
            },
            onPublished = {
                appNavigation.navigateBack()
            }
        )
    }

    composable<AddDiaryEntryRoute> { backstackEntry ->
        val args = backstackEntry.toRoute<AddDiaryEntryRoute>()

        AddDiaryEntryScreenRoute(
            recipeId = args.recipeId,
            entryId = args.entryId,
            recipeRepository = recipeRepository,
            triedRecipeRepository = triedRecipeRepository,
            onBackClick = {
                appNavigation.navigateBack()
            },
            onSaved = {
                appNavigation.navigateBack()
            },
            onFeedback = { message ->
                onFeedback(message)
            }
        )
    }

    composable<RecipeReviewsNavRoute> { backStackEntry ->
        val args = backStackEntry.toRoute<RecipeReviewsNavRoute>()

        RecipeReviewsRoute(
            recipeId = args.recipeId,
            recipeRepository = recipeRepository,
            reviewRepository = reviewRepository,
            onBackClick = {
                appNavigation.navigateBack()
            }
        )
    }
}