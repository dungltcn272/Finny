package com.ltcn272.finny.presentation.features.snackbar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Data class để chứa trạng thái của Snackbar.
 */
data class SnackbarState(
    val message: String,
    val type: TopSnackbarType,
    val duration: TopSnackbarDuration,
    val action: (@Composable () -> Unit)?
)

/**
 * Class quản lý trạng thái và hành vi của Snackbar.
 * Được đánh dấu @Stable để tối ưu hóa recomposition.
 */
@Stable
class SnackbarManager(private val scope: CoroutineScope) {
    var snackbarState by mutableStateOf<SnackbarState?>(null)
        private set

    private var snackbarJob: Job? = null

    /**
     * Hiển thị một tin nhắn trên Snackbar.
     * Bất kỳ tin nhắn nào đang hiển thị sẽ bị hủy và thay thế bằng tin nhắn mới.
     */
    fun showMessage(
        message: String,
        type: TopSnackbarType = TopSnackbarType.INFO,
        duration: TopSnackbarDuration = TopSnackbarDuration.SHORT,
        action: (@Composable () -> Unit)? = null
    ) {
        // Hủy bỏ job cũ để hiển thị snackbar mới ngay lập tức
        snackbarJob?.cancel()
        snackbarJob = scope.launch {
            snackbarState = SnackbarState(
                message = message,
                type = type,
                duration = duration,
                action = action
            )
            // Tự động ẩn sau một khoảng thời gian
            delay(duration.millis)
            dismiss()
        }
    }

    /**
     * Ẩn Snackbar đang hiển thị.
     */
    fun dismiss() {
        snackbarJob?.cancel()
        snackbarState = null
    }
}

/**
 * CompositionLocal để cung cấp SnackbarManager cho cây Composable.
 */
val LocalSnackbarManager = staticCompositionLocalOf<SnackbarManager> {
    error("SnackbarManager not provided. Make sure to wrap your app in a ProvideSnackbarManager.")
}

/**
 * Composable cung cấp SnackbarManager cho các Composable con của nó.
 * Nên được đặt ở cấp cao nhất của ứng dụng, ví dụ như trong AppNav.
 */
@Composable
fun ProvideSnackbarManager(content: @Composable () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarManager = remember { SnackbarManager(coroutineScope) }

    CompositionLocalProvider(LocalSnackbarManager provides snackbarManager) {
        content()
    }
}
