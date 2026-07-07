package it.polito.mad.cookbookcommunity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.polito.mad.cookbookcommunity.data.notification.NotificationRepository
import it.polito.mad.cookbookcommunity.model.notification.AppNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val notifications: List<AppNotification> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    val unreadCount: Int
        get() = notifications.count { !it.read }
}

class NotificationsViewModel(
    private val userId: String,
    private val repository: NotificationRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        observeNotifications()
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            runCatching {
                repository.markAsRead(notificationId)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(errorMessage = error.message ?: "Unable to update notification.")
                }
            }
        }
    }

    fun markAllAsRead() {
        val unreadNotifications = _uiState.value.notifications.filter { !it.read }
        if (unreadNotifications.isEmpty()) return

        viewModelScope.launch {
            runCatching {
                unreadNotifications.forEach { notification ->
                    repository.markAsRead(notification.id)
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(errorMessage = error.message ?: "Unable to update notifications.")
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            repository
                .observeNotificationsForUser(userId)
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Unable to load notifications."
                        )
                    }
                }
                .collect { notifications ->
                    _uiState.update {
                        it.copy(
                            notifications = notifications.sortedByDescending { notification ->
                                notification.createdAt
                            },
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
        }
    }
}
