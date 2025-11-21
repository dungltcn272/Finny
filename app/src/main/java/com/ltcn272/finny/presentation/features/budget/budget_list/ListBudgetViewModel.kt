package com.ltcn272.finny.presentation.features.budget.budget_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ltcn272.finny.domain.model.BudgetDetails
import com.ltcn272.finny.domain.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ListBudgetUiState(
    val budgets: List<BudgetDetails> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ListBudgetViewModel @Inject constructor(
    budgetRepository: BudgetRepository,
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _initialLoadComplete = MutableStateFlow(false)

    private val budgetsFlow = budgetRepository.getBudgetDetailsList()
        .onEach { _initialLoadComplete.value = true }

    val uiState: StateFlow<ListBudgetUiState> = combine(
        budgetsFlow,
        _isRefreshing,
        _error,
        _initialLoadComplete
    ) { budgets, isRefreshing, error, initialLoadComplete ->
        ListBudgetUiState(
            budgets = budgets,
            isLoading = !initialLoadComplete,
            isRefreshing = isRefreshing,
            error = error
        )
    }.catch { e ->
        _error.value = e.localizedMessage ?: "An unexpected error occurred"
        emit(ListBudgetUiState(isLoading = false))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ListBudgetUiState(isLoading = true)
    )

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(2000) // Simulate a refresh delay
            _isRefreshing.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}
