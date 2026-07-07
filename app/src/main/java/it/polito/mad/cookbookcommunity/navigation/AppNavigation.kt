package it.polito.mad.cookbookcommunity.navigation

import androidx.navigation.NavController

class AppNavigation(private val navController: NavController) {
    fun navigateToHome() {
        navController.navigate(HomeRoute) {
            popUpTo<HomeRoute> {
                inclusive = false
                saveState = false
            }
            launchSingleTop = true
            restoreState = false
        }
    }

    fun navigateToOwnProfile() {
        navController.navigate(OwnProfileRoute) {
            popUpTo<HomeRoute> {
                saveState = false
                inclusive = false
            }
            launchSingleTop = true
            restoreState = false
        }
    }

    fun navigateToOwnProfileFromContent() {
        navController.navigate(OwnProfileRoute)
    }

    fun navigateToOtherProfile(userId: String) {
        navController.navigate(OtherProfileRoute(userId)) {
            launchSingleTop = true
        }
    }

    fun navigateToExplore() {
        navController.navigate(RecipesListRoute) {
            popUpTo<HomeRoute> {
                saveState = false
                inclusive = false
            }
            launchSingleTop = true
            restoreState = false
        }
    }

    fun navigateToRecipeDetail(recipeId: String, fromList: Boolean) {
        navController.navigate(RecipeDetailRoute(recipeId, fromList)) {
            launchSingleTop = true
        }
    }

    fun navigateToPublishedRecipeFromCreate(recipeId: String) {
        navController.navigate(RecipeDetailRoute(recipeId, false)) {
            popUpTo<CreateRecipeRoute> {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    fun navigateToPublishedRecipeFromAdapt(recipeId: String) {
        navController.navigate(RecipeDetailRoute(recipeId, false)) {
            popUpTo<AdaptRecipeRoute> {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    fun navigateToRecipeListOwned(ownerId: String, isOwner: Boolean, ownerName: String) {
        navController.navigate(RecipeListOwnedRoute(ownerId, isOwner, ownerName)) {
            launchSingleTop = true
        }
    }

    fun navigateToCreateRecipe() {
        navController.navigate(CreateRecipeRoute) {
            popUpTo<HomeRoute> {
                saveState = false
                inclusive = false
            }
            launchSingleTop = true
            restoreState = false
        }
    }

    fun navigateToAdaptRecipe(sourceRecipeId: String) {
        navController.navigate(AdaptRecipeRoute(sourceRecipeId)) {
            launchSingleTop = true
        }
    }

    fun navigateToNotifications() {
        navController.navigate(NotificationsRoute) {
            launchSingleTop = true
        }
    }

    fun navigateToTriedRecipes(userId: String) {
        navigateToDiary(userId)
    }

    fun navigateToDiary(userId: String) {
        navController.navigate(DiaryRoute(userId)) {
            launchSingleTop = true
        }
    }

    fun navigateToAddDiaryEntry(recipeId: String, entryId: String? = null) {
        navController.navigate(
            AddDiaryEntryRoute(
                recipeId = recipeId,
                entryId = entryId
            )
        ) {
            launchSingleTop = true
        }
    }

    fun navigateToSavedCollections() {
        navController.navigate(SavedCollectionsRoute) {
            popUpTo<HomeRoute> {
                inclusive = false
                saveState = false
            }
            launchSingleTop = true
            restoreState = false
        }
    }

    fun navigateToAuthGraph() {
        navController.navigate(AuthGraphRoute) {
            launchSingleTop = true
        }
    }

    fun navigateToLogin() {
        navigateToAuthGraph()
    }

    fun navigateToSignUpAfterAccountCreation() {
        navController.navigate(SignUpAccountRoute) {
            popUpTo<AuthGraphRoute> {
                inclusive = false
            }
            launchSingleTop = true
        }
    }

    fun navigateAfterSuccessfulLogin(destination: PostLoginDestination?) {
        when (destination) {
            PostLoginDestination.SAVED -> {
                navigateToSavedCollections()
            }

            PostLoginDestination.PUBLISH -> {
                navigateToCreateRecipe()
            }

            PostLoginDestination.NOTIFICATIONS -> {
                navigateToNotifications()
            }

            PostLoginDestination.PROFILE,
            null -> {
                navigateToOwnProfile()
            }
        }
    }

    fun navigateToSignUp() {
        navController.navigate(SignUpAccountRoute) {
            launchSingleTop = true
        }
    }

    fun navigateToAuthEntry() {
        navController.navigate(AuthEntryRoute) {
            launchSingleTop = true
        }
    }

    fun navigateToAuthPhotoRole() {
        navController.navigate(AuthEntryPhotoRoleRoute) {
            launchSingleTop = true
        }
    }

    fun navigateToAuthPreferences() {
        navController.navigate(AuthEntryPreferencesRoute) {
            launchSingleTop = true
        }
    }

    fun navigateBack() {
        navController.popBackStack()
    }

    fun navigateToCollectionDetail(collectionId: String, collectionName: String) {
        navController.navigate(
            CollectionDetailNavRoute(
                collectionId = collectionId,
                collectionName = collectionName
            )
        ) {
            launchSingleTop = true
        }
    }

    fun navigateToSaveToCollection(recipeId: String) {
        navController.navigate(SaveToCollectionDialogRoute(recipeId)) {
            launchSingleTop = true
        }
    }

    fun navigateToAddReview(recipeId: String, reviewId: String? = null) {
        navController.navigate(AddReviewRoute(recipeId, reviewId)) {
            launchSingleTop = true
        }
    }

    fun navigateToAddTip(recipeId: String) {
        navController.navigate(AddTipRoute(recipeId)) {
            launchSingleTop = true
        }
    }

    fun navigateToRecipeReviews(recipeId: String) {
        navController.navigate(RecipeReviewsNavRoute(recipeId)) {
            launchSingleTop = true
        }
    }

    fun navigateToOriginalRecipe(recipeId: String) {
        navController.navigate(RecipeDetailRoute(recipeId, false))
    }
}
