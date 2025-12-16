package com.ltcn272.finny.presentation.features.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ltcn272.finny.data.SettingDataStore
import com.ltcn272.finny.domain.model.PricePlan
import com.ltcn272.finny.domain.repository.AuthRepository
import com.ltcn272.finny.domain.repository.FcmRepository
import com.ltcn272.finny.domain.repository.PriceRepository
import com.ltcn272.finny.domain.util.AppResult
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.ltcn272.finny.R
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Response

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val settingDataStore: SettingDataStore,
    private val priceRepository: PriceRepository,
    private val authRepository: AuthRepository,
    private val fcmRepository: FcmRepository
) : ViewModel() {

    val selectedCurrency: StateFlow<String> = settingDataStore.getSelectedCurrency
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "VND"
        )

    val username: StateFlow<String?> = settingDataStore.getUsername
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val enableNotifications: StateFlow<Boolean> = settingDataStore.getEnableNotifications
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = true)

    val authenticationEnabled: StateFlow<Boolean> = settingDataStore.getAuthenticationEnabled
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = false)

    private val _logoutEvent = MutableSharedFlow<Unit>(replay = 0)
    val logoutEvent = _logoutEvent.asSharedFlow()

    private val _pricePlans = MutableStateFlow<List<PricePlan>>(emptyList())
    val pricePlans: StateFlow<List<PricePlan>> = _pricePlans

    private val _isLoadingPrices = MutableStateFlow(false)
    val isLoadingPrices: StateFlow<Boolean> = _isLoadingPrices

    private val _isLoggingOut = MutableStateFlow(false)
    val isLoggingOut: StateFlow<Boolean> = _isLoggingOut

    private val _isSubscribing = MutableStateFlow(false)
    val isSubscribing: StateFlow<Boolean> = _isSubscribing

    private val _notificationEvent = MutableSharedFlow<Int>(replay = 0)
    val notificationEvent = _notificationEvent.asSharedFlow()

    init {
        fetchPricePlans()
    }

    private fun fetchPricePlans() {
        viewModelScope.launch {
            _isLoadingPrices.value = true
            when (val res = priceRepository.getPricePlans("en")) {
                is AppResult.Success -> _pricePlans.value = res.data
                else -> {}
            }
            _isLoadingPrices.value = false
        }
    }

    fun onCurrencySelected(currency: String) {
        viewModelScope.launch { settingDataStore.saveSelectedCurrency(currency) }
    }

    private suspend fun fetchFirebaseToken(): String? = suspendCancellableCoroutine { cont ->
        try {
            val task = FirebaseMessaging.getInstance().token
            task.addOnCompleteListener { t ->
                if (t.isSuccessful) cont.resume(t.result) else cont.resume(null)
            }
        } catch (e: Exception) {
            cont.resume(null)
        }
    }

    fun setEnableNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingDataStore.saveEnableNotifications(enabled)
            if (!enabled) return@launch

            val token = try { fetchFirebaseToken() } catch (t: Throwable) { null }
            if (token.isNullOrEmpty()) return@launch

            val maxAttempts = 3
            var attempt = 0
            var sent = false
            var backoff = 1000L

            while (attempt < maxAttempts && !sent) {
                attempt++
                try {
                    fcmRepository.updateOrCreateFcmToken(token)
                    sent = true
                    _notificationEvent.emit(R.string.notification_sent_success)
                } catch (t: Throwable) {
                    if (attempt >= maxAttempts) {
                        _notificationEvent.emit(R.string.notification_sent_failed)
                    } else {
                        delay(backoff)
                        backoff = (backoff * 2).coerceAtMost(8_000L)
                    }
                }
            }

            if (sent) {
                try {
                    val response: Response<Unit> = fcmRepository.testNotification(title = "[Test] From App", body = "Test notification from app")
                    if (response.isSuccessful) _notificationEvent.emit(R.string.notification_test_sent_success) else _notificationEvent.emit(R.string.notification_test_sent_failed)
                } catch (t: Throwable) {
                    _notificationEvent.emit(R.string.notification_test_sent_failed)
                }
            }
        }
    }

    fun setAuthenticationEnabled(enabled: Boolean) {
        viewModelScope.launch { settingDataStore.saveAuthenticationEnabled(enabled) }
    }

    fun onExportDataClicked() {}
    fun onSuggestFeatureClicked() {}
    fun onRateAppClicked() {}

    fun onLogoutClicked() {
        viewModelScope.launch {
            _isLoggingOut.value = true
            try { authRepository.logout() } catch (_: Throwable) {}
            _logoutEvent.emit(Unit)
            _isLoggingOut.value = false
        }
    }

    fun subscribe(planId: String, periodIndex: Int, onFinished: (Boolean, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            _isSubscribing.value = true
            try {
                println("Subscribing to plan=$planId period=$periodIndex")
                delay(800)
                onFinished(true, null)
            } catch (t: Throwable) {
                onFinished(false, t.localizedMessage)
            } finally {
                _isSubscribing.value = false
            }
        }
    }
}
