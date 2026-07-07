package it.polito.mad.cookbookcommunity.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import it.polito.mad.cookbookcommunity.data.AppContainer
import it.polito.mad.cookbookcommunity.navigation.graphs.authGraph
import it.polito.mad.cookbookcommunity.navigation.graphs.homeGraph
import it.polito.mad.cookbookcommunity.navigation.graphs.notificationsGraph
import it.polito.mad.cookbookcommunity.navigation.graphs.profileGraph
import it.polito.mad.cookbookcommunity.navigation.graphs.recipeGraph
import it.polito.mad.cookbookcommunity.viewmodel.NewRecipeStep

@Composable
fun AppNavHost(
    navController: NavHostController,
    appNavigation: AppNavigation,
    appContainer: AppContainer,
    modifier: Modifier = Modifier,
    navigateIfLoggedIn: (PostLoginDestination, () -> Unit) -> Unit,
    navigateAfterSuccessfulLogin: () -> Unit,
    onNewRecipeStepChanged: (NewRecipeStep) -> Unit,
    onNewRecipeFormDataChanged: (Boolean) -> Unit,
    onRegisterNewRecipeReset: (() -> Unit) -> Unit,
    onProfileEditingChanged: (Boolean) -> Unit,
    onSignOutClick: () -> Unit,
    onFeedback: (String) -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier
    ) {
        homeGraph(
            appNavigation = appNavigation,
            appContainer = appContainer,
            navigateIfLoggedIn = navigateIfLoggedIn,
            onFeedback = onFeedback,
        )

        recipeGraph(
            appNavigation = appNavigation,
            appContainer = appContainer,
            navigateIfLoggedIn = navigateIfLoggedIn,
            onNewRecipeStepChanged = onNewRecipeStepChanged,
            onNewRecipeFormDataChanged = onNewRecipeFormDataChanged,
            onRegisterNewRecipeReset = onRegisterNewRecipeReset,
            onFeedback = onFeedback,
        )

        notificationsGraph(
            appNavigation = appNavigation,
            appContainer = appContainer,
            onFeedback = onFeedback,
        )

        authGraph(
            appNavigation = appNavigation,
            appContainer = appContainer,
            navigateAfterSuccessfulLogin = navigateAfterSuccessfulLogin,
            onFeedback = onFeedback,
        )

        profileGraph(
            appNavigation = appNavigation,
            appContainer = appContainer,
            onProfileEditingChanged = onProfileEditingChanged,
            onSignOutClick = onSignOutClick,
            onFeedback = onFeedback,
        )
    }
}