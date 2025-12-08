package com.ltcn272.finny.presentation.features.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ltcn272.finny.data.SettingDataStore
import com.ltcn272.finny.domain.model.PricePlan
import com.ltcn272.finny.domain.repository.AuthRepository
import com.ltcn272.finny.domain.repository.PriceRepository
import com.ltcn272.finny.domain.util.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val settingDataStore: SettingDataStore,
    private val priceRepository: PriceRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val selectedCurrency: StateFlow<String> = settingDataStore.getSelectedCurrency
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "VND" // Default value
        )

    // Username StateFlow (nullable)
    val username: StateFlow<String?> = settingDataStore.getUsername
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    // Logout event to notify UI to navigate after logout
    private val _logoutEvent = MutableSharedFlow<Unit>(replay = 0)
    val logoutEvent = _logoutEvent.asSharedFlow()

    // Price plans state
    private val _pricePlans = MutableStateFlow<List<PricePlan>>(emptyList())
    val pricePlans: StateFlow<List<PricePlan>> = _pricePlans

    private val _isLoadingPrices = MutableStateFlow(false)
    val isLoadingPrices: StateFlow<Boolean> = _isLoadingPrices

    // Logging out state to show loading indicator during async logout
    private val _isLoggingOut = MutableStateFlow(false)
    val isLoggingOut: StateFlow<Boolean> = _isLoggingOut

    private val _isSubscribing = MutableStateFlow(false)
    val isSubscribing: StateFlow<Boolean> = _isSubscribing

    init {
        fetchPricePlans()
    }

    private fun fetchPricePlans() {
        viewModelScope.launch {
            _isLoadingPrices.value = true

            when (val res = priceRepository.getPricePlans("en")) {
                is AppResult.Success -> {
                    _pricePlans.value = res.data
                }
                is AppResult.Error -> {
                    // ignored for now
                }
                else -> {
                    // no-op for loading variant
                }
            }

            _isLoadingPrices.value = false
        }
    }

    fun onCurrencySelected(currency: String) {
        viewModelScope.launch {
            settingDataStore.saveSelectedCurrency(currency)
        }
    }

    fun onExportDataClicked() {
        // TODO: Implement data export logic
    }

    fun onSuggestFeatureClicked() {
        // TODO: Implement suggest feature logic
    }

    fun onRateAppClicked() {
        // TODO: Implement app rating logic
    }

    fun onLogoutClicked() {
        viewModelScope.launch {
            _isLoggingOut.value = true
            try {
                authRepository.logout()
            } catch (t: Throwable) {
                // ignore logout failures for now
            }
            try {
                _logoutEvent.emit(Unit)
            } finally {
                _isLoggingOut.value = false
            }
        }
    }

    // Placeholder subscribe flow: in real app this should call backend or billing SDK
    fun subscribe(planId: String, periodIndex: Int, onFinished: (Boolean, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            _isSubscribing.value = true
            try {
                // TODO: call subscribe API or billing integration
                // For now simulate success. Use planId/periodIndex in logs or server call.
                // Log usage to avoid unused parameter warnings
                println("Subscribing to plan=$planId period=$periodIndex")
                kotlinx.coroutines.delay(800)
                onFinished(true, null)
            } catch (t: Throwable) {
                onFinished(false, t.localizedMessage)
            } finally {
                _isSubscribing.value = false
            }
        }
    }
}
