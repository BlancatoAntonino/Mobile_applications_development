package it.polito.mad.cookbookcommunity.ui.authentication

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import it.polito.mad.cookbookcommunity.data.auth.AuthRepository
import it.polito.mad.cookbookcommunity.data.user.UserRepository
import it.polito.mad.cookbookcommunity.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun SignUpAccountScreenRoute(
    authRepository: AuthRepository,
    userRepository: UserRepository,
    onLoginSuccess: () -> Unit,
    onCreateAccount: () -> Unit,
    onFeedback: (String) -> Unit = {}
) {
    val viewModel: AuthViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                AuthViewModel(
                    authRepository = authRepository,
                    userRepository = userRepository
                )
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.isSignedIn) {
        if (uiState.isSignedIn) {
            viewModel.consumeSignInEvent()
            onLoginSuccess()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let(onFeedback)
    }

    SignUpAccountScreen(
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        onDismissError = viewModel::clearError,
        onCreateAccount = onCreateAccount,
        onGoogleSignInClick = {
            coroutineScope.launch {
                viewModel.clearError()

                requestGoogleIdToken(context)
                    .onSuccess { idToken ->
                        viewModel.signInWithGoogleIdToken(idToken)
                    }
                    .onFailure { throwable ->
                        if (!throwable.isGoogleSignInUserCancellation()) {
                            viewModel.onGoogleCredentialError(
                                throwable.message ?: "Unable to retrieve Google credentials."
                            )
                        }
                    }
            }
        }
    )
}
