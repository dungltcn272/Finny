package com.ltcn272.finny.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

private const val TAG = "SpeechRecognizer"

class SpeechRecognizerManager {

    private var speechRecognizer: SpeechRecognizer? = null

    private val _speechState = MutableStateFlow<SpeechState>(SpeechState.Idle)
    val speechState: StateFlow<SpeechState> = _speechState.asStateFlow()

    private val recognitionListener = object : RecognitionListener {
        // ... (TOÀN BỘ NỘI DUNG CỦA LISTENER GIỮ NGUYÊN)
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d(TAG, "✅ onReadyForSpeech: Dịch vụ đã sẵn sàng, bắt đầu nghe...")
            _speechState.value = SpeechState.Listening(isEndOfSpeech = false, partialText = "")
        }

        override fun onBeginningOfSpeech() {
            Log.d(TAG, "🎤 onBeginningOfSpeech: Đã phát hiện tiếng nói.")
        }

        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            Log.d(TAG, "🏁 onEndOfSpeech: Người dùng đã ngừng nói.")
            val currentPartialText = (_speechState.value as? SpeechState.Listening)?.partialText ?: ""
            _speechState.value = SpeechState.Listening(isEndOfSpeech = true, partialText = currentPartialText)
        }

        override fun onError(error: Int) {
            val errorText = when(error) {
                SpeechRecognizer.ERROR_AUDIO -> "Lỗi âm thanh"
                SpeechRecognizer.ERROR_CLIENT -> "Lỗi phía client"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Không đủ quyền"
                SpeechRecognizer.ERROR_NETWORK -> "Lỗi mạng"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Hết thời gian chờ mạng"
                SpeechRecognizer.ERROR_NO_MATCH -> "Không nhận dạng được"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Bộ nhận dạng đang bận"
                SpeechRecognizer.ERROR_SERVER -> "Lỗi từ server"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Hết thời gian nói"
                else -> "Lỗi không xác định"
            }
            Log.e(TAG, "❌ onError: $error - $errorText")

            if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                _speechState.value = SpeechState.Success("")
                return
            }
            _speechState.value = SpeechState.Error(errorText)
        }

        override fun onResults(results: Bundle?) {
            val recognizedText = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull() ?: ""
            Log.d(TAG, "👍 onResults: Kết quả cuối cùng = '$recognizedText'")
            _speechState.value = SpeechState.Success(recognizedText)
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val partialText = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull() ?: ""
            if (partialText.isNotBlank()) {
                Log.d(TAG, "💬 onPartialResults: Kết quả từng phần = '$partialText'")
                _speechState.value = SpeechState.Listening(isEndOfSpeech = false, partialText = partialText)
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    // --- SỬA LẠI HÀM NÀY ĐỂ NHẬN CONTEXT ---
    fun startListening(context: Context) {
        // --- THÊM ĐOẠN KIỂM TRA CHỦ ĐỘNG ---
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.e(TAG, "❌ Speech Recognition không khả dụng trên thiết bị")
            _speechState.value = SpeechState.Error(
                "Thiết bị chưa bật dịch vụ nhập giọng nói. Vui lòng kiểm tra cài đặt Google."
            )
            return
        }
        // --- KẾT THÚC THÊM ---

        Log.d(TAG, "▶️ startListening: Yêu cầu bắt đầu nghe...")
        try {
            speechRecognizer?.destroy()
            Log.d(TAG, "   - Hủy bỏ recognizer cũ (nếu có).")

            // Sử dụng context được truyền vào
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(recognitionListener)
            }
            Log.d(TAG, "   - Tạo mới SpeechRecognizer thành công.")

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                // Sử dụng context được truyền vào
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            }

            speechRecognizer?.startListening(intent)
            Log.d(TAG, "   - Lệnh startListening() đã được gọi.")

        } catch (e: Exception) {
            val errorMessage = "Dịch vụ giọng nói không có sẵn trên thiết bị này."
            Log.e(TAG, "💥 Lỗi nghiêm trọng khi khởi tạo: $errorMessage", e)
            _speechState.value = SpeechState.Error(errorMessage)
            e.printStackTrace()
        }
    }


    fun stopListening() {
        Log.d(TAG, "⏹️ stopListening: Yêu cầu dừng nghe...")
        try {
            speechRecognizer?.stopListening()
            Log.d(TAG, "   - Lệnh stopListening() đã được gọi.")
        } catch (e: Exception) {
            Log.e(TAG, "   - Lỗi khi gọi stopListening", e)
            e.printStackTrace()
        }
    }

    fun resetState() {
        _speechState.value = SpeechState.Idle
    }

    fun release() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

sealed class SpeechState {
    data object Idle : SpeechState()
    data class Listening(val isEndOfSpeech: Boolean, val partialText: String) : SpeechState()
    data class Success(val text: String) : SpeechState()
    data class Error(val message: String) : SpeechState()
}
