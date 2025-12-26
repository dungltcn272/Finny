package com.ltcn272.finny.presentation.features.transaction.recurring_transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ltcn272.finny.data.SettingDataStore
import com.ltcn272.finny.domain.model.RecurringTransaction
import com.ltcn272.finny.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecurringTransactionUiState(
    val isRefreshingByUser: Boolean = false,
    val currencyCode: String = "VND"
)

@HiltViewModel
class RecurringTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val settingDataStore: SettingDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecurringTransactionUiState())
    val uiState = _uiState.asStateFlow()

    val recurringTransactionsPagingFlow: Flow<PagingData<RecurringTransaction>> =
        transactionRepository.getRecurringTransactions().cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            val currency = settingDataStore.getSelectedCurrency.first()
            _uiState.update { it.copy(currencyCode = currency) }
        }
    }

    fun onUserPullToRefresh() {
        _uiState.update { it.copy(isRefreshingByUser = true) }
    }

    fun onRefreshFinished() {
        _uiState.update { it.copy(isRefreshingByUser = false) }
    }
}
