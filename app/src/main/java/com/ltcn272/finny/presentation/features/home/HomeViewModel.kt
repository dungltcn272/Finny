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
import com.ltcn272.finny.presentation.common.util.generateHarmonicColors
import com.ltcn272.finny.presentation.features.home.component.LegendData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.dautovicharis.charts.model.ChartDataSet
import io.github.dautovicharis.charts.model.toChartDataSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
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
    val chartDataSet: ChartDataSet? = null,
    val legendData: List<LegendData> = emptyList(),
    val totalRemainder: Long = 0,
    val pieColors: List<Color> = emptyList(),

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

    init {
        viewModelScope.launch {
            settingDataStore.usernameFlow.collect { username ->
                _uiState.update { it.copy(username = username) }
            }
        }
        _uiState.update { it.copy(currentDate = getCurrentDateFormatted()) }
        // Tải dữ liệu lần đầu
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) } // Bật isLoading cho lần đầu

            val budgetResult = budgetRepository.getRecentBudgets()
            val transactionResult = transactionRepository.getRecentTransactions()
            val allBudgets = if (budgetResult is AppResult.Success) budgetResult.data else emptyList()
            val allTransactions = if (transactionResult is AppResult.Success) transactionResult.data else emptyList()

            _uiState.update {
                it.copy(
                    isLoading = false, // Tắt isLoading sau khi tải xong
                    allRecentBudgets = allBudgets,
                    allRecentTransactions = allTransactions
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
            val allTransactions = if (transactionResult is AppResult.Success) transactionResult.data else emptyList()

            _uiState.update {
                it.copy(
                    isRefreshingByUser = false,
                    allRecentBudgets = allBudgets,
                    allRecentTransactions = allTransactions
                )
            }
            processAndUpdateUiState()
        }
    }

    fun setTransactionFilter(type: TransactionType?) {
        _uiState.update { it.copy(transactionFilterType = type) }
        processAndUpdateUiState()
    }

    private fun processAndUpdateUiState() {
        _uiState.update { currentState ->
            val allBudgets = currentState.allRecentBudgets
            val allTransactions = currentState.allRecentTransactions

            val topBudgets = allBudgets.sortedByDescending { it.startDate }.take(3)
            val featuredBudget = topBudgets.minByOrNull { it.daysRemaining ?: Int.MAX_VALUE }

            val filteredTransactions = if (currentState.transactionFilterType == null) {
                allTransactions
            } else {
                allTransactions.filter { it.type == currentState.transactionFilterType }
            }
            val transactionsToShow = filteredTransactions.sortedByDescending { it.dateTime }.take(5)

            val chartDataResult = createChartData(allBudgets)

            currentState.copy(
                topBudgets = topBudgets,
                featuredBudget = featuredBudget,
                transactionsToShow = transactionsToShow,
                chartDataSet = chartDataResult.first,
                legendData = chartDataResult.second.legend,
                totalRemainder = chartDataResult.second.totalRemainder,
                pieColors = chartDataResult.second.colors
            )
        }
    }

    private fun createChartData(budgets: List<Budget>): Pair<ChartDataSet?, ChartRelatedData> {
        val budgetsForChart = budgets.filter { it.totalOutcome > 0 }
        if (budgetsForChart.isEmpty()) {
            return Pair(null, ChartRelatedData())
        }

        val baseColor = Color(0xFF4A90E2)
        val dynamicColors = generateHarmonicColors(baseColor, budgetsForChart.size)
        val chartValues = budgetsForChart.map { it.totalOutcome.toFloat() }
        val chartLabels = budgetsForChart.map { it.name }
        val chartDataSet = chartValues.toChartDataSet("Budget Distribution", labels = chartLabels)

        val legend = budgetsForChart.mapIndexed { index, budget ->
            LegendData(
                name = budget.name,
                amount = budget.totalOutcome,
                color = dynamicColors[index]
            )
        }
        val totalRemainder = budgets.sumOf { it.amount }

        return Pair(chartDataSet, ChartRelatedData(legend, totalRemainder, dynamicColors))
    }

    private fun getCurrentDateFormatted(): String {
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)
        return ZonedDateTime.now().format(formatter)
    }

    private data class ChartRelatedData(
        val legend: List<LegendData> = emptyList(),
        val totalRemainder: Long = 0,
        val colors: List<Color> = emptyList()
    )
}
