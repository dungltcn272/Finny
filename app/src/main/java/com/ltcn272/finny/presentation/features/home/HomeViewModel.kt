package com.ltcn272.finny.presentation.features.home

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ltcn272.finny.data.SettingDataStore
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.domain.model.Transaction
import com.ltcn272.finny.domain.model.TransactionType
import com.ltcn272.finny.domain.repository.BudgetRepository
import com.ltcn272.finny.domain.repository.TransactionRepository
import com.ltcn272.finny.domain.util.AppResult
import com.ltcn272.finny.presentation.common.ui.DonutData
import com.ltcn272.finny.presentation.common.util.formatCurrencyNonComposable
import com.ltcn272.finny.presentation.common.util.generateHarmonicColors
import com.ltcn272.finny.presentation.common.util.getCurrentDateFormatted
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshingByUser: Boolean = false,
    val username: String? = null,
    val currentDate: String = "",

    val allRecentBudgets: List<Budget> = emptyList(),
    val allRecentTransactions: List<Transaction> = emptyList(),

    val topBudgets: List<Budget> = emptyList(),
    val featuredBudget: Budget? = null,
    val transactionsToShow: List<Transaction> = emptyList(),

    val donutChartData: List<DonutData> = emptyList(),
    val donutChartTotalAmount: Double = 0.0,

    val transactionFilterType: TransactionType? = null,
    val currency: String = "VND"
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val settingDataStore: SettingDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // BỎ CỜ isDataLoaded VÀ HÀM onHomeScreenResumed
    // private var isDataLoaded = false

    init {
        // Lấy username và ngày tháng
        viewModelScope.launch {
            settingDataStore.usernameFlow.collect { username ->
                _uiState.update { it.copy(username = username) }
            }
        }
        _uiState.update { it.copy(currentDate = getCurrentDateFormatted()) }

        // GỌI LẠI HÀM LOAD Ở ĐÂY. NÓ SẼ CHỈ CHẠY MỘT LẦN KHI VIEWMODEL ĐƯỢC TẠO
        loadInitialData()
    }

    // fun onHomeScreenResumed() { ... } // BỎ HÀM NÀY

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val budgetResult = budgetRepository.getRecentBudgets()
            val transactionResult = transactionRepository.getRecentTransactions()
            val allBudgets = if (budgetResult is AppResult.Success) budgetResult.data else emptyList()
            val allTransactions = if (transactionResult is AppResult.Success) transactionResult.data else emptyList()

            _uiState.update {
                it.copy(
                    // isLoading = false, // Sẽ được set ở processAndUpdateUiState
                    allRecentBudgets = allBudgets,
                    allRecentTransactions = allTransactions
                )
            }
            processAndUpdateUiState() // Hàm này sẽ set isLoading = false
        }
    }

    fun refreshDataFromPull() {
        // Tải lại dữ liệu và set isRefreshingByUser
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshingByUser = true) }

            val budgetResult = budgetRepository.getRecentBudgets()
            val transactionResult = transactionRepository.getRecentTransactions()
            val allBudgets = if (budgetResult is AppResult.Success) budgetResult.data else emptyList()
            val allTransactions = if (transactionResult is AppResult.Success) transactionResult.data else emptyList()

            _uiState.update {
                it.copy(
                    isRefreshingByUser = false, // Reset cờ
                    allRecentBudgets = allBudgets,
                    allRecentTransactions = allTransactions
                )
            }
            processAndUpdateUiState()
        }
    }

    private fun processAndUpdateUiState() {
        _uiState.update { currentState ->
            // ... (code xử lý dữ liệu bên trong hàm này không đổi) ...
            val allBudgets = currentState.allRecentBudgets
            val allTransactions = currentState.allRecentTransactions

            val topBudgets = allBudgets.sortedByDescending { it.startDate }.take(3)
            val featuredBudget = topBudgets.minByOrNull { it.daysRemaining ?: Double.MAX_VALUE }

            val filteredTransactions = if (currentState.transactionFilterType == null) {
                allTransactions
            } else {
                allTransactions.filter { it.type == currentState.transactionFilterType }
            }
            val transactionsToShow = filteredTransactions.sortedByDescending { it.dateTime }.take(5)

            val (donutData, totalAmount) = createDonutChartData(allBudgets, currentState.currency)

            // TRẢ VỀ STATE HOÀN CHỈNH VÀ SET isLoading = false
            currentState.copy(
                isLoading = false,
                topBudgets = topBudgets,
                featuredBudget = featuredBudget,
                transactionsToShow = transactionsToShow,
                donutChartData = donutData,
                donutChartTotalAmount = totalAmount.toDouble()
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
                color = dynamicColors[index]
            )
        }

        return Pair(donutDataList, totalOutcome)
    }
}
