package com.ltcn272.finny.presentation.features.dashboard

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.*
import com.ltcn272.finny.domain.repository.DashboardRepository
import com.ltcn272.finny.domain.util.AppResult
import com.ltcn272.finny.domain.util.ErrorType
import com.ltcn272.finny.presentation.common.ui.DonutData
import com.ltcn272.finny.presentation.common.util.formatCurrencyNonComposable
import com.ltcn272.finny.presentation.common.util.generateHarmonicColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import javax.inject.Inject

enum class OverviewPeriod(val value: String) {
    WEEK("week"), MONTH("month")
}

enum class ReportTab { BUDGETS, CATEGORIES }

enum class DashboardMode { OVERVIEW, REPORT }

data class DashboardUiState(
    val dashboardMode: DashboardMode = DashboardMode.OVERVIEW,
    val isLoading: Boolean = true,

    val selectedTab: ReportTab = ReportTab.BUDGETS,
    val timeRangeTitle: String = "",
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate = LocalDate.now(),
    val showDatePicker: Boolean = false,

    val budgetReportData: BudgetReport? = null,
    val categoryReportData: CategoryReport? = null,

    val activeReportData: Report? = null,
    val activeDonutChartData: List<DonutData> = emptyList(),

    val selectedPeriod: OverviewPeriod = OverviewPeriod.WEEK,
    val overviewData: DashboardOverview? = null,
    val aiInsight: String? = null,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val app: Application,
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _dashboardErrorEvent = MutableSharedFlow<String>()
    val dashboardErrorEvent = _dashboardErrorEvent.asSharedFlow()

    init {
        val today = LocalDate.now()
        val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        _uiState.update { it.copy(startDate = startOfWeek, endDate = endOfWeek) }


        loadDataForCurrentMode()

        viewModelScope.launch {
            _uiState.map { it.startDate to it.endDate }.distinctUntilChanged().collect { (start, end) ->
                updateTimeRangeTitle(start, end)
                if (_uiState.value.dashboardMode == DashboardMode.REPORT) {
                    loadReportData()
                }
            }
        }
    }

    private fun loadDataForCurrentMode() {
        when (_uiState.value.dashboardMode) {
            DashboardMode.OVERVIEW -> loadOverviewData()
            DashboardMode.REPORT -> {
                if (_uiState.value.budgetReportData == null) {
                    loadReportData()
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                    updateActiveReportData()
                }
            }
        }
    }

    fun onPeriodSelected(period: OverviewPeriod) {
        if (period == _uiState.value.selectedPeriod) return
        _uiState.update { it.copy(selectedPeriod = period) }
        loadOverviewData()
    }

    private fun loadOverviewData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val period = _uiState.value.selectedPeriod.value

            val overviewResultDeferred = async { dashboardRepository.getOverview(period) }
            val aiInsightResultDeferred = async { dashboardRepository.getAiReport(period) }

            val overviewResult = overviewResultDeferred.await()
            val aiInsightResult = aiInsightResultDeferred.await()

            val overviewData = if (overviewResult is AppResult.Success) overviewResult.data else null
            val aiInsight = if (aiInsightResult is AppResult.Success) aiInsightResult.data else null

            if (overviewResult is AppResult.Error) {
                _dashboardErrorEvent.emit(mapErrorToString(overviewResult.errorType))
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    overviewData = overviewData,
                    aiInsight = aiInsight,
                )
            }
        }
    }

    private fun loadReportData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val zoneId = ZoneId.systemDefault()
            val start = _uiState.value.startDate.atStartOfDay(zoneId)
            val end = _uiState.value.endDate.atTime(23, 59, 59).atZone(zoneId)

            val budgetResultDeferred = async { dashboardRepository.getBudgetReport(start, end) }
            val categoryResultDeferred = async { dashboardRepository.getCategoryReport(start, end) }

            val budgetResult = budgetResultDeferred.await()
            val categoryResult = categoryResultDeferred.await()

            var hasError = false
            if (budgetResult is AppResult.Error) {
                _dashboardErrorEvent.emit(mapErrorToString(budgetResult.errorType))
                hasError = true
            }
            if (categoryResult is AppResult.Error) {
                _dashboardErrorEvent.emit(mapErrorToString(categoryResult.errorType))
                hasError = true
            }

            if (!hasError) {
                val budgetData = (budgetResult as AppResult.Success).data
                val categoryData = (categoryResult as AppResult.Success).data
                _uiState.update {
                    it.copy(
                        budgetReportData = budgetData,
                        categoryReportData = categoryData
                    )
                }
            }

            _uiState.update { it.copy(isLoading = false) }
            updateActiveReportData()
        }
    }

    fun toggleDashboardMode() {
        _uiState.update {
            val newMode = if (it.dashboardMode == DashboardMode.REPORT) {
                DashboardMode.OVERVIEW
            } else {
                DashboardMode.REPORT
            }
            it.copy(dashboardMode = newMode, isLoading = true)
        }
        loadDataForCurrentMode()
    }

    fun onTabSelected(tab: ReportTab) {
        if (_uiState.value.selectedTab == tab) return
        _uiState.update { it.copy(selectedTab = tab) }
        updateActiveReportData()
    }

    private fun updateActiveReportData() {
        _uiState.update { currentState ->
            val activeReport = when (currentState.selectedTab) {
                ReportTab.BUDGETS -> currentState.budgetReportData
                ReportTab.CATEGORIES -> currentState.categoryReportData
            }

            val donutData = if (activeReport != null) {
                createDonutChartData(activeReport.detailItems)
            } else {
                emptyList()
            }

            currentState.copy(
                activeReportData = activeReport,
                activeDonutChartData = donutData
            )
        }
    }

    private fun updateTimeRangeTitle(start: LocalDate, end: LocalDate) {
        val today = LocalDate.now()
        val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        val title = if (start.isEqual(startOfWeek) && ChronoUnit.DAYS.between(start, end) == 6L) {
            app.getString(R.string.this_week)
        } else if (start.dayOfMonth == 1 && end.dayOfMonth == end.lengthOfMonth()) {
            start.format(DateTimeFormatter.ofPattern("MMMM, yyyy", Locale.getDefault()))
        } else {
            val startFormatted = start.format(DateTimeFormatter.ofPattern("dd/MM/yy"))
            val endFormatted = end.format(DateTimeFormatter.ofPattern("dd/MM/yy"))
            "$startFormatted - $endFormatted"
        }
        _uiState.update { it.copy(timeRangeTitle = title) }
    }


    fun setDateRange(start: LocalDate, end: LocalDate) {
        _uiState.update { it.copy(startDate = start, endDate = end, showDatePicker = false) }
    }

    fun previousDateRange() {
        _uiState.update {
            it.copy(
                startDate = it.startDate.minusWeeks(1),
                endDate = it.endDate.minusWeeks(1)
            )
        }
    }

    fun nextDateRange() {
        _uiState.update {
            it.copy(
                startDate = it.startDate.plusWeeks(1),
                endDate = it.endDate.plusWeeks(1)
            )
        }
    }

    fun onShowDatePicker() {
        _uiState.update { it.copy(showDatePicker = true) }
    }

    fun onDismissDatePicker() {
        _uiState.update { it.copy(showDatePicker = false) }
    }

    private fun createDonutChartData(items: List<Any>): List<DonutData> {
        val chartItems = items.mapNotNull {
            when (it) {
                is BudgetReportDetail -> if (it.outcome > 0) DonutData(it.name, it.outcome.toFloat(), formatCurrencyNonComposable(it.outcome, "VND")) else null
                is CategoryReportDetail -> if (it.outcome > 0) DonutData(it.name, it.outcome.toFloat(), formatCurrencyNonComposable(it.outcome, "VND")) else null
                else -> null
            }
        }
        if (chartItems.isEmpty()) return emptyList()

        val baseColor = Color(0xFFFFA726)
        val dynamicColors = generateHarmonicColors(baseColor, chartItems.size)

        return chartItems.mapIndexed { index, item ->
            item.copy(color = dynamicColors.getOrElse(index) { Color.Gray })
        }
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
