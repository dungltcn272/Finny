package com.ltcn272.finny.presentation.features.budget.budget_detail

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.domain.model.BudgetPeriod
import com.ltcn272.finny.domain.model.DaySection
import com.ltcn272.finny.domain.model.Transaction
import com.ltcn272.finny.domain.model.TransactionFilter
import com.ltcn272.finny.domain.model.TransactionType
import com.ltcn272.finny.domain.repository.BudgetRepository
import com.ltcn272.finny.domain.repository.TransactionRepository
import com.ltcn272.finny.domain.util.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.ehsannarmani.compose_charts.models.Bars
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

data class ChartUiState(
    val data: List<Bars> = emptyList(),
    val maxValue: Double = 100.0
)

sealed class FilterPeriod {
    data object All : FilterPeriod()
    data class Monthly(val yearMonth: YearMonth) : FilterPeriod()
    data class Daily(val date: LocalDate) : FilterPeriod()

    fun getDisplayName(locale: Locale): String {
        return when (this) {
            is All -> "All"
            is Daily -> this.date.dayOfMonth.toString()
            is Monthly -> this.yearMonth.month.getDisplayName(TextStyle.SHORT, locale)
        }
    }
}

data class BudgetDetailUiState(
    val budget: Budget? = null,
    val transactions: List<Transaction> = emptyList(),
    val filterPeriods: List<FilterPeriod> = emptyList(),
    val selectedFilter: FilterPeriod = FilterPeriod.All,
    val currency: String = "VND",
    val showDeleteConfirmation: Boolean = false,
    val isDeleting: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BudgetDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val budgetId: StateFlow<String> = savedStateHandle.getStateFlow("budgetId", "")

    private val _selectedFilter = MutableStateFlow<FilterPeriod>(FilterPeriod.All)
    val selectedFilter: StateFlow<FilterPeriod> = _selectedFilter

    private val _uiState = MutableStateFlow(BudgetDetailUiState())
    val uiState: StateFlow<BudgetDetailUiState> = _uiState

    private val _navigateBack = MutableSharedFlow<Unit>()
    val navigateBack: SharedFlow<Unit> = _navigateBack.asSharedFlow()


    private val allTransactions: StateFlow<List<Transaction>> = budgetId.flatMapLatest { id ->
        transactionRepository.getLocalTransactions(TransactionFilter(budgetId = id))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            combine(
                budgetId.flatMapLatest { id -> budgetRepository.getBudgetDetails(id) },
                allTransactions,
                _selectedFilter
            ) { budgetDetails, transactions, selectedFilter ->
                val budget = budgetDetails?.budget
                val filterPeriods = generateFilterPeriods(budget, transactions)
                val currentSelectedFilter =
                    if (filterPeriods.contains(selectedFilter)) selectedFilter else FilterPeriod.All

                val filteredTransactions = when (currentSelectedFilter) {
                    FilterPeriod.All -> transactions
                    is FilterPeriod.Daily -> {
                        val date = currentSelectedFilter.date
                        transactions.filter { it.dateTime.toLocalDate() == date }
                    }

                    is FilterPeriod.Monthly -> {
                        val yearMonth = currentSelectedFilter.yearMonth
                        transactions.filter { YearMonth.from(it.dateTime.toLocalDate()) == yearMonth }
                    }
                }

                _uiState.value.copy(
                    budget = budget,
                    transactions = filteredTransactions,
                    filterPeriods = filterPeriods,
                    selectedFilter = currentSelectedFilter
                )
            }.collect {
                _uiState.value = it
            }
        }
    }


    val groupedTransactions: StateFlow<List<DaySection>> = uiState.map { state ->
        state.transactions
            .groupBy { it.dateTime.toLocalDate() }
            .map { (date, transactions) ->
                val total =
                    transactions.sumOf { if (it.type == TransactionType.INCOME) it.amount else -it.amount }
                DaySection(date, total, transactions)
            }
            .sortedByDescending { it.date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    val chartUiState: StateFlow<ChartUiState> = uiState.map { state ->
        val transactionsByDay = state.transactions.groupBy { it.dateTime.dayOfWeek }
        val locale = Locale.getDefault()
        val daysOfWeek = DayOfWeek.entries.toTypedArray()

        val bars = daysOfWeek.map { day ->
            val transactionsForDay = transactionsByDay[day] ?: emptyList()
            val income =
                transactionsForDay.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val outcome =
                transactionsForDay.filter { it.type == TransactionType.OUTCOME }.sumOf { it.amount }

            Bars(
                label = day.getDisplayName(TextStyle.SHORT, locale),
                values = listOf(
                    Bars.Data(
                        label = "Outcome",
                        value = outcome,
                        color = SolidColor(Color.Red)
                    ),
                    Bars.Data(
                        label = "Income",
                        value = income,
                        color = SolidColor(Color.Green)
                    )
                )
            )
        }

        val maxDataValue =
            bars.maxOfOrNull { bar -> bar.values.maxOfOrNull { data -> data.value } ?: 0.0 } ?: 0.0
        val niceMaxValue = calculateNiceMaxValue(maxDataValue)

        ChartUiState(data = bars, maxValue = niceMaxValue)

    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChartUiState())

    fun setFilter(filter: FilterPeriod?) {
        _selectedFilter.value = filter ?: FilterPeriod.All
    }

    fun onDeleteConfirmation() {
        _uiState.value = _uiState.value.copy(showDeleteConfirmation = true)
    }

    fun onDismissDeleteDialog() {
        _uiState.value = _uiState.value.copy(showDeleteConfirmation = false)
    }

    fun onDeleteBudget() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, showDeleteConfirmation = false)
            val result = budgetRepository.deleteBudgetLocally(budgetId.value)
            if (result is AppResult.Success) {
                _navigateBack.emit(Unit)
            }
            _uiState.value = _uiState.value.copy(isDeleting = false)
        }
    }


    fun formatCurrency(value: Double, locale: Locale): String {
        val isVietnamese = locale.language == "vi"
        val million = 1_000_000
        val thousand = 1_000

        return when {
            value >= million -> {
                val displayValue = value / million
                val suffix = if (isVietnamese) "TR" else "M"
                "${NumberFormat.getInstance().format(displayValue)}$suffix"
            }

            value >= thousand -> {
                val displayValue = value / thousand
                val suffix = if (isVietnamese) "N" else "K"
                "${NumberFormat.getInstance().format(displayValue)}$suffix"
            }

            else -> NumberFormat.getInstance().format(value)
        }
    }

    private fun calculateNiceMaxValue(actualMax: Double): Double {
        if (actualMax <= 0) return 40.0
        val power = floor(log10(actualMax))
        val divisor = 4 * 10.0.pow(power)
        return (floor(actualMax / divisor) + 1) * divisor
    }

    private fun generateFilterPeriods(
        budget: Budget?,
        transactions: List<Transaction>
    ): List<FilterPeriod> {
        if (budget == null) return listOf(FilterPeriod.All)

        val latestTransactionDate = transactions.maxOfOrNull { it.dateTime }
        val now = ZonedDateTime.now()

        val endDateForGeneration =
            if (latestTransactionDate != null && latestTransactionDate.isAfter(now)) {
                latestTransactionDate
            } else {
                now
            }

        val periods = mutableListOf<FilterPeriod>(FilterPeriod.All)

        when (budget.period) {
            BudgetPeriod.SINGLE -> { /* No extra periods */
            }

            BudgetPeriod.ONE_WEEK -> {
                var currentDate = budget.startDate.toLocalDate()
                val endLocalDate = endDateForGeneration.toLocalDate()
                while (!currentDate.isAfter(endLocalDate)) {
                    periods.add(FilterPeriod.Daily(currentDate))
                    currentDate = currentDate.plusDays(1)
                }
            }

            BudgetPeriod.ONE_MONTH, BudgetPeriod.ONE_YEAR -> {
                var currentMonth = YearMonth.from(budget.startDate)
                val endMonth = YearMonth.from(endDateForGeneration)
                while (!currentMonth.isAfter(endMonth)) {
                    periods.add(FilterPeriod.Monthly(currentMonth))
                    currentMonth = currentMonth.plusMonths(1)
                }
            }

            else -> { /* No extra periods */
            }
        }
        return periods
    }
}
