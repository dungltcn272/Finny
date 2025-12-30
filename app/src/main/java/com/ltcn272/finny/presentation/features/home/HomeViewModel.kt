package com.ltcn272.finny.presentation.features.home

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ltcn272.finny.R
import com.ltcn272.finny.data.SettingDataStore
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.domain.model.Transaction
import com.ltcn272.finny.domain.model.TransactionType
import com.ltcn272.finny.domain.repository.BudgetRepository
import com.ltcn272.finny.domain.repository.ChatRepository
import com.ltcn272.finny.domain.repository.TransactionRepository
import com.ltcn272.finny.domain.util.AppResult
import com.ltcn272.finny.domain.util.ErrorType
import com.ltcn272.finny.presentation.common.ui.DonutData
import com.ltcn272.finny.presentation.common.util.ConnectivityObserver
import com.ltcn272.finny.presentation.common.util.NetworkStatus
import com.ltcn272.finny.presentation.common.util.formatCurrencyNonComposable
import com.ltcn272.finny.presentation.common.util.generateHarmonicColors
import com.ltcn272.finny.presentation.common.util.getCurrentDateFormatted
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshingByUser: Boolean = false,
    val username: String? = null,
    val currentDate: String = "",

    val allRecentBudgets: List<Budget> = emptyList(),
    val allRecentTransactions: List<Transaction>? = null,

    val topBudgets: List<Budget> = emptyList(),
    val featuredBudget: Budget? = null,
    val transactionsToShow: List<Transaction> = emptyList(),

    val donutChartData: List<DonutData> = emptyList(),
    val donutChartTotalAmount: Double = 0.0,

    val transactionFilterType: TransactionType? = null,
    val currency: String = "VND",
    val showCreateBudgetPrompt: Boolean = false,
    val networkStatus: NetworkStatus = NetworkStatus.Available ,
    val bankNotificationCount: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val app: Application,
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val settingDataStore: SettingDataStore,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent = _errorEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            settingDataStore.usernameFlow.collect { username ->
                _uiState.update { it.copy(username = username) }
            }
        }
        viewModelScope.launch {
            settingDataStore.getSelectedCurrency.collect { currency ->
                _uiState.update { it.copy(currency = currency) }
            }
        }
        viewModelScope.launch {
            settingDataStore.bankNotificationInboxFlow.collect { inboxString ->
                val count = if (inboxString.isBlank()) 0 else inboxString.split("|||").size
                _uiState.update { it.copy(bankNotificationCount = count) }
            }
        }

        _uiState.update { it.copy(currentDate = getCurrentDateFormatted()) }

        observeConnectivity()
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            var isFirstObservation = true
            connectivityObserver.observe().collectLatest { status ->
                _uiState.update { it.copy(networkStatus = status) }
                if (status == NetworkStatus.Available) {
                    if (isFirstObservation || _uiState.value.allRecentBudgets.isEmpty()) {
                        loadInitialData()
                    }
                    isFirstObservation = false
                }
            }
        }
    }


    private fun loadInitialData() {
        if (_uiState.value.networkStatus != NetworkStatus.Available) {
            _uiState.update { it.copy(isLoading = false) } // Nếu không có mạng thì không loading nữa
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showCreateBudgetPrompt = false) }

            val budgetResult = budgetRepository.getRecentBudgets()
            val transactionResult = transactionRepository.getRecentTransactions()

            val allBudgets = if (budgetResult is AppResult.Success) budgetResult.data else emptyList()
            val allTransactions = if (transactionResult is AppResult.Success) transactionResult.data else null

            // Chỉ bắn lỗi nếu đó không phải lỗi mạng (lỗi mạng đã có indicator)
            if (budgetResult is AppResult.Error && budgetResult.errorType != ErrorType.NETWORK) {
                _errorEvent.emit(mapErrorToString(budgetResult.errorType))
            }
            if (transactionResult is AppResult.Error && transactionResult.errorType != ErrorType.NETWORK) {
                _errorEvent.emit(mapErrorToString(transactionResult.errorType))
            }

            val shouldShowPrompt = budgetResult is AppResult.Success && allBudgets.isEmpty()

            _uiState.update {
                it.copy(
                    allRecentBudgets = allBudgets,
                    allRecentTransactions = allTransactions,
                    showCreateBudgetPrompt = shouldShowPrompt
                )
            }
            processAndUpdateUiState()
        }
    }

    fun refreshDataFromPull() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshingByUser = true) }

            val budgetResult = budgetRepository.getRecentBudgets()
            val transactionResult = transactionRepository.getRecentTransactions()

            val allBudgets = if (budgetResult is AppResult.Success) budgetResult.data else emptyList()
            val allTransactions = if (transactionResult is AppResult.Success) transactionResult.data else null

            if (budgetResult is AppResult.Error) {
                _errorEvent.emit(mapErrorToString(budgetResult.errorType))
            }
            if (transactionResult is AppResult.Error) {
                _errorEvent.emit(mapErrorToString(transactionResult.errorType))
            }

            val shouldShowPrompt = budgetResult is AppResult.Success && allBudgets.isEmpty()

            _uiState.update {
                it.copy(
                    isRefreshingByUser = false,
                    allRecentBudgets = allBudgets,
                    allRecentTransactions = allTransactions,
                    showCreateBudgetPrompt = shouldShowPrompt
                )
            }
            processAndUpdateUiState()
        }
    }

    private fun processAndUpdateUiState() {
        _uiState.update { currentState ->
            val allBudgets = currentState.allRecentBudgets
            val allTransactions = currentState.allRecentTransactions ?: emptyList()

            val topBudgets = allBudgets.sortedByDescending { it.startDate }.take(3)
            val featuredBudget = topBudgets.minByOrNull { it.daysRemaining ?: Double.MAX_VALUE }

            val filteredTransactions = if (currentState.transactionFilterType == null) {
                allTransactions
            } else {
                allTransactions.filter { it.type == currentState.transactionFilterType }
            }
            val transactionsToShow = filteredTransactions.sortedByDescending { it.dateTime }.take(5)

            val (donutData, totalAmount) = createDonutChartData(allBudgets, currentState.currency)

            currentState.copy(
                isLoading = false,
                topBudgets = topBudgets,
                featuredBudget = featuredBudget,
                transactionsToShow = transactionsToShow,
                donutChartData = donutData,
                donutChartTotalAmount = totalAmount
            )
        }
    }

    fun setTransactionFilter(type: TransactionType?) {
        _uiState.update { it.copy(transactionFilterType = type) }
        processAndUpdateUiState()
    }

    private fun createDonutChartData(budgets: List<Budget>, currency: String): Pair<List<DonutData>, Double> {
        val budgetsForChart = budgets.filter { it.totalOutcome > 0 }
        if (budgetsForChart.isEmpty()) {
            return Pair(emptyList(), 0.0)
        }

        val totalOutcome = budgetsForChart.sumOf { it.totalOutcome }
        val baseColor = Color(0xFF4A90E2)
        val dynamicColors = generateHarmonicColors(baseColor, budgetsForChart.size)

        val donutDataList = budgetsForChart.mapIndexed { index, budget ->
            DonutData(
                label = budget.name,
                value = budget.totalOutcome.toFloat(),
                amountText = formatCurrencyNonComposable(budget.totalOutcome, currency),
                color = dynamicColors.getOrElse(index) { Color.Gray }
            )
        }

        return Pair(donutDataList, totalOutcome)
    }

    private fun mapErrorToString(errorType: ErrorType): String {
        return when (errorType) {
            ErrorType.NETWORK -> app.getString(R.string.error_network)
            ErrorType.TIMEOUT -> app.getString(R.string.error_timeout)
            ErrorType.UNAUTHORIZED -> app.getString(R.string.error_unauthorized)
            ErrorType.SERVER_ERROR -> app.getString(R.string.error_server)
            ErrorType.UNKNOWN -> app.getString(R.string.error_unknown)
        }
    }
}
