package com.ltcn272.finny.presentation.features.budget.create_edit

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.domain.model.RecurringConfig
import com.ltcn272.finny.domain.model.RecurringIntervalUnit
import com.ltcn272.finny.domain.repository.BudgetRepository
import com.ltcn272.finny.domain.util.AppResult
import com.ltcn272.finny.presentation.common.ui.TopSnackbarType
import com.ltcn272.finny.presentation.common.util.formatDoubleForInput
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import javax.inject.Inject

enum class BudgetMode { CREATE, EDIT }

data class SnackbarState(
    val visible: Boolean = false,
    val message: String = "",
    val type: TopSnackbarType = TopSnackbarType.INFO
)

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

    val snackbarState: SnackbarState = SnackbarState(),
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
    @ApplicationContext private val context: Context
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
                    recurringTopupAmount = budget.recurring?.topupAmount?.let { amount -> formatDoubleForInput(amount) } ?: "",
                    recurringIntervalUnit = budget.recurring?.unit ?: RecurringIntervalUnit.MONTH,
                    recurringIntervalValue = budget.recurring?.value ?: 1,
                    startDate = budget.startDate
                )
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value) }

    fun onAmountChange(value: String) {
        val cleanValue = value.filter { it.isDigit() }
        _uiState.update { it.copy(amount = cleanValue) }
    }

    fun onRecurringTopupAmountChange(value: String) {
        val cleanValue = value.filter { it.isDigit() }
        _uiState.update { it.copy(recurringTopupAmount = cleanValue) }
    }
    fun onRecurringToggled(isEnabled: Boolean) = _uiState.update { it.copy(isRecurring = isEnabled) }
    fun onIntervalSelected(unit: RecurringIntervalUnit) = _uiState.update { it.copy(recurringIntervalUnit = unit) }
    fun onStartDateSelected(date: ZonedDateTime) = _uiState.update { it.copy(startDate = date) }

    fun onSnackbarDismissed() {
        _uiState.update { it.copy(snackbarState = it.snackbarState.copy(visible = false)) }
    }

    fun submit() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, snackbarState = it.snackbarState.copy(visible = false)) }

            val currentState = _uiState.value
            val name = currentState.name.trim()
            val amount = currentState.amount.toDoubleOrNull()

            if (name.isBlank()) {
                _uiState.update { it.copy(isSubmitting = false, snackbarState = SnackbarState(visible = true, message = context.getString(R.string.error_budget_name_empty), type = TopSnackbarType.WARNING)) }
                return@launch
            }

            if (amount == null || amount <= 0) {
                _uiState.update { it.copy(isSubmitting = false, snackbarState = SnackbarState(visible = true, message = context.getString(R.string.error_amount_must_be_positive), type = TopSnackbarType.WARNING)) }
                return@launch
            }

            val recurringTopupAmount = currentState.recurringTopupAmount.toDoubleOrNull()
            if (currentState.isRecurring && (recurringTopupAmount == null || recurringTopupAmount <= 0)) {
                _uiState.update { it.copy(isSubmitting = false, snackbarState = SnackbarState(visible = true, message = context.getString(R.string.error_recurring_amount_positive), type = TopSnackbarType.WARNING)) }
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
                        // <<< SỬA Ở ĐÂY
                        _uiState.update { it.copy(isSubmitting = false, snackbarState = SnackbarState(visible = true, message = "Lỗi: ${result.errorType}", type = TopSnackbarType.ERROR)) }
                    }
                    is AppResult.Loading -> {}
                }
            }
        }
    }
}
