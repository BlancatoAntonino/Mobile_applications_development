package it.polito.mad.cookbookcommunity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.polito.mad.cookbookcommunity.data.auth.AuthRepository
import it.polito.mad.cookbookcommunity.data.user.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSignedIn: Boolean = false,
    val isSignedOut: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signInWithGoogleIdToken(idToken: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            runCatching {
                val authUser = authRepository
                    .signInWithGoogle(idToken)
                    .getOrThrow()

                userRepository.ensureUserProfile(authUser)

                authUser
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSignedIn = true,
                        errorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSignedIn = false,
                        errorMessage = throwable.message ?: "Google sign-in failed."
                    )
                }
            }
        }
    }

    fun onGoogleCredentialError(message: String) {
        _uiState.update {
            it.copy(
                isLoading = false,
                isSignedIn = false,
                errorMessage = message
            )
        }
    }

    fun consumeSignInEvent() {
        _uiState.update {
            it.copy(isSignedIn = false)
        }
    }

    fun clearError() {
        _uiState.update {
            it.copy(errorMessage = null)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            runCatching {
                authRepository.signOut()
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSignedIn = false,
                        isSignedOut = true,
                        errorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Sign-out failed."
                    )
                }
            }
        }
    }

    fun consumeSignOutEvent() {
        _uiState.update {
            it.copy(isSignedOut = false)
        }
    }
}