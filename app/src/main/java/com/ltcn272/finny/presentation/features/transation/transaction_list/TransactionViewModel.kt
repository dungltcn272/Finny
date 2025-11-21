package com.ltcn272.finny.presentation.features.transation.transaction_list

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.domain.model.DaySection
import com.ltcn272.finny.domain.model.TransactionFilter
import com.ltcn272.finny.domain.model.TransactionType
import com.ltcn272.finny.domain.repository.BudgetRepository
import com.ltcn272.finny.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlin.io.path.copyTo
import kotlin.io.path.exists

private fun getStartOfWeek(date: LocalDate): LocalDate =
    date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

private fun getEndOfWeek(date: LocalDate): LocalDate =
    date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

data class TransactionListUiState(
    val groupedTransactions: List<DaySection> = emptyList(),
    val timeRangeTitle: String = "",
    val budgets: List<Budget> = emptyList(),
    val selectedBudget: Budget? = null,
    val currency: String = "VND",
    val isBudgetLoading: Boolean = true,
    val isTransactionLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val showDatePicker: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val zoneId = ZoneId.systemDefault()
    private val today = LocalDate.now(zoneId)

    private val _filterState = MutableStateFlow(
        TransactionFilter(
            startDate = getStartOfWeek(today),
            endDate = getEndOfWeek(today),
            budgetId = null,
            selectedBudget = null
        )
    )

    private val _uiState = MutableStateFlow(TransactionListUiState())
    val uiState: StateFlow<TransactionListUiState> = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(
                timeRangeTitle = formatRangeTitle(
                    _filterState.value.startDate!!,
                    _filterState.value.endDate!!
                )
            )
        }

        viewModelScope.launch {
            budgetRepository.getLocalBudgets().collect { budgets ->
                _uiState.update {
                    it.copy(
                        budgets = budgets,
                        isBudgetLoading = false
                    )
                }
            }
        }

        viewModelScope.launch {
            delay(1000)
            _filterState.flatMapLatest { filter ->
                transactionRepository.getLocalTransactions(filter)
            }.collect { transactions ->
                val grouped = processAndGroupTransactions(transactions, _filterState.value)
                _uiState.update {
                    it.copy(
                        groupedTransactions = grouped,
                        isTransactionLoading = false
                    )
                }
            }
        }
    }

    private fun processAndGroupTransactions(
        transactions: List<com.ltcn272.finny.domain.model.Transaction>,
        filter: TransactionFilter
    ): List<DaySection> {
        val groupedByDate = transactions
            .groupBy { it.dateTime.toLocalDate() ?: today }
            .mapValues { (date, dailyTransactions) ->
                val total =
                    dailyTransactions.sumOf { if (it.type == TransactionType.INCOME) it.amount else -it.amount }
                DaySection(
                    date = date,
                    totalAmount = total,
                    transactions = dailyTransactions.sortedByDescending { it.dateTime }
                )
            }

        val finalGroups = groupedByDate.toMutableMap()
        val isTodayInRange = !today.isBefore(filter.startDate) && !today.isAfter(filter.endDate)
        if (isTodayInRange && !finalGroups.containsKey(today)) {
            finalGroups[today] =
                DaySection(date = today, totalAmount = 0.0, transactions = emptyList())
        }

        return finalGroups.values.sortedByDescending { it.date }
    }

    private fun updateFilter(newFilter: TransactionFilter) {
        _uiState.update {
            it.copy(
                timeRangeTitle = formatRangeTitle(newFilter.startDate!!, newFilter.endDate!!),
                selectedBudget = newFilter.selectedBudget
            )
        }
        _filterState.value = newFilter
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }

            delay(1000)

            _uiState.update { it.copy(isRefreshing = false) }

        }
    }

    fun setDateRange(start: LocalDate, end: LocalDate) {
        val newFilter = _filterState.value.copy(startDate = start, endDate = end)
        updateFilter(newFilter)
        onDismissDatePicker()
    }

    fun onShowDatePicker() {
        _uiState.update { it.copy(showDatePicker = true) }
    }

    fun onDismissDatePicker() {
        _uiState.update { it.copy(showDatePicker = false) }
    }

    fun setBudgetFilter(budget: Budget?) {
        val newFilter = _filterState.value.copy(budgetId = budget?.id, selectedBudget = budget)
        updateFilter(newFilter)
    }

    fun navigateToNextWeek() {
        val current = _filterState.value
        val nextWeekStart = current.startDate!!.plusWeeks(1)
        val newFilter = current.copy(
            startDate = getStartOfWeek(nextWeekStart),
            endDate = getEndOfWeek(nextWeekStart)
        )
        updateFilter(newFilter)
    }

    fun navigateToPreviousWeek() {
        val current = _filterState.value
        val prevWeekStart = current.startDate!!.minusWeeks(1)
        val newFilter = current.copy(
            startDate = getStartOfWeek(prevWeekStart),
            endDate = getEndOfWeek(prevWeekStart)
        )
        updateFilter(newFilter)
    }

    private fun formatRangeTitle(start: LocalDate, end: LocalDate): String {
        val startOfWeek = getStartOfWeek(today)
        val endOfWeek = getEndOfWeek(today)
        return if (start == startOfWeek && end == endOfWeek) {
            "This week"
        } else {
            val formatter = DateTimeFormatter.ofPattern("MMM dd", Locale.ENGLISH)
            "${start.format(formatter)} - ${end.format(formatter)}"
        }
    }
}

