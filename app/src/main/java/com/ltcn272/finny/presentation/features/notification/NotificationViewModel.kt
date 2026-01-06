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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class NotificationTabState { ALL, UNREAD }

data class NotificationUiState(
    val selectedTab: NotificationTabState = NotificationTabState.ALL,
    val notificationToShow: Notification? = null,
    val isMarkingAsRead: Boolean = false
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    private val _notificationsPagingFlow = MutableStateFlow<PagingData<Notification>>(PagingData.empty())
    val notificationsPagingFlow: StateFlow<PagingData<Notification>> = _notificationsPagingFlow.asStateFlow()

    init {
        notificationRepository.getNotifications()
            .cachedIn(viewModelScope)
            .onEach { pagingData ->
                val currentTab = _uiState.value.selectedTab
                val filteredData = if (currentTab == NotificationTabState.UNREAD) {
                    pagingData.filter { !it.isRead }
                } else {
                    pagingData
                }
                _notificationsPagingFlow.value = filteredData
            }
            .launchIn(viewModelScope)
    }

    fun onTabSelected(tab: NotificationTabState) {
        _uiState.update { it.copy(selectedTab = tab) }
        // Tải lại PagingData để áp dụng bộ lọc mới
        viewModelScope.launch {
            notificationRepository.getNotifications().cachedIn(viewModelScope).collect { pagingData ->
                val filteredData = if (tab == NotificationTabState.UNREAD) {
                    pagingData.filter { !it.isRead }
                } else {
                    pagingData
                }
                _notificationsPagingFlow.value = filteredData
            }
        }
    }

    fun showNotification(notification: Notification) {
        _uiState.update { it.copy(notificationToShow = notification, isMarkingAsRead = !notification.isRead) }
        if (!notification.isRead) {
            markNotificationAsRead(notification.id)
        }
    }

    fun dismissNotification() {
        _uiState.update { it.copy(notificationToShow = null, isMarkingAsRead = false) }
    }

    private fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            val result = notificationRepository.markAsRead(notificationId)
            if (result is AppResult.Success) {
                val updatedPagingData = _notificationsPagingFlow.value.map { notification ->
                    if (notification.id == notificationId) {
                        notification.copy(isRead = true)
                    } else {
                        notification
                    }
                }
                _notificationsPagingFlow.value = updatedPagingData

                _uiState.update { currentState ->
                    if (currentState.notificationToShow?.id == notificationId) {
                        currentState.copy(
                            notificationToShow = currentState.notificationToShow.copy(isRead = true),
                            isMarkingAsRead = false
                        )
                    } else {
                        currentState
                    }
                }
            }
        }
    }
}
