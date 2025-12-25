package com.ltcn272.finny.presentation.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.ltcn272.finny.data.SettingDataStore
import com.ltcn272.finny.domain.model.Chat
import com.ltcn272.finny.domain.repository.ChatRepository
import com.ltcn272.finny.domain.util.AppResult
import com.ltcn272.finny.domain.util.ErrorType
import com.ltcn272.finny.presentation.common.ui.TopSnackbarType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

data class ChatSnackbarState(
    val visible: Boolean = false,
    val errorType: ErrorType? = null,
    val type: TopSnackbarType = TopSnackbarType.INFO
)

data class ChatUiState(
    val isAiTyping: Boolean = false,
    val pendingMessages: List<Chat> = emptyList(),
    val refreshTrigger: Int = 0,
    val snackbarState: ChatSnackbarState = ChatSnackbarState(),
    val currencyCode: String = "VND"
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val settingDataStore: SettingDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    val messages = chatRepository.getChatMessages().cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            settingDataStore.getSelectedCurrency.collect { currency ->
                _uiState.update { it.copy(currencyCode = currency) }
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || _uiState.value.isAiTyping) return

        val pendingMessage = Chat(
            id = UUID.randomUUID().toString(),
            isFromUser = true,
            text = text,
            cards = emptyList(),
            timestamp = LocalDateTime.now()
        )

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    pendingMessages = it.pendingMessages + pendingMessage,
                    isAiTyping = true
                )
            }

            val result = chatRepository.sendMessageAndGetResponse(text)

            _uiState.update { it.copy(isAiTyping = false) }

            when (result) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            pendingMessages = it.pendingMessages.filterNot { msg -> msg.id == pendingMessage.id },
                            refreshTrigger = it.refreshTrigger + 1
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            pendingMessages = it.pendingMessages.filterNot { msg -> msg.id == pendingMessage.id },
                            snackbarState = ChatSnackbarState(
                                visible = true,
                                errorType = result.errorType,
                                type = TopSnackbarType.ERROR
                            )
                        )
                    }
                }
                is AppResult.Loading -> {}
            }
        }
    }

    fun onSnackbarDismissed() {
        _uiState.update { it.copy(snackbarState = it.snackbarState.copy(visible = false)) }
    }
}
