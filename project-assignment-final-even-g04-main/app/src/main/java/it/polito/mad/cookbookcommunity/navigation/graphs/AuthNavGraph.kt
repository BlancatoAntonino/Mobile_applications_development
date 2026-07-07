package it.polito.mad.cookbookcommunity.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import it.polito.mad.cookbookcommunity.data.AppContainer
import it.polito.mad.cookbookcommunity.navigation.AppNavigation
import it.polito.mad.cookbookcommunity.navigation.AuthEntryPhotoRoleRoute
import it.polito.mad.cookbookcommunity.navigation.AuthEntryPreferencesRoute
import it.polito.mad.cookbookcommunity.navigation.AuthEntryRoute
import it.polito.mad.cookbookcommunity.navigation.AuthGraphRoute
import it.polito.mad.cookbookcommunity.navigation.SignUpAccountRoute
import it.polito.mad.cookbookcommunity.ui.authentication.AuthEntryPhotoRoleScreen
import it.polito.mad.cookbookcommunity.ui.authentication.AuthEntryPreferencesScreen
import it.polito.mad.cookbookcommunity.ui.authentication.AuthEntryScreen
import it.polito.mad.cookbookcommunity.ui.authentication.SignUpAccountScreenRoute

internal fun NavGraphBuilder.authGraph(
    appNavigation: AppNavigation,
    appContainer: AppContainer,
    navigateAfterSuccessfulLogin: () -> Unit,
    onFeedback: (String) -> Unit,
) {
    val authRepository = appContainer.authRepository
    val userProfileRepository = appContainer.userProfileRepository

    navigation<AuthGraphRoute>(
        startDestination = SignUpAccountRoute
    ) {
        composable<SignUpAccountRoute> {
            SignUpAccountScreenRoute(
                authRepository = authRepository,
                userRepository = userProfileRepository,
                onLoginSuccess = {
                    onFeedback("Login successful.")
                    navigateAfterSuccessfulLogin()
                },
                onCreateAccount = {
                    appNavigation.navigateToAuthEntry()
                },
                onFeedback = { message ->
                    onFeedback(message)
                }
            )
        }

        composable<AuthEntryRoute> {
            AuthEntryScreen(
                onNext = {
                    appNavigation.navigateToAuthPhotoRole()
                },
                onBack = {
                    appNavigation.navigateBack()
                }
            )
        }

        composable<AuthEntryPhotoRoleRoute> {
            AuthEntryPhotoRoleScreen(
                onNext = {
                    appNavigation.navigateToAuthPreferences()
                },
                onBack = {
                    appNavigation.navigateBack()
                }
            )
        }

        composable<AuthEntryPreferencesRoute> {
            AuthEntryPreferencesScreen(
                onCreationSuccess = {
                    appNavigation.navigateToSignUpAfterAccountCreation()
                },
                onBack = {
                    appNavigation.navigateBack()
                }
            )
        }
    }
}