package com.ltcn272.finny.presentation.features.chat

import android.Manifest
import android.app.Application
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ltcn272.finny.data.SettingDataStore
import com.ltcn272.finny.domain.model.Chat
import com.ltcn272.finny.domain.repository.ChatRepository
import com.ltcn272.finny.domain.util.AppResult
import com.ltcn272.finny.domain.util.ErrorType
import com.ltcn272.finny.presentation.common.ui.TopSnackbarType
import com.ltcn272.finny.util.PermissionUtils
import com.ltcn272.finny.util.SpeechRecognizerManager
import com.ltcn272.finny.util.SpeechState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

data class ChatSnackbarState(
    val visible: Boolean = false,
    val errorType: ErrorType? = null,
    val message: String? = null,
    val type: TopSnackbarType = TopSnackbarType.INFO
)

data class ChatUiState(
    val isAiTyping: Boolean = false,
    val pendingMessages: List<Chat> = emptyList(),
    val refreshTrigger: Int = 0,
    val snackbarState: ChatSnackbarState = ChatSnackbarState(),
    val currencyCode: String = "VND",
    val isListening: Boolean = false,
    val recognizedText: String = "",
    val hasRecordPermission: Boolean = false
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val application: Application, // 'application' context được Hilt cung cấp sẵn
    private val chatRepository: ChatRepository,
    private val settingDataStore: SettingDataStore
) : ViewModel() {

    // --- THAY ĐỔI: KHỞI TẠO THỦ CÔNG ---
    private val speechRecognizerManager = SpeechRecognizerManager()

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    val messages: Flow<PagingData<Chat>> = chatRepository.getChatMessages().cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            settingDataStore.getSelectedCurrency.collect { currency ->
                _uiState.update { it.copy(currencyCode = currency) }
            }
        }

        _uiState.update { it.copy(hasRecordPermission = PermissionUtils.hasPermission(application, Manifest.permission.RECORD_AUDIO)) }

        viewModelScope.launch {
            speechRecognizerManager.speechState.collect { state ->
                when (state) {
                    is SpeechState.Listening -> {
                        _uiState.update {
                            it.copy(
                                isListening = !state.isEndOfSpeech,
                                recognizedText = state.partialText
                            )
                        }
                    }
                    is SpeechState.Success -> {
                        _uiState.update { it.copy(isListening = false, recognizedText = state.text) }
                        speechRecognizerManager.resetState()
                    }
                    is SpeechState.Error -> {
                        _uiState.update {
                            it.copy(
                                isListening = false,
                                recognizedText = "",
                                snackbarState = ChatSnackbarState(visible = true, message = state.message, type = TopSnackbarType.ERROR)
                            )
                        }
                        speechRecognizerManager.resetState()
                    }
                    SpeechState.Idle -> {
                        _uiState.update { it.copy(isListening = false) }
                    }
                }
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || uiState.value.isAiTyping) return

        _uiState.update { it.copy(recognizedText = "") }

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
                    isAiTyping = true,
                    pendingMessages = listOf(pendingMessage) + it.pendingMessages
                )
            }

            when (val result = chatRepository.sendMessageAndGetResponse(text)) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            pendingMessages = emptyList(),
                            isAiTyping = false,
                            refreshTrigger = it.refreshTrigger + 1
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isAiTyping = false,
                            pendingMessages = it.pendingMessages.filterNot { msg -> msg.id == pendingMessage.id },
                            snackbarState = ChatSnackbarState(true, result.errorType, type = TopSnackbarType.ERROR)
                        )
                    }
                }

                AppResult.Loading -> {}
            }
        }
    }

    fun onRecognizedTextChanged(text: String) {
        _uiState.update { it.copy(recognizedText = text) }
    }

    fun startListening() {
        _uiState.update { it.copy(recognizedText = "") }
        // --- THAY ĐỔI: TRUYỀN CONTEXT VÀO ---
        speechRecognizerManager.startListening(application)
    }

    fun stopListening() {
        speechRecognizerManager.stopListening()
    }

    fun onPermissionResult(isGranted: Boolean) {
        _uiState.update { it.copy(hasRecordPermission = isGranted) }
        if (isGranted) {
            startListening()
        } else {
            _uiState.update { it.copy(snackbarState = ChatSnackbarState(true, message = "Cần quyền ghi âm để sử dụng tính năng này", type = TopSnackbarType.WARNING)) }
        }
    }

    fun onSnackbarDismissed() {
        _uiState.update { it.copy(snackbarState = it.snackbarState.copy(visible = false)) }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizerManager.release()
    }
}
