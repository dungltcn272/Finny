package com.ltcn272.finny.presentation.features.budget.budget_detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.domain.model.Transaction
import com.ltcn272.finny.domain.model.TransactionFilter
import com.ltcn272.finny.domain.model.TransactionType
import com.ltcn272.finny.domain.repository.BudgetRepository
import com.ltcn272.finny.domain.repository.TransactionRepository
import com.ltcn272.finny.domain.util.AppResult
import com.ltcn272.finny.presentation.common.ui.LineChartData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month // THÊM IMPORT
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle // THÊM IMPORT
import java.time.temporal.TemporalAdjusters
import java.util.Locale // THÊM IMPORT
import javax.inject.Inject

// Data class và Enum không thay đổi
enum class ChartTimeRange {
    WEEK, MONTH
}

data class BudgetDetailUiState(
    val budget: Budget? = null,
    val timeRange: ChartTimeRange = ChartTimeRange.WEEK,
    val incomeChartData: List<LineChartData> = emptyList(),
    val outcomeChartData: List<LineChartData> = emptyList(),
    val transactionCount: Int = 0,
    val netAmount: Double = 0.0,
    val isStatisticsLoading: Boolean = true,
    val isRefreshingByUser: Boolean = false,
    val currency: String = "VND",
)

sealed class BudgetDetailEvent {
    data object DeleteSuccess : BudgetDetailEvent()
    data class Error(val message: String) : BudgetDetailEvent()
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BudgetDetailViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository // THÊM REPOSITORY
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetDetailUiState())
    val uiState: StateFlow<BudgetDetailUiState> = _uiState.asStateFlow()

    // THÊM SHARED FLOW CHO EVENT
    private val _eventFlow = MutableSharedFlow<BudgetDetailEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val _filterStateForPaging = MutableStateFlow(TransactionFilter())

    private var isInitialized = false
    private val TAG = "BudgetDetailVM"
    private var allTransactions: List<Transaction> = emptyList()

    val transactionsPagingFlow: Flow<PagingData<Transaction>> = _filterStateForPaging
        .flatMapLatest { currentFilter ->
            if (currentFilter.budgetId.isNullOrEmpty()) {
                emptyFlow()
            } else {
                transactionRepository.getTransactions(currentFilter)
            }
        }
        .cachedIn(viewModelScope)

    fun initialize(budget: Budget) {
        if (isInitialized) return
        isInitialized = true

        Log.d(TAG, "--- initialize() --- Budget nhận được: ${budget.name}")

        _uiState.update {
            it.copy(
                budget = budget,
                currency = budget.currency,
                // Lấy các giá trị ban đầu từ budget để UI có cái hiển thị ngay
                transactionCount = 0, // Sẽ được cập nhật
                netAmount = budget.totalIncome - budget.totalOutcome,
                isStatisticsLoading = true // <<< BẬT LOADING MỚI
            )
        }

        loadAllTransactionDetails(budget.serverId)
    }

    private fun loadAllTransactionDetails(budgetId: String?) {
        if (budgetId.isNullOrEmpty()) {
            _uiState.update { it.copy(isStatisticsLoading = false) } // Tắt loading nếu có lỗi
            Log.e(TAG, "loadAllTransactionDetails thất bại vì budgetId rỗng.")
            return
        }

        viewModelScope.launch {
            if(!_uiState.value.isRefreshingByUser) {
                _uiState.update { it.copy(isStatisticsLoading = true) }
            }

            val result = transactionRepository.getAllTransactionsByBudget(budgetId)

            when (result) {
                is AppResult.Success -> {
                    val transactionList = result.data
                    this@BudgetDetailViewModel.allTransactions = transactionList
                    Log.d(TAG, "Repo đã trả về thành công ${transactionList.size} giao dịch.")

                    val totalIncome = transactionList.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                    val totalOutcome = transactionList.filter { it.type == TransactionType.OUTCOME }.sumOf { it.amount }

                    _uiState.update {
                        it.copy(
                            transactionCount = transactionList.size,
                            netAmount = totalIncome - totalOutcome,
                            budget = it.budget?.copy(
                                totalIncome = totalIncome,
                                totalOutcome = totalOutcome
                            ),
                            isStatisticsLoading = false, // <<< TẮT LOADING
                            isRefreshingByUser = false
                        )
                    }
                    updateChartData(uiState.value.timeRange)
                }
                is AppResult.Error -> {
                    Log.e(TAG, "Lỗi khi lấy tất cả giao dịch: ${result.errorType}")
                    _uiState.update { it.copy(isStatisticsLoading = false, isRefreshingByUser = false) } // <<< TẮT LOADING
                }
                is AppResult.Loading -> {}
            }

            _filterStateForPaging.update {
                it.copy(
                    budgetId = budgetId,
                    startDate = getStartOfWeek(),
                    endDate = getEndOfWeek()
                )
            }
        }
    }

    // --- SỬA LẠI HÀM NÀY ---
    private fun updateChartData(range: ChartTimeRange) {
        if (range == ChartTimeRange.WEEK) {
            updateChartDataForWeek()
        } else {
            updateChartDataForYear()
        }
    }

    private fun updateChartDataForWeek() {
        val config = ChartConfig(
            startDate = getStartOfWeek(),
            endDate = getEndOfWeek(),
            labels = (0..6).map { getStartOfWeek().plusDays(it.toLong()) },
            dateFormat = "EEE"
        )

        val relevantTransactions = allTransactions.filter {
            val date = it.dateTime.toLocalDate()
            !date.isBefore(config.startDate) && !date.isAfter(config.endDate)
        }
        Log.d(TAG, "Có ${relevantTransactions.size} giao dịch phù hợp cho biểu đồ WEEK")

        val incomeByDate = relevantTransactions
            .filter { it.type == TransactionType.INCOME }
            .groupBy { it.dateTime.toLocalDate() }
            .mapValues { (_, trans) -> trans.sumOf { it.amount }.toFloat() }

        val outcomeByDate = relevantTransactions
            .filter { it.type == TransactionType.OUTCOME }
            .groupBy { it.dateTime.toLocalDate() }
            .mapValues { (_, trans) -> trans.sumOf { it.amount }.toFloat() }

        val incomeChartData = config.labels.map { date ->
            LineChartData(date.format(DateTimeFormatter.ofPattern(config.dateFormat, Locale("vi"))), incomeByDate[date] ?: 0f)
        }

        val outcomeChartData = config.labels.map { date ->
            LineChartData(date.format(DateTimeFormatter.ofPattern(config.dateFormat, Locale("vi"))), outcomeByDate[date] ?: 0f)
        }

        _uiState.update {
            it.copy(
                incomeChartData = incomeChartData,
                outcomeChartData = outcomeChartData
            )
        }
    }

    private fun updateChartDataForYear() {
        val currentYear = LocalDate.now().year
        val startOfYear = LocalDate.of(currentYear, 1, 1)
        val endOfYear = LocalDate.of(currentYear, 12, 31)

        val relevantTransactions = allTransactions.filter {
            val date = it.dateTime.toLocalDate()
            !date.isBefore(startOfYear) && !date.isAfter(endOfYear)
        }
        Log.d(TAG, "Có ${relevantTransactions.size} giao dịch phù hợp cho biểu đồ MONTH (cả năm)")

        val incomeByMonth = relevantTransactions
            .filter { it.type == TransactionType.INCOME }
            .groupBy { it.dateTime.month } // Nhóm theo tháng
            .mapValues { (_, trans) -> trans.sumOf { it.amount }.toFloat() }

        val outcomeByMonth = relevantTransactions
            .filter { it.type == TransactionType.OUTCOME }
            .groupBy { it.dateTime.month } // Nhóm theo tháng
            .mapValues { (_, trans) -> trans.sumOf { it.amount }.toFloat() }

        // Tạo labels cho 12 tháng
        val allMonths = Month.entries
        val vietnameseLocale = Locale("vi")

        val incomeChartData = allMonths.map { month ->
            LineChartData(
                label = "T${month.value}", // "T1", "T2", ...
                value = incomeByMonth[month] ?: 0f
            )
        }

        val outcomeChartData = allMonths.map { month ->
            LineChartData(
                label = "T${month.value}", // "T1", "T2", ...
                value = outcomeByMonth[month] ?: 0f
            )
        }

        _uiState.update {
            it.copy(
                incomeChartData = incomeChartData,
                outcomeChartData = outcomeChartData
            )
        }
    }

    fun onTimeRangeSelected(range: ChartTimeRange) {
        if (range == uiState.value.timeRange) return
        _uiState.update { it.copy(timeRange = range) }
        // Chỉ cần gọi lại updateChartData với time range mới, không cần gọi lại API
        updateChartData(range)

        // Cập nhật lại filter cho Paging
        val startDate = if (range == ChartTimeRange.WEEK) getStartOfWeek() else getStartOfMonth()
        val endDate = if (range == ChartTimeRange.WEEK) getEndOfWeek() else getEndOfMonth()
        _filterStateForPaging.update { it.copy(startDate = startDate, endDate = endDate) }
    }

    fun onUserPullToRefresh() {
        if (uiState.value.isRefreshingByUser) return
        _uiState.update { it.copy(isRefreshingByUser = true) }
        // Khi người dùng kéo để làm mới, gọi lại hàm tải tất cả dữ liệu
        loadAllTransactionDetails(uiState.value.budget?.serverId)
    }

    fun deleteBudget() {
        viewModelScope.launch {
            val budgetToDelete = uiState.value.budget
            if (budgetToDelete?.serverId == null) {
                _eventFlow.emit(BudgetDetailEvent.Error("Không tìm thấy ID ngân sách để xóa."))
                return@launch
            }
            budgetRepository.deleteBudget(budgetToDelete.serverId).collectLatest { result ->
                when(result) {
                    is AppResult.Success -> {
                        Log.d(TAG, "Xóa budget thành công.")
                        _eventFlow.emit(BudgetDetailEvent.DeleteSuccess)
                    }
                    is AppResult.Error -> {
                        val errorMessage = "Lỗi khi xóa: ${result.errorType}"
                        Log.e(TAG, errorMessage)
                        _eventFlow.emit(BudgetDetailEvent.Error(errorMessage))
                    }
                    is AppResult.Loading -> {}
                }
            }
        }
    }

    // Data class nhỏ này chỉ dùng nội bộ cho hàm updateChartDataForWeek
    private data class ChartConfig(
        val startDate: LocalDate,
        val endDate: LocalDate,
        val labels: List<LocalDate>,
        val dateFormat: String
    )

    private fun getStartOfWeek(): LocalDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    private fun getEndOfWeek(): LocalDate = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    private fun getStartOfMonth(): LocalDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth())
    private fun getEndOfMonth(): LocalDate = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth())
}
