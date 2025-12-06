package com.ltcn272.finny.presentation.features.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.Transaction
import com.ltcn272.finny.domain.model.TransactionFilter
import com.ltcn272.finny.domain.model.TransactionType
import com.ltcn272.finny.domain.repository.BudgetRepository
import com.ltcn272.finny.domain.repository.TransactionRepository
import com.ltcn272.finny.presentation.common.util.BudgetColorUtils
import com.ltcn272.finny.presentation.common.util.CategoryUtils
import com.netguru.multiplatform.charts.bubble.Bubble
import com.netguru.multiplatform.charts.pie.PieChartData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.ehsannarmani.compose_charts.models.Line
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import androidx.compose.ui.graphics.Color as UiColor
import androidx.compose.ui.graphics.SolidColor

enum class TimeFilter(val displayNameRes: Int) {
    ALL(R.string.all),
    THIS_MONTH(R.string.this_month),
    THIS_WEEK(R.string.this_week),
    TODAY(R.string.today)
}

data class HomeUiState(
    val balance: Double = 0.0,
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val categoryBubbleData: List<Bubble> = emptyList(),
    val budgetExpenseBreakdown: List<PieChartData> = emptyList(),
    val currency: String = "VND",
    val selectedTimeFilter: TimeFilter = TimeFilter.ALL,
    // Trend chart additions
    val trendTitle: String? = null,
    val trendLabels: List<String> = emptyList(),
    val trendLines: List<Line> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    budgetRepository: BudgetRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _timeFilter = MutableStateFlow(TimeFilter.ALL)

    private val allTransactions: StateFlow<List<Transaction>> =
        transactionRepository.getLocalTransactions(TransactionFilter())
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private val allBudgets = budgetRepository.getLocalBudgets()

    val uiState: StateFlow<HomeUiState> = combine(
        allTransactions, allBudgets, _timeFilter
    ) { transactions, budgets, filter ->
        val filteredTransactions = filterTransactions(transactions, filter)

        val income = filteredTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = filteredTransactions.filter { it.type == TransactionType.OUTCOME }.sumOf { it.amount }
        val balance = income - expense

        val categoryBubbleData = filteredTransactions
            .filter { it.type == TransactionType.OUTCOME }
            .groupBy { it.category }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }
            .map { (category, amount) ->
                val style = CategoryUtils.getStyle(category)
                Bubble(
                    name = category.name.lowercase().replaceFirstChar { it.titlecase() },
                    value = amount.toFloat(),
                    icon = style.icon,
                    color = style.color
                )
            }

        val budgetMap = budgets.associateBy { it.id }
        val budgetPieData = filteredTransactions
            .filter { it.type == TransactionType.OUTCOME && it.budgetId.isNotBlank() }
            .groupBy { it.budgetId }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }
            .mapKeys { (budgetId, _) -> budgetMap[budgetId] }
            .filterKeys { it != null }
            .toList()
            .sortedByDescending { (_, amount) -> amount }
            .map { (budget, amount) ->
                PieChartData(
                    name = budget!!.name,
                    value = amount,
                    color = BudgetColorUtils.generateRandomColor()
                )
            }

        // Build trend data from ALL transactions (not filtered) per requirement
        val trendData = buildTrendData(allTransactions.value)

        HomeUiState(
            balance = balance,
            income = income,
            expense = expense,
            categoryBubbleData = categoryBubbleData,
            budgetExpenseBreakdown = budgetPieData,
            selectedTimeFilter = filter,
            trendTitle = trendData?.first,
            trendLabels = trendData?.second ?: emptyList(),
            trendLines = trendData?.third ?: emptyList()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun cycleTimeFilter() {
        val currentFilter = _timeFilter.value
        val nextFilterIndex = (currentFilter.ordinal + 1) % TimeFilter.entries.size
        _timeFilter.value = TimeFilter.entries[nextFilterIndex]
    }

    private fun filterTransactions(transactions: List<Transaction>, filter: TimeFilter): List<Transaction> {
        val now = LocalDate.now()
        return when (filter) {
            TimeFilter.ALL -> transactions
            TimeFilter.TODAY -> transactions.filter { it.dateTime.toLocalDate() == now }
            TimeFilter.THIS_WEEK -> {
                val startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val endOfWeek = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                transactions.filter {
                    val txDate = it.dateTime.toLocalDate()
                    !txDate.isBefore(startOfWeek) && !txDate.isAfter(endOfWeek)
                }
            }
            TimeFilter.THIS_MONTH -> {
                val startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth())
                val endOfMonth = now.with(TemporalAdjusters.lastDayOfMonth())
                transactions.filter {
                    val txDate = it.dateTime.toLocalDate()
                    !txDate.isBefore(startOfMonth) && !txDate.isAfter(endOfMonth)
                }
            }
        }
    }

    private fun buildTrendData(transactions: List<Transaction>): Triple<String, List<String>, List<Line>>? {
        if (transactions.isEmpty()) return null
        val byDate = transactions.groupBy { it.dateTime.toLocalDate() }
        val distinctDays = byDate.keys.sorted()
        if (distinctDays.size <= 1) return null // Placeholder rule

        // Distinct months
        val months = distinctDays.map { java.time.YearMonth.from(it) }.distinct().sorted()
        val nowMonth = months.maxOrNull()
        if (months.size >= 6 && nowMonth != null) {
            val last6 = (0 until 6).map { nowMonth.minusMonths((5 - it).toLong()) }
            return aggregateMonths(last6, transactions, title = context.getString(R.string.last_6_months))
        }
        if (months.size >= 2 && nowMonth != null) {
            val lastN = months.takeLast(months.size) // already sorted ascending
            return aggregateMonths(lastN, transactions, title = context.getString(R.string.last_n_months, lastN.size))
        }
        // Weeks fallback (ISO week-based year)
        val weekFields = java.time.temporal.WeekFields.ISO
        val weeks = distinctDays.map { it.get(weekFields.weekOfWeekBasedYear()) to it.get(weekFields.weekBasedYear()) }
            .distinct()
            .sortedBy { it.second * 100 + it.first } // year-week ordering
        if (weeks.size < 2) {
            // Days fallback (>1 day guaranteed here)
            return aggregateDays(distinctDays, transactions, title = context.getString(R.string.last_n_days, distinctDays.size))
        }
        val lastWeeks = weeks.takeLast(weeks.size) // all weeks available
        return aggregateWeeks(lastWeeks, transactions, title = context.getString(R.string.last_n_weeks, lastWeeks.size))
    }

    private fun aggregateMonths(months: List<java.time.YearMonth>, transactions: List<Transaction>, title: String): Triple<String, List<String>, List<Line>> {
        val labels = months.map { it.monthValue.toString() + "/" + (it.year % 100) }
        val incomeValues = months.map { m -> transactions.filter { java.time.YearMonth.from(it.dateTime.toLocalDate()) == m && it.type == TransactionType.INCOME }.sumOf { it.amount } }
        val expenseValues = months.map { m -> transactions.filter { java.time.YearMonth.from(it.dateTime.toLocalDate()) == m && it.type == TransactionType.OUTCOME }.sumOf { it.amount } }
        val lines = listOf(
            Line(label = context.getString(R.string.thu), values = incomeValues, color = SolidColor(UiColor(0xFF4CAF50)), curvedEdges = true),
            Line(label = context.getString(R.string.chi), values = expenseValues, color = SolidColor(UiColor(0xFFF44336)), curvedEdges = true)
        )
        return Triple(title, labels, lines)
    }

    private fun aggregateWeeks(weeks: List<Pair<Int, Int>>, transactions: List<Transaction>, title: String): Triple<String, List<String>, List<Line>> {
        val labels = weeks.map { "W" + it.first }
        val weekFields = java.time.temporal.WeekFields.ISO
        val incomeValues = weeks.map { (w, y) ->
            transactions.filter {
                val d = it.dateTime.toLocalDate()
                d.get(weekFields.weekOfWeekBasedYear()) == w && d.get(weekFields.weekBasedYear()) == y && it.type == TransactionType.INCOME
            }.sumOf { it.amount }
        }
        val expenseValues = weeks.map { (w, y) ->
            transactions.filter {
                val d = it.dateTime.toLocalDate()
                d.get(weekFields.weekOfWeekBasedYear()) == w && d.get(weekFields.weekBasedYear()) == y && it.type == TransactionType.OUTCOME
            }.sumOf { it.amount }
        }
        val lines = listOf(
            Line(label = context.getString(R.string.thu), values = incomeValues, color = SolidColor(UiColor(0xFF4CAF50)), curvedEdges = true),
            Line(label = context.getString(R.string.chi), values = expenseValues, color = SolidColor(UiColor(0xFFF44336)), curvedEdges = true)
        )
        return Triple(title, labels, lines)
    }

    private fun aggregateDays(days: List<LocalDate>, transactions: List<Transaction>, title: String): Triple<String, List<String>, List<Line>> {
        val labels = days.map { it.dayOfMonth.toString() }
        val incomeValues = days.map { d -> transactions.filter { it.dateTime.toLocalDate() == d && it.type == TransactionType.INCOME }.sumOf { it.amount } }
        val expenseValues = days.map { d -> transactions.filter { it.dateTime.toLocalDate() == d && it.type == TransactionType.OUTCOME }.sumOf { it.amount } }
        val lines = listOf(
            Line(label = context.getString(R.string.thu), values = incomeValues, color = SolidColor(UiColor(0xFF4CAF50)), curvedEdges = true),
            Line(label = context.getString(R.string.chi), values = expenseValues, color = SolidColor(UiColor(0xFFF44336)), curvedEdges = true)
        )
        return Triple(title, labels, lines)
    }
}