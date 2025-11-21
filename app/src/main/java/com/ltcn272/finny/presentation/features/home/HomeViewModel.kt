package com.ltcn272.finny.presentation.features.home

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ltcn272.finny.domain.model.Transaction
import com.ltcn272.finny.domain.model.TransactionCategory
import com.ltcn272.finny.domain.model.TransactionFilter
import com.ltcn272.finny.domain.model.TransactionType
import com.ltcn272.finny.domain.repository.BudgetRepository
import com.ltcn272.finny.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.ehsannarmani.compose_charts.models.Pie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import javax.inject.Inject
import kotlin.random.Random

enum class TimeFilter(val displayName: String) {
    ALL("All"),
    THIS_MONTH("This Month"),
    THIS_WEEK("This Week"),
    TODAY("Today")
}

data class HomeUiState(
    val balance: Double = 0.0,
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val categoryExpenseBreakdown: List<Pie> = emptyList(),
    val budgetExpenseBreakdown: List<Pie> = emptyList(), // Changed to Pie
    val currency: String = "VND",
    val selectedTimeFilter: TimeFilter = TimeFilter.ALL
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository
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

        // Category Breakdown (Pie Chart)
        val categoryPieData = filteredTransactions
            .filter { it.type == TransactionType.OUTCOME }
            .groupBy { it.category }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }
            .map { (category, amount) ->
                Pie(
                    label = category.name.lowercase().replaceFirstChar { it.titlecase() },
                    data = amount,
                    color = getFixedColorForCategory(category)
                )
            }

        // Budget Breakdown (Pie Chart)
        val budgetMap = budgets.associateBy { it.id }
        val budgetPieData = filteredTransactions
            .filter { it.type == TransactionType.OUTCOME && it.budgetId != null }
            .groupBy { it.budgetId }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }
            .mapKeys { (budgetId, _) -> budgetMap[budgetId] }
            .filterKeys { it != null }
            .toList()
            .sortedByDescending { (_, amount) -> amount } // Sort from largest to smallest
            .map { (budget, amount) ->
                Pie(
                    label = budget!!.name,
                    data = amount,
                    color = generateRandomColor()
                )
            }

        HomeUiState(
            balance = balance,
            income = income,
            expense = expense,
            categoryExpenseBreakdown = categoryPieData,
            budgetExpenseBreakdown = budgetPieData, // Updated data
            selectedTimeFilter = filter
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun cycleTimeFilter() {
        val currentFilter = _timeFilter.value
        val nextFilterIndex = (currentFilter.ordinal + 1) % TimeFilter.values().size
        _timeFilter.value = TimeFilter.values()[nextFilterIndex]
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

    private fun getFixedColorForCategory(category: TransactionCategory): Color {
        return when (category) {
            TransactionCategory.FOOD -> Color(0xFFC56EFF)
            TransactionCategory.TRANSPORTATION -> Color(0xFF28E0B4)
            TransactionCategory.HOUSING -> Color(0xFF3A86FF)
            TransactionCategory.SHOPPING -> Color(0xFFFFBE0B)
            TransactionCategory.HEALTHCARE -> Color(0xFFFF006E)
            TransactionCategory.ENTERTAINMENT -> Color(0xFFFB5607)
            TransactionCategory.EDUCATION -> Color(0xFF2EC4B6)
            TransactionCategory.UTILITIES -> Color(0xFF00A896)
            TransactionCategory.SALARY -> Color(0xFF4CAF50)
            TransactionCategory.GIFT -> Color(0xFFE91E63)
            else -> Color.Gray
        }
    }

    private fun generateRandomColor(): Color {
        val random = Random.Default
        return Color(
            red = random.nextFloat(),
            green = random.nextFloat(),
            blue = random.nextFloat(),
            alpha = 1f
        )
    }

    fun formatCurrency(amount: Double, locale: Locale = Locale.getDefault(), currencyCode: String = "VND"): String {
        val format = NumberFormat.getCurrencyInstance(locale)
        try {
            val currency = java.util.Currency.getInstance(currencyCode)
            format.currency = currency
        } catch (e: Exception) {
            // fallback
        }
        return format.format(amount)
    }
}