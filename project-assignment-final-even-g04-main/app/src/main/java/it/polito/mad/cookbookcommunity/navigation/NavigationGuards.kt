package it.polito.mad.cookbookcommunity.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute

fun NavDestination?.isProtectedRoute(): Boolean {
    return this?.hasRoute<OwnProfileRoute>() == true ||
            this?.hasRoute<CreateRecipeRoute>() == true ||
            this?.hasRoute<AdaptRecipeRoute>() == true ||
            this?.hasRoute<NotificationsRoute>() == true ||
            this?.hasRoute<SavedCollectionsRoute>() == true ||
            this?.hasRoute<CollectionDetailNavRoute>() == true ||
            this?.hasRoute<TriedRecipesRoute>() == true ||
            this?.hasRoute<DiaryRoute>() == true ||
            this?.hasRoute<AddDiaryEntryRoute>() == true ||
            this?.hasRoute<AddReviewRoute>() == true ||
            this?.hasRoute<AddTipRoute>() == true ||
            this?.hasRoute<SaveToCollectionDialogRoute>() == true
}

fun NavDestination?.postLoginDestinationForProtectedRoute(): PostLoginDestination {
    return when {
        this?.hasRoute<CreateRecipeRoute>() == true ||
                this?.hasRoute<AdaptRecipeRoute>() == true -> {
            PostLoginDestination.PUBLISH
        }

        this?.hasRoute<SavedCollectionsRoute>() == true ||
                this?.hasRoute<CollectionDetailNavRoute>() == true ||
                this?.hasRoute<SaveToCollectionDialogRoute>() == true -> {
            PostLoginDestination.SAVED
        }

        this?.hasRoute<NotificationsRoute>() == true -> {
            PostLoginDestination.NOTIFICATIONS
        }

        else -> {
            PostLoginDestination.PROFILE
        }
    }
}