package com.ltcn272.finny.presentation.features.setting

import android.app.Application
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ltcn272.finny.R
import com.ltcn272.finny.data.SettingDataStore
import com.ltcn272.finny.domain.repository.AuthRepository
import com.ltcn272.finny.domain.repository.TransactionRepository
import com.ltcn272.finny.domain.util.AppResult
import com.ltcn272.finny.presentation.features.snackbar.SnackbarManager
import com.ltcn272.finny.presentation.features.snackbar.TopSnackbarDuration
import com.ltcn272.finny.presentation.features.snackbar.TopSnackbarType
import com.ltcn272.finny.util.PermissionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SettingNotificationEvent {
    data object RequestPermission : SettingNotificationEvent()
}

data class SettingUiState(
    val username: String? = null,
    val selectedCurrency: String = "VND",
    val actualNotificationStatus: Boolean = false,
    val isLoggingOut: Boolean = false,
    val isExportingPdf: Boolean = false
)

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val application: Application,
    private val settingDataStore: SettingDataStore,
    private val authRepository: AuthRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val hasSystemPermission = MutableStateFlow(PermissionUtils.hasNotificationPermission(application))
    private val _isLoggingOut = MutableStateFlow(false)
    private val _isExportingPdf = MutableStateFlow(false)

    val uiState: StateFlow<SettingUiState> = combine(
        settingDataStore.usernameFlow,
        settingDataStore.getSelectedCurrency,
        settingDataStore.getEnableNotifications.combine(hasSystemPermission) { pref, perm -> pref && perm },
        _isLoggingOut,
        _isExportingPdf,
    ) { values ->
        val username = values[0] as String?
        val currency = values[1] as String
        val notificationStatus = values[2] as Boolean
        val isLoggingOut = values[3] as Boolean
        val isExporting = values[4] as Boolean

        SettingUiState(
            username = username,
            selectedCurrency = currency,
            actualNotificationStatus = notificationStatus,
            isLoggingOut = isLoggingOut,
            isExportingPdf = isExporting,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingUiState()
    )

    private val _logoutEvent = MutableSharedFlow<Unit>(replay = 0)
    val logoutEvent = _logoutEvent.asSharedFlow()

    private val _notificationEvent = MutableSharedFlow<SettingNotificationEvent>()
    val notificationEvent = _notificationEvent.asSharedFlow()

    fun exportStatement(snackbarManager: SnackbarManager, onSuccess: (base64: String) -> Unit) {
        viewModelScope.launch {
            transactionRepository.exportStatement().collect { result ->
                when (result) {
                    is AppResult.Loading -> {
                        _isExportingPdf.value = true
                    }
                    is AppResult.Success -> {
                        _isExportingPdf.value = false
                        onSuccess(result.data)
                    }
                    is AppResult.Error -> {
                        _isExportingPdf.value = false
                        snackbarManager.showMessage(
                            application.getString(R.string.error_exporting_pdf),
                            TopSnackbarType.ERROR
                        )
                    }
                }
            }
        }
    }

    fun showPdfExportSuccess(snackbarManager: SnackbarManager, fileName: String) {
        snackbarManager.showMessage(
            application.getString(R.string.pdf_saved_to_downloads, fileName),
            TopSnackbarType.SUCCESS
        )
    }

    fun showPdfExportError(snackbarManager: SnackbarManager) {
        snackbarManager.showMessage(
            application.getString(R.string.error_saving_pdf),
            TopSnackbarType.ERROR
        )
    }

    fun showNoPdfViewerFound(snackbarManager: SnackbarManager) {
        snackbarManager.showMessage(
            application.getString(R.string.no_pdf_viewer_found),
            TopSnackbarType.WARNING
        )
    }

    fun onCurrencySelected(currency: String) {
        viewModelScope.launch {
            settingDataStore.saveSelectedCurrency(currency)
        }
    }

    fun onEnableNotificationsToggled() {
        viewModelScope.launch {
            if (!hasSystemPermission.value) {
                _notificationEvent.emit(SettingNotificationEvent.RequestPermission)
            } else {
                val currentPreference = uiState.value.actualNotificationStatus
                // We don't need to pass snackbarManager here, as setEnableNotifications handles it
                setEnableNotifications(null, !currentPreference)
            }
        }
    }

    fun onNotificationPermissionResult(snackbarManager: SnackbarManager, isGranted: Boolean) {
        hasSystemPermission.value = isGranted
        viewModelScope.launch {
            if (isGranted) {
                setEnableNotifications(snackbarManager, true)
            } else {
                setEnableNotifications(snackbarManager, false)
                snackbarManager.showMessage(
                    message = application.getString(R.string.notification_permission_denied),
                    type = TopSnackbarType.WARNING,
                    duration = TopSnackbarDuration.LONG,
                    action = {
                        TextButton(onClick = { PermissionUtils.openAppNotificationSettings(application) }) {
                            Text(application.getString(R.string.open_settings), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        }
    }

    fun setEnableNotifications(snackbarManager: SnackbarManager?, enabled: Boolean) {
        viewModelScope.launch {
            settingDataStore.saveEnableNotifications(enabled)
            if (enabled) {
                snackbarManager?.showMessage(
                    application.getString(R.string.notifications_enabled),
                    TopSnackbarType.SUCCESS
                )
            }
        }
    }

    fun syncNotificationStatus() {
        val currentPermission = PermissionUtils.hasNotificationPermission(application)
        hasSystemPermission.value = currentPermission
        viewModelScope.launch {
            if (!currentPermission && settingDataStore.getEnableNotifications.first()) {
                settingDataStore.saveEnableNotifications(false)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _isLoggingOut.value = true
            try {
                authRepository.logout()
                _logoutEvent.emit(Unit)
            } catch (_: Exception) {
            } finally {
                _isLoggingOut.value = false
            }
        }
    }
}
