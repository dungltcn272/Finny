package com.ltcn272.finny.presentation.features.dashboard

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ltcn272.finny.domain.model.BudgetReport
import com.ltcn272.finny.domain.model.CategoryReport
import com.ltcn272.finny.domain.model.ReportTotals
import com.ltcn272.finny.domain.repository.DashboardRepository
import com.ltcn272.finny.domain.util.AppResult
import com.ltcn272.finny.presentation.common.ui.DonutData
import com.ltcn272.finny.presentation.common.util.formatCurrencyNonComposable
import com.ltcn272.finny.presentation.common.util.generateHarmonicColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

enum class ReportTab { BUDGETS, CATEGORIES }

data class DashboardUiState(
    val isLoading: Boolean = true,
    val selectedTab: ReportTab = ReportTab.BUDGETS,
    val startDate: ZonedDateTime = ZonedDateTime.now(),
    val endDate: ZonedDateTime = ZonedDateTime.now(),
    val showDatePicker: Boolean = false, // <<< ADD THIS STATE

    val totals: ReportTotals? = null,

    // Processed data for UI
    val donutChartData: List<DonutData> = emptyList(),
    val reportDetailItems: List<Any> = emptyList(),
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    private var budgetReport: BudgetReport? = null
    private var categoryReport: CategoryReport? = null

    init {
        val now = ZonedDateTime.now()
        val startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toLocalDate().atStartOfDay(now.zone)
        val endOfWeek = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).toLocalDate().atTime(23, 59, 59).atZone(now.zone)
        setDateRangeAndFetch(startOfWeek, endOfWeek)
    }

    fun onTabSelected(tab: ReportTab) {
        if (tab == _uiState.value.selectedTab) return
        _uiState.update { it.copy(selectedTab = tab) }
        processDataForUi()
    }

    // --- NEW FUNCTIONS TO HANDLE DATE PICKER ---
    fun onShowDatePicker() {
        _uiState.update { it.copy(showDatePicker = true) }
    }

    fun onDismissDatePicker() {
        _uiState.update { it.copy(showDatePicker = false) }
    }

    fun setDateRange(start: LocalDate, end: LocalDate) {
        onDismissDatePicker()
        val zone = _uiState.value.startDate.zone
        val startDateTime = start.atStartOfDay(zone)
        val endDateTime = end.atTime(23, 59, 59).atZone(zone)
        setDateRangeAndFetch(startDateTime, endDateTime)
    }
    // ------------------------------------------

    fun nextDateRange() {
        val currentStart = _uiState.value.startDate
        val newStart = currentStart.plusWeeks(1)
        val newEnd = newStart.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).toLocalDate().atTime(23, 59, 59).atZone(currentStart.zone)
        setDateRangeAndFetch(newStart, newEnd)
    }

    fun previousDateRange() {
        val currentStart = _uiState.value.startDate
        val newStart = currentStart.minusWeeks(1)
        val newEnd = newStart.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).toLocalDate().atTime(23, 59, 59).atZone(currentStart.zone)
        setDateRangeAndFetch(newStart, newEnd)
    }

    private fun setDateRangeAndFetch(start: ZonedDateTime, end: ZonedDateTime) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, startDate = start, endDate = end) }

            val budgetResultDeferred = viewModelScope.launch {
                when (val result = dashboardRepository.getBudgetReport(start, end)) {
                    is AppResult.Success -> budgetReport = result.data
                    is AppResult.Error -> budgetReport = null
                    is AppResult.Loading -> { /* No-op */ }
                }
            }
            val categoryResultDeferred = viewModelScope.launch {
                when (val result = dashboardRepository.getCategoryReport(start, end)) {
                    is AppResult.Success -> categoryReport = result.data
                    is AppResult.Error -> categoryReport = null
                    is AppResult.Loading -> { /* No-op */ }
                }
            }

            joinAll(budgetResultDeferred, categoryResultDeferred)
            processDataForUi()
        }
    }

    private fun processDataForUi() {
        val selectedTab = _uiState.value.selectedTab
        val (totals, pieItems, detailItems) = if (selectedTab == ReportTab.BUDGETS) {
            Triple(budgetReport?.totals, budgetReport?.pieItems, budgetReport?.detailItems)
        } else {
            Triple(categoryReport?.totals, categoryReport?.pieItems, categoryReport?.detailItems)
        }

        if (totals == null || pieItems == null || detailItems == null) {
            _uiState.update { it.copy(isLoading = false, totals = null, donutChartData = emptyList(), reportDetailItems = emptyList()) }
            return
        }

        val baseColor = Color(0xFFFFA726) // Orange color
        val colors = generateHarmonicColors(baseColor, pieItems.size)
        val donutData = pieItems.mapIndexed { index, pieItem ->
            DonutData(
                label = pieItem.label,
                value = pieItem.value.toFloat(),
                amountText = formatCurrencyNonComposable(pieItem.value, "VND"),
                color = colors[index]
            )
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                totals = totals,
                donutChartData = donutData,
                reportDetailItems = detailItems
            )
        }
    }
}
