package com.ltcn272.finny.presentation.features.bank_notification

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ltcn272.finny.R
import com.ltcn272.finny.data.SettingDataStore
import com.ltcn272.finny.domain.repository.ChatRepository
import com.ltcn272.finny.domain.util.AppResult
import com.ltcn272.finny.domain.util.ErrorType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BankNotificationInboxUiState(
    val isLoading: Boolean = true,
    val notifications: List<PendingBankNotification> = emptyList()
)

sealed class InboxEvent {
    data class ShowSnackbar(val message: String, val isError: Boolean) : InboxEvent()
}

@HiltViewModel
class BankNotificationInboxViewModel @Inject constructor(
    private val app: Application,
    private val settingDataStore: SettingDataStore,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BankNotificationInboxUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<InboxEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadNotificationsOneTime()
    }

    private fun loadNotificationsOneTime() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val inboxString = settingDataStore.bankNotificationInboxFlow.first()
            val parsedNotifications = parseInboxString(inboxString)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    notifications = parsedNotifications
                )
            }
        }
    }

    fun sendToAI(notification: PendingBankNotification) {
        if (notification.isLoading || notification.cardResult != null) return

        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    notifications = currentState.notifications.map {
                        if (it.id == notification.id) it.copy(isLoading = true) else it
                    }
                )
            }

            val prompt = "Finny, tạo giao dịch nội dung là ${notification.text}"

            // Sửa đổi lời gọi hàm cho phù hợp với Repository mới
            when (chatRepository.sendMessageAndGetResponse(text = prompt, imageUrl = null)) {
                is AppResult.Success -> {
                    // Vì API mới không trả về card trực tiếp, chúng ta sẽ giả định thành công
                    // và thông báo cho người dùng kiểm tra lại ở màn hình Chat hoặc Giao dịch
                    _eventFlow.emit(InboxEvent.ShowSnackbar(app.getString(R.string.notification_sent_to_ai), false))
                    // Xóa thông báo khỏi DataStore để không hiển thị lại
                    removeNotificationFromDataStore(notification, removeFromUi = true)
                }
                is AppResult.Error -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            notifications = currentState.notifications.map {
                                if (it.id == notification.id) it.copy(isLoading = false) else it
                            }
                        )
                    }
                    _eventFlow.emit(InboxEvent.ShowSnackbar(app.getString(R.string.ai_could_not_recognize_transaction), true))
                }
                is AppResult.Loading -> {}
            }
        }
    }

    fun deleteNotification(notification: PendingBankNotification) {
        viewModelScope.launch {
            removeNotificationFromDataStore(notification, removeFromUi = true)
        }
    }

    private suspend fun removeNotificationFromDataStore(notificationToRemove: PendingBankNotification, removeFromUi: Boolean) {
        val currentInboxString = settingDataStore.bankNotificationInboxFlow.first()
        val notifications = currentInboxString.split("|||").toMutableList()
        notifications.remove(notificationToRemove.originalString)
        val newInboxString = notifications.joinToString("|||")
        settingDataStore.saveBankNotificationInbox(newInboxString)

        if (removeFromUi) {
            _uiState.update {
                it.copy(notifications = it.notifications.filterNot { n -> n.id == notificationToRemove.id })
            }
            _eventFlow.emit(InboxEvent.ShowSnackbar(app.getString(R.string.notification_deleted), false))
        }
    }

    private fun parseInboxString(inbox: String): List<PendingBankNotification> {
        if (inbox.isBlank()) return emptyList()
        return inbox.split("|||").mapNotNull { rawString ->
            val parts = rawString.split(";;;")
            if (parts.size == 3) {
                PendingBankNotification(
                    appName = parts[0],
                    title = parts[1],
                    text = parts[2],
                    originalString = rawString
                )
            } else {
                null
            }
        }.reversed()
    }

    private fun mapErrorToString(errorType: ErrorType): String {
        return when (errorType) {
            ErrorType.NETWORK -> app.getString(R.string.error_network)
            ErrorType.TIMEOUT -> app.getString(R.string.error_timeout)
            ErrorType.UNAUTHORIZED -> app.getString(R.string.error_unauthorized)
            ErrorType.SERVER_ERROR -> app.getString(R.string.error_server)
            ErrorType.UNKNOWN -> app.getString(R.string.error_unknown)
        }
    }
}
