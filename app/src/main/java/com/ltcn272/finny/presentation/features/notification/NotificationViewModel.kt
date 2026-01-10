package com.ltcn272.finny.presentation.features.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.ltcn272.finny.domain.model.Notification
import com.ltcn272.finny.domain.repository.NotificationRepository
import com.ltcn272.finny.domain.util.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class NotificationTabState { ALL, UNREAD }

data class NotificationUiState(
    val selectedTab: NotificationTabState = NotificationTabState.ALL,
    val notificationToShow: Notification? = null,
    val isMarkingAsRead: Boolean = false,
    val readIds: Set<String> = emptySet(),
    val unreadCount: Int = 0
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    private val originalNotificationsFlow = notificationRepository.getNotifications().cachedIn(viewModelScope)

    val notificationsPagingFlow: Flow<PagingData<Notification>> = combine(
        originalNotificationsFlow,
        _uiState
    ) { pagingData, state ->
        var processedPagingData = pagingData.map { notification ->
            if (state.readIds.contains(notification.id)) {
                notification.copy(isRead = true)
            } else {
                notification
            }
        }

        if (state.selectedTab == NotificationTabState.UNREAD) {
            processedPagingData = processedPagingData.filter { notification ->
                val isOpening = notification.id == state.notificationToShow?.id
                isOpening || (!notification.isRead && !state.readIds.contains(notification.id))
            }
        }
        processedPagingData
    }.cachedIn(viewModelScope)

    init {
        refreshUnreadCount()
    }

    private fun refreshUnreadCount() {
        viewModelScope.launch {
            when (val result = notificationRepository.getUnreadCount()) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(unreadCount = result.data) }
                }
                else -> {}
            }
        }
    }

    fun onTabSelected(tab: NotificationTabState) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun showNotification(notification: Notification) {
        _uiState.update { it.copy(notificationToShow = notification) }
        if (!notification.isRead && !_uiState.value.readIds.contains(notification.id)) {
            markNotificationAsRead(notification.id)
        }
    }

    fun dismissNotification() {
        _uiState.update { it.copy(notificationToShow = null, isMarkingAsRead = false) }
    }

    private fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isMarkingAsRead = true) }
            val result = notificationRepository.markAsRead(notificationId)
            if (result is AppResult.Success) {
                _uiState.update { currentState ->
                    currentState.copy(
                        readIds = currentState.readIds + notificationId,
                        isMarkingAsRead = false,
                        notificationToShow = currentState.notificationToShow?.let {
                            if (it.id == notificationId) it.copy(isRead = true) else it
                        },
                        unreadCount = (currentState.unreadCount - 1).coerceAtLeast(0)
                    )
                }
                refreshUnreadCount()
            } else {
                _uiState.update { it.copy(isMarkingAsRead = false) }
            }
        }
    }
}
