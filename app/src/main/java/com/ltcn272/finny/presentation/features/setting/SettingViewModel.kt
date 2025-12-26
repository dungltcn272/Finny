package com.ltcn272.finny.presentation.features.setting

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ltcn272.finny.R
import com.ltcn272.finny.data.SettingDataStore
import com.ltcn272.finny.domain.repository.AuthRepository
import com.ltcn272.finny.domain.repository.TransactionRepository
import com.ltcn272.finny.domain.util.AppResult
import com.ltcn272.finny.presentation.common.ui.TopSnackbarType
import com.ltcn272.finny.util.PermissionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SettingNotificationEvent {
    data object RequestPermission : SettingNotificationEvent()
}

data class SnackbarState(
    val visible: Boolean = false,
    val message: String = "",
    val type: TopSnackbarType = TopSnackbarType.INFO,
    val actionTitle: String? = null,
    val onActionClick: (() -> Unit)? = null
)

data class SettingUiState(
    val username: String? = null,
    val selectedCurrency: String = "VND",
    val actualNotificationStatus: Boolean = false,
    val isLoggingOut: Boolean = false,
    val isExportingPdf: Boolean = false,
    val snackbarState: SnackbarState = SnackbarState()
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
    private val _snackbarState = MutableStateFlow(SnackbarState())

    val uiState: StateFlow<SettingUiState> = combine(
        flows = listOf(
            settingDataStore.usernameFlow,
            settingDataStore.getSelectedCurrency,
            settingDataStore.getEnableNotifications.combine(hasSystemPermission) { pref, perm -> pref && perm },
            _isLoggingOut,
            _isExportingPdf,
            _snackbarState
        )
    ) { values ->
        val username = values[0] as String?
        val currency = values[1] as String
        val notificationStatus = values[2] as Boolean
        val isLoggingOut = values[3] as Boolean
        val isExporting = values[4] as Boolean
        val snackbar = values[5] as SnackbarState

        SettingUiState(
            username = username,
            selectedCurrency = currency,
            actualNotificationStatus = notificationStatus,
            isLoggingOut = isLoggingOut,
            isExportingPdf = isExporting,
            snackbarState = snackbar
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

    fun exportStatement(onSuccess: (base64: String) -> Unit) {
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
                        showSnackbar(
                            application.getString(R.string.error_exporting_pdf),
                            TopSnackbarType.ERROR
                        )
                    }
                }
            }
        }
    }


    fun showPdfExportSuccess(fileName: String) {
        showSnackbar(
            application.getString(R.string.pdf_saved_to_downloads , fileName),
            TopSnackbarType.SUCCESS
        )
    }

    fun showPdfExportError() {
        showSnackbar(
            application.getString(R.string.error_saving_pdf),
            TopSnackbarType.ERROR
        )
    }

    fun showNoPdfViewerFound() {
        showSnackbar(
            application.getString(R.string.no_pdf_viewer_found),
            TopSnackbarType.WARNING
        )
    }

    private fun showSnackbar(message: String, type: TopSnackbarType) {
        _snackbarState.value = SnackbarState(
            visible = true,
            message = message,
            type = type
        )
    }

    fun onSnackbarDismissed() {
        _snackbarState.update { it.copy(visible = false) }
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
                setEnableNotifications(!currentPreference)
            }
        }
    }

    fun onNotificationPermissionResult(isGranted: Boolean) {
        hasSystemPermission.value = isGranted
        viewModelScope.launch {
            if (isGranted) {
                setEnableNotifications(true)
            } else {
                setEnableNotifications(false)
                _snackbarState.value = SnackbarState(
                    visible = true,
                    message = application.getString(R.string.notification_permission_denied),
                    type = TopSnackbarType.WARNING,
                    actionTitle = application.getString(R.string.open_settings),
                    onActionClick = { PermissionUtils.openAppNotificationSettings(application) }
                )
            }
        }
    }

    fun setEnableNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingDataStore.saveEnableNotifications(enabled)
            if (enabled) {
                _snackbarState.value = SnackbarState(
                    visible = true,
                    message = application.getString(R.string.notifications_enabled),
                    type = TopSnackbarType.SUCCESS
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
