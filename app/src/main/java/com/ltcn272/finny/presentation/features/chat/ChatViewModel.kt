package com.ltcn272.finny.presentation.features.chat

import android.Manifest
import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ltcn272.finny.R
import com.ltcn272.finny.data.SettingDataStore
import com.ltcn272.finny.domain.model.Chat
import com.ltcn272.finny.domain.repository.ChatRepository
import com.ltcn272.finny.domain.repository.TransactionRepository
import com.ltcn272.finny.domain.util.AppResult
import com.ltcn272.finny.domain.util.ErrorType
import com.ltcn272.finny.presentation.features.snackbar.SnackbarManager
import com.ltcn272.finny.presentation.features.snackbar.TopSnackbarType
import com.ltcn272.finny.util.PermissionUtils
import com.ltcn272.finny.util.SpeechRecognizerManager
import com.ltcn272.finny.util.SpeechState
import com.ltcn272.finny.util.createFileFromUri
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

data class ChatUiState(
    val isAiTyping: Boolean = false,
    val sessionMessages: List<Chat> = emptyList(),
    val currencyCode: String = "VND",
    val isListening: Boolean = false,
    val recognizedText: String = "",
    val hasRecordPermission: Boolean = false,
    val isUploadingImage: Boolean = false,
    val showSuggestions: Boolean = true,
    val suggestionPrompts: List<String> = listOf(
        "Hôm nay tôi chi bao nhiêu?",
        "Tổng thu nhập tháng này",
        "Các khoản chi lớn nhất",
        "So sánh chi tiêu tháng này và tháng trước"
    )
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val application: Application,
    private val chatRepository: ChatRepository,
    private val transactionRepository: TransactionRepository,
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

    fun uploadImageAndSend(uri: Uri, snackbarManager: SnackbarManager) {
        viewModelScope.launch {
            hideSuggestions()
            _uiState.update { it.copy(isUploadingImage = true, isAiTyping = true) }

            val tempFile = createFileFromUri(application, uri, snackbarManager)
                ?: run {
                    _uiState.update { it.copy(isUploadingImage = false, isAiTyping = false) }
                    return@launch
                }

            transactionRepository.uploadImage(tempFile).collectLatest { result ->
                when (result) {
                    is AppResult.Success -> {
                        val imageUrl = result.data
                        sendMessage(text = "đây là ảnh về giao dịch của tôi", imageUrl = imageUrl, snackbarManager = snackbarManager, imageUri = uri.toString())
                    }
                    is AppResult.Error -> {
                        snackbarManager.showMessage(mapErrorToString(result.errorType), TopSnackbarType.ERROR)
                        _uiState.update {
                            it.copy(
                                isUploadingImage = false,
                                isAiTyping = false
                            )
                        }
                    }
                    is AppResult.Loading -> { /* Đang tải */ }
                }
            }
            tempFile.delete()
        }
    }

    fun sendMessage(text: String?, imageUrl: String? = null, snackbarManager: SnackbarManager, imageUri: String? = null) {
        if (text.isNullOrBlank() && imageUrl.isNullOrBlank()) return
        if (uiState.value.isAiTyping) return

        hideSuggestions()
        _uiState.update { it.copy(recognizedText = "") }

        val userMessage = Chat(
            id = UUID.randomUUID().toString(),
            isFromUser = true,
            text = text!!,
            image = imageUri ?: imageUrl,
            cards = emptyList(),
            timestamp = LocalDateTime.now()
        )
        _uiState.update {
            it.copy(
                isAiTyping = true,
                isUploadingImage = false,
                sessionMessages = listOf(userMessage) + it.sessionMessages
            )
        }

        viewModelScope.launch {
            when (val result = chatRepository.sendMessageAndGetResponse(text, imageUrl)) {
                is AppResult.Success -> {
                    val aiMessage = result.data
                    _uiState.update {
                        it.copy(
                            isAiTyping = false,
                            sessionMessages = listOf(aiMessage) + it.sessionMessages
                        )
                    }
                }
                is AppResult.Error -> {
                    snackbarManager.showMessage(mapErrorToString(result.errorType), TopSnackbarType.ERROR)
                    _uiState.update {
                        it.copy(
                            isAiTyping = false,
                            sessionMessages = it.sessionMessages.filterNot { msg -> msg.id == userMessage.id },
                            recognizedText = userMessage.text
                        )
                    }
                }
                AppResult.Loading -> {}
            }
        }
    }

    fun onRecognizedTextChanged(text: String) {
        if (text.isNotEmpty() && uiState.value.showSuggestions) {
            hideSuggestions()
        }
        _uiState.update { it.copy(recognizedText = text) }
    }

    fun startListening() {
        if (uiState.value.showSuggestions) {
            hideSuggestions()
        }
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

    fun hideSuggestions() {
        _uiState.update { it.copy(showSuggestions = false) }
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
