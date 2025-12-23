package com.ltcn272.finny.presentation.features.transaction.create_edit

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.domain.model.Category
import com.ltcn272.finny.domain.model.RecurringIntervalUnit
import com.ltcn272.finny.domain.model.RecurringTransactionInfo
import com.ltcn272.finny.domain.model.Transaction
import com.ltcn272.finny.domain.model.TransactionType
import com.ltcn272.finny.domain.repository.BudgetRepository
import com.ltcn272.finny.domain.repository.CategoryRepository
import com.ltcn272.finny.domain.repository.TransactionRepository
import com.ltcn272.finny.domain.util.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

enum class TransactionMode { CREATE, EDIT }

data class CreateEditTransactionUiState(
    val mode: TransactionMode = TransactionMode.CREATE,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val finished: Boolean = false,
    val name: String = "",
    val amount: String = "",
    val transactionType: TransactionType = TransactionType.OUTCOME,
    val dateTime: ZonedDateTime = ZonedDateTime.now(),
    val description: String? = null,
    val imageUri: String? = null,
    val selectedBudget: Budget? = null,
    val selectedCategory: Category? = null,
    val isRecurring: Boolean = false,
    val recurringStartDate: ZonedDateTime = ZonedDateTime.now(),
    val recurringIntervalUnit: RecurringIntervalUnit = RecurringIntervalUnit.MONTH,
    val recurringIntervalValue: Int = 1,
    val isBudgetSelectionHidden: Boolean = false
)

@HiltViewModel
class CreateEditTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateEditTransactionUiState())
    val uiState = _uiState.asStateFlow()

    private var transactionIdToEdit: String? = null
    private var budgetIdToResolve: String? = null

    // Lưu lại giao dịch gốc khi sửa để so sánh
    private var originalTransaction: Transaction? = null

    val budgetsPagingFlow: Flow<PagingData<Budget>> = budgetRepository.getBudgets().cachedIn(viewModelScope)
    val categoriesPagingFlow: Flow<PagingData<Category>> = categoryRepository.getCategories().cachedIn(viewModelScope)

    fun initialize(
        transaction: Transaction?,
        predefinedBudgetId: String?,
        predefinedDate: LocalDate?
    ) {
        if (originalTransaction != null) return // Tránh initialize lại

        if (transaction != null) {
            // --- CHẾ ĐỘ EDIT ---
            this.transactionIdToEdit = transaction.serverId
            this.budgetIdToResolve = transaction.budgetId
            this.originalTransaction = transaction // LƯU LẠI GIAO DỊCH GỐC

            _uiState.update {
                it.copy(
                    mode = TransactionMode.EDIT,
                    name = transaction.name,
                    amount = transaction.amount.toString().removeSuffix(".0"),
                    transactionType = transaction.type,
                    dateTime = transaction.dateTime,
                    description = transaction.description,
                    imageUri = transaction.image,
                    selectedCategory = transaction.category,
                    isRecurring = transaction.isRecurring,
                    recurringStartDate = transaction.recurringInfo?.startDate ?: ZonedDateTime.now(),
                    recurringIntervalUnit = transaction.recurringInfo?.intervalUnit ?: RecurringIntervalUnit.MONTH,
                    recurringIntervalValue = transaction.recurringInfo?.intervalValue ?: 1,
                )
            }
        } else {
            // --- CHẾ ĐỘ CREATE ---
            this.budgetIdToResolve = predefinedBudgetId
            this.originalTransaction = null
            val initialDate = predefinedDate?.atTime(LocalTime.now())?.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now()

            _uiState.update {
                it.copy(
                    mode = TransactionMode.CREATE,
                    dateTime = initialDate,
                    isBudgetSelectionHidden = (predefinedBudgetId != null)
                )
            }
        }
    }

    fun resolveDependencies(allBudgets: List<Budget>, allCategories: List<Category>) {
        _uiState.update { currentState ->
            val finalBudget = currentState.selectedBudget ?: allBudgets.find { it.serverId == budgetIdToResolve } ?: allBudgets.firstOrNull()
            val finalCategory = currentState.selectedCategory ?: if (currentState.mode == TransactionMode.CREATE) allCategories.firstOrNull() else null

            currentState.copy(
                selectedBudget = finalBudget,
                selectedCategory = finalCategory
            )
        }
    }

    private fun createUpdateMap(): Map<String, Any?> {
        val original = originalTransaction ?: return emptyMap()
        val current = _uiState.value
        val updateMap = mutableMapOf<String, Any?>()

        val currentAmount = current.amount.toDoubleOrNull()
        val currentName = current.name.trim()
        val currentDescription = current.description?.trim()

        if (original.name != currentName) {
            updateMap["name"] = currentName
        }
        if (currentAmount != null && original.amount != currentAmount) {
            updateMap["amount"] = currentAmount
        }
        if (original.budgetId != current.selectedBudget?.serverId) {
            updateMap["budget_id"] = current.selectedBudget?.serverId
        }
        if (original.type != current.transactionType) {
            updateMap["type"] = current.transactionType.name.lowercase()
        }
        if (original.category?.serverId != current.selectedCategory?.serverId) {
            updateMap["category_id"] = current.selectedCategory?.serverId
        }
        if (original.dateTime != current.dateTime) {
            val utcDateTime = current.dateTime.withZoneSameInstant(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_INSTANT)
            updateMap["date_time"] = utcDateTime
        }
        if (original.description != currentDescription) {
            updateMap["description"] = currentDescription
        }
        // Thêm logic cho is_recurring và các trường liên quan nếu cần
        if (original.isRecurring != current.isRecurring) {
            updateMap["is_recurring"] = current.isRecurring
        }

        return updateMap
    }

    fun submit() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            val currentState = _uiState.value

            val name = currentState.name.trim()
            val amount = currentState.amount.toDoubleOrNull()
            val budget = currentState.selectedBudget
            val category = currentState.selectedCategory

            if (name.isBlank()) {
                _uiState.update { it.copy(isSubmitting = false, error = "Tên giao dịch không được để trống") }
                return@launch
            }
            if (amount == null || amount <= 0) {
                _uiState.update { it.copy(isSubmitting = false, error = "Số tiền phải lớn hơn 0") }
                return@launch
            }
            if (budget == null) {
                _uiState.update { it.copy(isSubmitting = false, error = "Vui lòng chọn một ngân sách") }
                return@launch
            }
            if (category == null) {
                _uiState.update { it.copy(isSubmitting = false, error = "Vui lòng chọn một danh mục") }
                return@launch
            }

            val resultFlow: Flow<AppResult<Transaction>> = if (currentState.mode == TransactionMode.CREATE) {
                val transactionToCreate = Transaction(
                    serverId = "", name = name, amount = amount, budgetId = budget.serverId!!, type = currentState.transactionType,
                    dateTime = currentState.dateTime, description = currentState.description, image = currentState.imageUri, category = category,
                    isRecurring = currentState.isRecurring,
                    recurringInfo = if (currentState.isRecurring) RecurringTransactionInfo(
                        startDate = currentState.recurringStartDate, intervalUnit = currentState.recurringIntervalUnit, intervalValue = currentState.recurringIntervalValue
                    ) else null
                )
                transactionRepository.createTransaction(transactionToCreate)
            } else {
                val updateMap = createUpdateMap()
                if (updateMap.isEmpty()) {
                    _uiState.update { it.copy(isSubmitting = false, finished = true, error = "Không có thay đổi nào.") }
                    return@launch
                }
                Log.d("CreateEditVM", "Đang gửi Map để cập nhật: $updateMap")
                transactionRepository.updateTransaction(transactionIdToEdit!!, updateMap)
            }

            resultFlow.collectLatest { result ->
                when (result) {
                    is AppResult.Success -> _uiState.update { it.copy(isSubmitting = false, finished = true) }
                    is AppResult.Error -> _uiState.update { it.copy(isSubmitting = false, error = "Lỗi: ${result.errorType}") }
                    is AppResult.Loading -> { /* Đã xử lý isSubmitting */ }
                }
            }
        }
    }

    // Các hàm xử lý thay đổi input từ UI
    fun onNameChange(value: String) = _uiState.update { it.copy(name = value) }
    fun onAmountChange(value: String) = _uiState.update { it.copy(amount = value) }
    fun onBudgetSelected(budget: Budget) = _uiState.update { it.copy(selectedBudget = budget) }
    fun onTypeSelected(type: TransactionType) = _uiState.update { it.copy(transactionType = type) }
    fun onDateTimeChange(dateTime: ZonedDateTime) = _uiState.update { it.copy(dateTime = dateTime) }
    fun onCategorySelected(category: Category) = _uiState.update { it.copy(selectedCategory = category) }
    fun onRecurringToggled(isEnabled: Boolean) = _uiState.update { it.copy(isRecurring = isEnabled) }
    fun onRecurringStartDateChange(date: ZonedDateTime) = _uiState.update { it.copy(recurringStartDate = date) }
    fun onRecurringIntervalChange(unit: RecurringIntervalUnit, value: Int) = _uiState.update { it.copy(recurringIntervalUnit = unit, recurringIntervalValue = value) }
    fun onDescriptionChange(desc: String?) = _uiState.update { it.copy(description = desc) }
    fun onImageSelected(uri: String?) = _uiState.update { it.copy(imageUri = uri) }
    fun errorShown() = _uiState.update { it.copy(error = null) }
}
