package com.ltcn272.finny.presentation.features.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.ltcn272.finny.domain.model.Notification
import com.ltcn272.finny.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

enum class NotificationTabState { ALL, UNREAD }

data class NotificationUiState(
    val selectedTab: NotificationTabState = NotificationTabState.ALL
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    val notificationsPagingFlow: Flow<PagingData<Notification>> = _uiState
        .flatMapLatest { state ->
            notificationRepository.getNotifications()
                .map { pagingData ->
                    if (state.selectedTab == NotificationTabState.UNREAD) {
                        pagingData.filter { !it.isRead }
                    } else {
                        pagingData
                    }
                }
        }.cachedIn(viewModelScope)

    fun onTabSelected(tab: NotificationTabState) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
}
