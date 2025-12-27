package com.ltcn272.finny.presentation.features.chat

import android.Manifest
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ltcn272.finny.R
import com.ltcn272.finny.data.SettingDataStore
import com.ltcn272.finny.domain.model.Chat
import com.ltcn272.finny.domain.repository.ChatRepository
import com.ltcn272.finny.domain.util.AppResult
import com.ltcn272.finny.domain.util.ErrorType
import com.ltcn272.finny.presentation.features.snackbar.SnackbarManager
import com.ltcn272.finny.presentation.features.snackbar.TopSnackbarType
import com.ltcn272.finny.util.PermissionUtils
import com.ltcn272.finny.util.SpeechRecognizerManager
import com.ltcn272.finny.util.SpeechState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

data class ChatUiState(
    val isAiTyping: Boolean = false,
    val pendingMessages: List<Chat> = emptyList(),
    val refreshTrigger: Int = 0,
    val currencyCode: String = "VND",
    val isListening: Boolean = false,
    val recognizedText: String = "",
    val hasRecordPermission: Boolean = false
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val application: Application,
    private val chatRepository: ChatRepository,
    private val settingDataStore: SettingDataStore
) : ViewModel() {

    private val speechRecognizerManager = SpeechRecognizerManager()

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent = _errorEvent.asSharedFlow()

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
                        _errorEvent.emit(state.message)
                        _uiState.update {
                            it.copy(
                                isListening = false,
                                recognizedText = ""
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

    fun sendMessage(text: String, snackbarManager: SnackbarManager) {
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
                            pendingMessages = it.pendingMessages.filterNot { msg -> msg.id == pendingMessage.id }
                        )
                    }
                    snackbarManager.showMessage(mapErrorToString(result.errorType), TopSnackbarType.ERROR)
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
        speechRecognizerManager.startListening(application)
    }

    fun stopListening() {
        speechRecognizerManager.stopListening()
    }

    fun onPermissionResult(isGranted: Boolean, snackbarManager: SnackbarManager) {
        _uiState.update { it.copy(hasRecordPermission = isGranted) }
        if (isGranted) {
            startListening()
        } else {
            snackbarManager.showMessage("Cần quyền ghi âm để sử dụng tính năng này", TopSnackbarType.WARNING)
        }
    }

    private fun mapErrorToString(errorType: ErrorType): String {
        return when (errorType) {
            ErrorType.NETWORK -> application.getString(R.string.error_network)
            ErrorType.TIMEOUT -> application.getString(R.string.error_timeout)
            ErrorType.UNAUTHORIZED -> application.getString(R.string.error_unauthorized)
            ErrorType.SERVER_ERROR -> application.getString(R.string.error_server)
            ErrorType.UNKNOWN -> application.getString(R.string.error_unknown)
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizerManager.release()
    }
}
