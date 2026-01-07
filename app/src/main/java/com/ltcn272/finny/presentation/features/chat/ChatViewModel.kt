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
    val selectedImageUri: Uri? = null,
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

    fun onImageSelected(uri: Uri) {
        _uiState.update { it.copy(selectedImageUri = uri) }
    }

    fun clearSelectedImage() {
        _uiState.update { it.copy(selectedImageUri = null) }
    }

    fun sendMessage(text: String, snackbarManager: SnackbarManager) {
        val imageUri = uiState.value.selectedImageUri
        if (text.isBlank() && imageUri == null) return
        if (uiState.value.isAiTyping) return

        _uiState.update { it.copy(recognizedText = "", selectedImageUri = null) }

        if (imageUri != null) {
            uploadImageAndSendMessage(imageUri, text.ifBlank { "Đây là ảnh về giao dịch của tôi" }, snackbarManager)
        } else {
            sendMessageInternal(text = text, imageUrl = null, imageUriForDisplay = null, snackbarManager = snackbarManager)
        }
    }

    private fun uploadImageAndSendMessage(uri: Uri, text: String, snackbarManager: SnackbarManager) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingImage = true, isAiTyping = true) }

            val userMessage = createUserMessage(text, uri.toString())
            _uiState.update { it.copy(sessionMessages = listOf(userMessage) + it.sessionMessages) }

            val tempFile = createFileFromUri(application, uri, snackbarManager)
                ?: run {
                    _uiState.update { state ->
                        state.copy(
                            isUploadingImage = false, isAiTyping = false,
                            sessionMessages = state.sessionMessages.filterNot { it.id == userMessage.id }
                        )
                    }
                    return@launch
                }

            transactionRepository.uploadImage(tempFile).collectLatest { result ->
                when (result) {
                    is AppResult.Success -> {
                        sendMessageInternal(text, result.data, uri.toString(), snackbarManager, userMessage.id)
                    }
                    is AppResult.Error -> {
                        snackbarManager.showMessage(mapErrorToString(result.errorType), TopSnackbarType.ERROR)
                        _uiState.update { state ->
                            state.copy(
                                isUploadingImage = false, isAiTyping = false,
                                sessionMessages = state.sessionMessages.filterNot { it.id == userMessage.id }
                            )
                        }
                    }
                    is AppResult.Loading -> { }
                }
            }
            tempFile.delete()
        }
    }

    private fun sendMessageInternal(
        text: String?,
        imageUrl: String? = null,
        imageUriForDisplay: String? = null,
        snackbarManager: SnackbarManager,
        existingUserMessageId: String? = null
    ) {
        val userMessage = if (existingUserMessageId == null) {
            val msg = createUserMessage(text!!, imageUriForDisplay)
            _uiState.update {
                it.copy(
                    isAiTyping = true,
                    isUploadingImage = false,
                    sessionMessages = listOf(msg) + it.sessionMessages
                )
            }
            msg
        } else {
            uiState.value.sessionMessages.first { it.id == existingUserMessageId }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAiTyping = true, isUploadingImage = false) }

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
                            recognizedText = userMessage.text,
                            selectedImageUri = if (userMessage.image != null) Uri.parse(userMessage.image) else null
                        )
                    }
                }
                AppResult.Loading -> {}
            }
        }
    }

    private fun createUserMessage(text: String, imageUri: String?): Chat {
        return Chat(
            id = UUID.randomUUID().toString(),
            isFromUser = true,
            text = text,
            image = imageUri,
            cards = emptyList(),
            timestamp = LocalDateTime.now()
        )
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
