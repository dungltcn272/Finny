package com.ltcn272.finny.presentation.features.transaction.transaction_list

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.domain.model.Transaction
import com.ltcn272.finny.domain.model.TransactionFilter
import com.ltcn272.finny.domain.repository.BudgetRepository
import com.ltcn272.finny.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import javax.inject.Inject

private fun getStartOfWeek(date: LocalDate): LocalDate =
    date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

private fun getEndOfWeek(date: LocalDate): LocalDate =
    date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

data class TransactionListUiState(
    val timeRangeTitle: String = "",
    val selectedBudget: Budget? = null,
    val currency: String = "VND",
    val showDatePicker: Boolean = false,
    val isRefreshingByUser: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val zoneId = ZoneId.systemDefault()
    private val today = LocalDate.now(zoneId)

    private val _filterState = MutableStateFlow(
        TransactionFilter(
            startDate = getStartOfWeek(today),
            endDate = getEndOfWeek(today),
            budgetId = null
        )
    )

    private val _uiState = MutableStateFlow(TransactionListUiState())
    val uiState: StateFlow<TransactionListUiState> = _uiState.asStateFlow()

    val budgetsPagingFlow: Flow<PagingData<Budget>> = budgetRepository.getBudgets()
        .cachedIn(viewModelScope)

    val transactionsPagingFlow: Flow<PagingData<Transaction>> = _filterState
        .flatMapLatest { currentFilter ->
            transactionRepository.getTransactions(currentFilter)
        }
        .cachedIn(viewModelScope)

    init {
        _uiState.update { it.copy(timeRangeTitle = formatRangeTitle(_filterState.value.startDate!!, _filterState.value.endDate!!)) }
    }

    fun onUserPullToRefresh() {
        _uiState.update { it.copy(isRefreshingByUser = true) }
    }

    fun onRefreshFinished() {
        _uiState.update { it.copy(isRefreshingByUser = false) }
    }

    fun setBudgetFilter(budget: Budget?) {
        _uiState.update { it.copy(selectedBudget = budget) }
        _filterState.update { it.copy(budgetId = budget?.serverId) }
    }

    private fun updateDateRange(start: LocalDate, end: LocalDate) {
        _filterState.update { it.copy(startDate = start, endDate = end) }
        _uiState.update { it.copy(timeRangeTitle = formatRangeTitle(start, end)) }
    }

    fun setDateRange(start: LocalDate, end: LocalDate) {
        updateDateRange(start, end)
        onDismissDatePicker()
    }

    fun onShowDatePicker() {
        _uiState.update { it.copy(showDatePicker = true) }
    }

    fun onDismissDatePicker() {
        _uiState.update { it.copy(showDatePicker = false) }
    }

    fun navigateToNextWeek() {
        val currentStart = _filterState.value.startDate!!
        val nextWeekStart = currentStart.plusWeeks(1)
        updateDateRange(getStartOfWeek(nextWeekStart), getEndOfWeek(nextWeekStart))
    }

    fun navigateToPreviousWeek() {
        val currentStart = _filterState.value.startDate!!
        val prevWeekStart = currentStart.minusWeeks(1)
        updateDateRange(getStartOfWeek(prevWeekStart), getEndOfWeek(prevWeekStart))
    }

    private fun formatRangeTitle(start: LocalDate, end: LocalDate): String {
        val today = LocalDate.now(zoneId)
        val startOfWeek = getStartOfWeek(today)

        return if (start.isEqual(startOfWeek) && ChronoUnit.DAYS.between(start, end) == 6L) {
            context.getString(R.string.this_week)
        } else if (start.dayOfMonth == 1 && end.dayOfMonth == end.lengthOfMonth()) {
            start.format(DateTimeFormatter.ofPattern("MMMM, yyyy", Locale.getDefault()))
        } else {
            val startFormatted = start.format(DateTimeFormatter.ofPattern("dd/MM/yy"))
            val endFormatted = end.format(DateTimeFormatter.ofPattern("dd/MM/yy"))
            "$startFormatted - $endFormatted"
        }
    }
}
