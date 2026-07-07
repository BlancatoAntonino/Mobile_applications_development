package it.polito.mad.cookbookcommunity.ui.app

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import it.polito.mad.cookbookcommunity.data.AppContainer
import it.polito.mad.cookbookcommunity.navigation.AdaptRecipeRoute
import it.polito.mad.cookbookcommunity.navigation.AppNavHost
import it.polito.mad.cookbookcommunity.navigation.AppNavigation
import it.polito.mad.cookbookcommunity.navigation.AuthEntryPhotoRoleRoute
import it.polito.mad.cookbookcommunity.navigation.AuthEntryPreferencesRoute
import it.polito.mad.cookbookcommunity.navigation.AuthEntryRoute
import it.polito.mad.cookbookcommunity.navigation.CreateRecipeRoute
import it.polito.mad.cookbookcommunity.navigation.HomeRoute
import it.polito.mad.cookbookcommunity.navigation.OwnProfileRoute
import it.polito.mad.cookbookcommunity.navigation.PostLoginDestination
import it.polito.mad.cookbookcommunity.navigation.RecipesListRoute
import it.polito.mad.cookbookcommunity.navigation.SavedCollectionsRoute
import it.polito.mad.cookbookcommunity.navigation.SignUpAccountRoute
import it.polito.mad.cookbookcommunity.navigation.isProtectedRoute
import it.polito.mad.cookbookcommunity.navigation.postLoginDestinationForProtectedRoute
import it.polito.mad.cookbookcommunity.session.SessionManager
import it.polito.mad.cookbookcommunity.viewmodel.NewRecipeStep
import kotlinx.coroutines.launch

@Composable
fun CookBookCommunityRoot(
    appContainer: AppContainer,
    modifier: Modifier = Modifier,
) {
    val authRepository = appContainer.authRepository

    val navController = rememberNavController()
    val appNavigation = remember(navController) {
        AppNavigation(navController)
    }

    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDest = currentBackStack?.destination

    val currentAuthUser by authRepository.currentUser.collectAsStateWithLifecycle()
    val isUserAuthenticated = currentAuthUser != null
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    fun showFeedback(message: String) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    val bottomBarRoutes = listOf(
        HomeRoute,
        RecipesListRoute,
        CreateRecipeRoute,
        SavedCollectionsRoute,
        OwnProfileRoute
    )

    val isBottomBarScreen = bottomBarRoutes.any { route ->
        currentDest?.hasRoute(route::class) == true
    }

    var newRecipeCurrentStep by remember { mutableStateOf(NewRecipeStep.BASIC_INFO) }
    var newRecipeHasDraftData by remember { mutableStateOf(false) }
    var isProfileEditing by remember { mutableStateOf(false) }
    var postLoginDestination by remember {
        mutableStateOf<PostLoginDestination?>(null)
    }

    val isCreateRecipeRoute = currentDest?.hasRoute<CreateRecipeRoute>() == true
    val isAdaptRecipeRoute = currentDest?.hasRoute<AdaptRecipeRoute>() == true
    val isNewRecipeWizardRoute = isCreateRecipeRoute || isAdaptRecipeRoute

    val isBasicInfoStep =
        isNewRecipeWizardRoute &&
                newRecipeCurrentStep == NewRecipeStep.BASIC_INFO
    val isOwnProfileRoute = currentDest?.hasRoute<OwnProfileRoute>() == true

    val isSignInRoute =
        currentDest?.hasRoute<SignUpAccountRoute>() == true

    val isAuthEntryRoute =
        currentDest?.hasRoute<AuthEntryRoute>() == true ||
                currentDest?.hasRoute<AuthEntryPhotoRoleRoute>() == true ||
                currentDest?.hasRoute<AuthEntryPreferencesRoute>() == true

    var isNavigating: (() -> Unit)? by remember { mutableStateOf(null) }
    var resetNewRecipeForm: (() -> Unit) by remember { mutableStateOf({}) }

    if (isNavigating != null) {
        AlertDialog(
            onDismissRequest = { isNavigating = null },
            title = { Text("Discard recipe draft?") },
            text = { Text("Leaving now will discard the data you've entered for this recipe.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val nav = isNavigating
                        isNavigating = null
                        resetNewRecipeForm()
                        nav?.invoke()
                    }
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { isNavigating = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    fun checkNavigateFromNewRecipe(navigate: () -> Unit) {
        if (isNewRecipeWizardRoute && newRecipeHasDraftData) {
            isNavigating = navigate
        } else {
            navigate()
        }
    }

    fun navigateToLoginFor(destination: PostLoginDestination) {
        postLoginDestination = destination
        appNavigation.navigateToLogin()
    }

    fun navigateAfterSuccessfulLogin() {
        val destination = postLoginDestination
        postLoginDestination = null
        appNavigation.navigateAfterSuccessfulLogin(destination)
    }

    fun navigateIfLoggedIn(
        destinationAfterLogin: PostLoginDestination,
        navigateWhenLoggedIn: () -> Unit
    ) {
        if (isUserAuthenticated) {
            navigateWhenLoggedIn()
        } else {
            showFeedback("Sign in to continue.")
            navigateToLoginFor(destinationAfterLogin)
        }
    }

    LaunchedEffect(isUserAuthenticated, currentDest) {
        if (!isUserAuthenticated && currentDest.isProtectedRoute()) {
            showFeedback("Sign in to continue.")
            navigateToLoginFor(
                currentDest.postLoginDestinationForProtectedRoute()
            )
        }
    }

    val showBottomBar =
        (isBottomBarScreen || isSignInRoute) &&
                (!isCreateRecipeRoute || isBasicInfoStep) &&
                !(isOwnProfileRoute && isProfileEditing)

    AppScaffold(
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        showBottomBar = showBottomBar,
        isHomeSelected = currentDest?.hasRoute<HomeRoute>() == true,
        isExploreSelected = currentDest?.hasRoute<RecipesListRoute>() == true,
        isPublishSelected =
            currentDest?.hasRoute<CreateRecipeRoute>() == true ||
                    (isSignInRoute && postLoginDestination == PostLoginDestination.PUBLISH),
        isSavedSelected =
            currentDest?.hasRoute<SavedCollectionsRoute>() == true ||
                    (isSignInRoute && postLoginDestination == PostLoginDestination.SAVED),
        isProfileSelected =
            currentDest?.hasRoute<OwnProfileRoute>() == true ||
                    (isSignInRoute &&
                            (postLoginDestination == PostLoginDestination.PROFILE ||
                                    postLoginDestination == null)) ||
                    isAuthEntryRoute,
        onHomeClick = {
            checkNavigateFromNewRecipe {
                postLoginDestination = null
                appNavigation.navigateToHome()
            }
        },
        onExploreClick = {
            checkNavigateFromNewRecipe {
                postLoginDestination = null
                appNavigation.navigateToExplore()
            }
        },
        onPublishClick = {
            checkNavigateFromNewRecipe {
                navigateIfLoggedIn(
                    destinationAfterLogin = PostLoginDestination.PUBLISH,
                    navigateWhenLoggedIn = {
                        appNavigation.navigateToCreateRecipe()
                    }
                )
            }
        },
        onSavedClick = {
            checkNavigateFromNewRecipe {
                navigateIfLoggedIn(
                    destinationAfterLogin = PostLoginDestination.SAVED,
                    navigateWhenLoggedIn = {
                        appNavigation.navigateToSavedCollections()
                    }
                )
            }
        },
        onProfileClick = {
            checkNavigateFromNewRecipe {
                navigateIfLoggedIn(
                    destinationAfterLogin = PostLoginDestination.PROFILE,
                    navigateWhenLoggedIn = {
                        appNavigation.navigateToOwnProfile()
                    }
                )
            }
        },
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            appNavigation = appNavigation,
            appContainer = appContainer,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            navigateIfLoggedIn = { destinationAfterLogin, navigateWhenLoggedIn ->
                navigateIfLoggedIn(
                    destinationAfterLogin = destinationAfterLogin,
                    navigateWhenLoggedIn = navigateWhenLoggedIn
                )
            },
            navigateAfterSuccessfulLogin = {
                navigateAfterSuccessfulLogin()
            },
            onNewRecipeStepChanged = { step ->
                newRecipeCurrentStep = step
            },
            onNewRecipeFormDataChanged = { hasDraftData ->
                newRecipeHasDraftData = hasDraftData
            },
            onRegisterNewRecipeReset = { reset ->
                resetNewRecipeForm = reset
            },
            onProfileEditingChanged = { isEditing ->
                isProfileEditing = isEditing
            },
            onSignOutClick = {
                coroutineScope.launch {
                    SessionManager.signOut()
                    postLoginDestination = PostLoginDestination.PROFILE
                    appNavigation.navigateToLogin()
                }
            },
            onFeedback = { message ->
                showFeedback(message)
            }
        )
    }
}