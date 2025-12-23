package com.ltcn272.finny.presentation.features.budget.create_edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.domain.model.RecurringConfig
import com.ltcn272.finny.domain.model.RecurringIntervalUnit
import com.ltcn272.finny.domain.repository.BudgetRepository
import com.ltcn272.finny.domain.util.AppResult
import com.ltcn272.finny.presentation.common.util.formatDoubleForInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import javax.inject.Inject

enum class BudgetMode { CREATE, EDIT }

data class CreateEditBudgetUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val mode: BudgetMode = BudgetMode.CREATE,

    val name: String = "",
    val amount: String = "",

    val isRecurring: Boolean = false,
    val recurringTopupAmount: String = "",
    val recurringIntervalUnit: RecurringIntervalUnit = RecurringIntervalUnit.MONTH,
    val recurringIntervalValue: Int = 1,
    val startDate: ZonedDateTime = ZonedDateTime.now(),

    val error: String? = null,
    val finished: Boolean = false
) {
    val nextRunAt: ZonedDateTime?
        get() {
            if (!isRecurring) return null
            return when (recurringIntervalUnit) {
                RecurringIntervalUnit.DAY -> startDate.plusDays(recurringIntervalValue.toLong())
                RecurringIntervalUnit.WEEK -> startDate.plusWeeks(recurringIntervalValue.toLong())
                RecurringIntervalUnit.MONTH -> startDate.plusMonths(recurringIntervalValue.toLong())
                RecurringIntervalUnit.YEAR -> startDate.plusYears(recurringIntervalValue.toLong())
            }
        }
}

@HiltViewModel
class CreateEditBudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateEditBudgetUiState())
    val uiState = _uiState.asStateFlow()

    private var budgetId: String? = null

    fun initialize(budget: Budget?) {
        if (budget == null) {
            _uiState.update { it.copy(isLoading = false, mode = BudgetMode.CREATE) }
        } else {
            this.budgetId = budget.serverId
            _uiState.update {
                it.copy(
                    isLoading = false,
                    mode = BudgetMode.EDIT,
                    name = budget.name,
                    amount = formatDoubleForInput(budget.amount),
                    isRecurring = budget.recurring != null,
                    recurringTopupAmount = budget.recurring?.topupAmount?.toString() ?: "",
                    recurringIntervalUnit = budget.recurring?.unit ?: RecurringIntervalUnit.MONTH,
                    recurringIntervalValue = budget.recurring?.value ?: 1,
                    startDate = budget.startDate
                )
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value) }
    fun onAmountChange(value: String) = _uiState.update { it.copy(amount = value) }
    fun onRecurringTopupAmountChange(value: String) = _uiState.update { it.copy(recurringTopupAmount = value) }
    fun onRecurringToggled(isEnabled: Boolean) = _uiState.update { it.copy(isRecurring = isEnabled) }
    fun onIntervalSelected(unit: RecurringIntervalUnit) = _uiState.update { it.copy(recurringIntervalUnit = unit) }
    fun onStartDateSelected(date: ZonedDateTime) = _uiState.update { it.copy(startDate = date) }

    fun errorShown() {
        _uiState.update { it.copy(error = null) }
    }

    fun submit() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }

            val currentState = _uiState.value
            val name = currentState.name.trim()
            val amount = currentState.amount.toDoubleOrNull()

            if (name.isBlank()) {
                _uiState.update { it.copy(isSubmitting = false, error = "Tên ngân sách không được để trống") }
                return@launch
            }

            if (amount == null || amount <= 0) {
                _uiState.update { it.copy(isSubmitting = false, error = "Số tiền phải là một số lớn hơn 0") }
                return@launch
            }

            val recurringTopupAmount = currentState.recurringTopupAmount.toDoubleOrNull()
            if (currentState.isRecurring && (recurringTopupAmount == null || recurringTopupAmount <= 0)) {
                _uiState.update { it.copy(isSubmitting = false, error = "Số tiền nạp định kỳ phải lớn hơn 0") }
                return@launch
            }

            val recurringConfig = if (currentState.isRecurring) {
                RecurringConfig(
                    unit = currentState.recurringIntervalUnit,
                    value = currentState.recurringIntervalValue,
                    topupAmount = recurringTopupAmount!!,
                    nextRunAt = null,
                    lastRunAt = null
                )
            } else null

            val budgetToSave = Budget(
                serverId = budgetId,
                name = name,
                amount = amount,
                limit = amount,
                currency = "VND",
                startDate = currentState.startDate,
                recurring = recurringConfig,
                isSingle = recurringConfig == null
            )

            val resultFlow = if (currentState.mode == BudgetMode.CREATE) {
                budgetRepository.createBudget(budgetToSave)
            } else {
                budgetRepository.updateBudget(budgetToSave)
            }

            resultFlow.collectLatest { result ->
                when (result) {
                    is AppResult.Success -> {
                        _uiState.update { it.copy(isSubmitting = false, finished = true) }
                    }
                    is AppResult.Error -> {
                        _uiState.update { it.copy(isSubmitting = false, error = "Lỗi: ${result.errorType}") }
                    }
                    is AppResult.Loading -> {
                    }
                }
            }
        }
    }
}
