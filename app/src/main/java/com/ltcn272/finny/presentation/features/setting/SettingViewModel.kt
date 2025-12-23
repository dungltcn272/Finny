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

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val application: Application,
    private val settingDataStore: SettingDataStore,
    private val authRepository: AuthRepository
) : ViewModel() {

    val selectedCurrency: StateFlow<String> = settingDataStore.getSelectedCurrency
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "VND")

    val username: StateFlow<String?> = settingDataStore.usernameFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val hasSystemPermission = MutableStateFlow(PermissionUtils.hasNotificationPermission(application))

    val actualNotificationStatus: StateFlow<Boolean> = settingDataStore.getEnableNotifications
        .combine(hasSystemPermission) { userPreference, hasPermission ->
            userPreference && hasPermission
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)


    private val _isLoggingOut = MutableStateFlow(false)
    val isLoggingOut: StateFlow<Boolean> = _isLoggingOut

    private val _logoutEvent = MutableSharedFlow<Unit>(replay = 0)
    val logoutEvent = _logoutEvent.asSharedFlow()

    private val _notificationEvent = MutableSharedFlow<SettingNotificationEvent>()
    val notificationEvent = _notificationEvent.asSharedFlow()

    private val _snackbarState = MutableStateFlow(SnackbarState())
    val snackbarState: StateFlow<SnackbarState> = _snackbarState.asStateFlow()

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
                val currentPreference = actualNotificationStatus.value
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
