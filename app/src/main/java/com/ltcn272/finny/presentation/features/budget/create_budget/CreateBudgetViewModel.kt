// Trong file presentation/features/budget/create_budget/CreateBudgetViewModel.kt

package com.ltcn272.finny.presentation.features.budget.create_budget

import androidx.lifecycle.SavedStateHandle // Import
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ltcn272.finny.core.navigation.NavArgs // Import
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.domain.model.BudgetPeriod
import com.ltcn272.finny.domain.repository.BudgetRepository
import com.ltcn272.finny.domain.util.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID
import javax.inject.Inject

data class CreateBudgetUiState(
    val budgetId: String? = null, // Thêm ID để biết là create hay update
    val isEditMode: Boolean = false,
    val name: String = "",
    val amount: String = "",
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedPeriod: BudgetPeriod = BudgetPeriod.ONE_MONTH,
    val isDatePickerVisible: Boolean = false,
    val saveInProgress: Boolean = false, // Đổi tên cho rõ nghĩa
    val saveSuccess: Boolean = false, // Đổi tên cho rõ nghĩa
    val error: String? = null
)

@HiltViewModel
class CreateBudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    savedStateHandle: SavedStateHandle // Inject SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateBudgetUiState())
    val uiState: StateFlow<CreateBudgetUiState> = _uiState.asStateFlow()

    private val budgetId: String? = savedStateHandle[NavArgs.BUDGET_ID]

    init {
        if (budgetId != null) {
            _uiState.update { it.copy(budgetId = budgetId, isEditMode = true) }
            loadInitialBudget(budgetId)
        }
    }

    private fun loadInitialBudget(id: String) {
        viewModelScope.launch {
            val budget = budgetRepository.getBudgetDetails(id).firstOrNull()?.budget
            if (budget != null) {
                _uiState.update {
                    it.copy(
                        name = budget.name,
                        amount = budget.limit.toString(),
                        selectedDate = budget.startDate.toLocalDate(),
                        selectedPeriod = budget.period
                    )
                }
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onAmountChange(amount: String) {
        _uiState.update { it.copy(amount = amount) }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date, isDatePickerVisible = false) }
    }

    fun onPeriodSelected(period: BudgetPeriod) {
        _uiState.update { it.copy(selectedPeriod = period) }
    }

    fun showDatePicker() {
        _uiState.update { it.copy(isDatePickerVisible = true) }
    }

    fun dismissDatePicker() {
        _uiState.update { it.copy(isDatePickerVisible = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun saveBudget() {
        viewModelScope.launch {
            _uiState.update { it.copy(saveInProgress = true) }

            val currentState = _uiState.value
            val amountAsDouble = currentState.amount.toDoubleOrNull()
            if (amountAsDouble == null || currentState.name.isBlank()) {
                _uiState.update {
                    it.copy(
                        error = "Please fill in all fields correctly.",
                        saveInProgress = false
                    )
                }
                return@launch
            }

            val budgetToSave = Budget(
                id = currentState.budgetId ?: UUID.randomUUID().toString(),
                name = currentState.name,
                userId = "", // This will be set by the backend/auth manager
                limit = amountAsDouble,
                period = currentState.selectedPeriod,
                startDate = currentState.selectedDate.atStartOfDay(ZonedDateTime.now().zone),
                createdAt = ZonedDateTime.now(), // Cần logic để không thay đổi khi update
                updatedAt = ZonedDateTime.now()
            )

            val result = if (currentState.isEditMode) {
                budgetRepository.updateBudgetLocally(budgetToSave)
            } else {
                budgetRepository.addBudgetLocally(budgetToSave)
            }

            when (result) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(saveInProgress = false, saveSuccess = true) }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            error = result.message,
                            saveInProgress = false
                        )
                    }
                }
                AppResult.Loading -> { /* Handled by saveInProgress */ }
            }
        }
    }
}
