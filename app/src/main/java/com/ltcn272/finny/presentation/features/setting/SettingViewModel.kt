package com.ltcn272.finny.presentation.features.setting

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ltcn272.finny.R
import com.ltcn272.finny.data.SettingDataStore
import com.ltcn272.finny.domain.repository.AuthRepository
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

// TẠO UI STATE DATA CLASS
data class SettingUiState(
    val username: String? = null,
    val selectedCurrency: String = "VND",
    val actualNotificationStatus: Boolean = false,
    val isLoggingOut: Boolean = false,
    val snackbarState: SnackbarState = SnackbarState()
)

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val application: Application,
    private val settingDataStore: SettingDataStore,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val hasSystemPermission = MutableStateFlow(PermissionUtils.hasNotificationPermission(application))
    private val _isLoggingOut = MutableStateFlow(false)
    private val _snackbarState = MutableStateFlow(SnackbarState())

    val uiState: StateFlow<SettingUiState> = combine(
        settingDataStore.usernameFlow,
        settingDataStore.getSelectedCurrency,
        settingDataStore.getEnableNotifications.combine(hasSystemPermission) { pref, perm -> pref && perm },
        _isLoggingOut,
        _snackbarState
    ) { username, currency, notificationStatus, isLoggingOut, snackbar ->
        SettingUiState(
            username = username,
            selectedCurrency = currency,
            actualNotificationStatus = notificationStatus,
            isLoggingOut = isLoggingOut,
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
                // Lấy giá trị hiện tại từ StateFlow
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

    fun onSnackbarDismissed() {
        _snackbarState.update { it.copy(visible = false) }
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
