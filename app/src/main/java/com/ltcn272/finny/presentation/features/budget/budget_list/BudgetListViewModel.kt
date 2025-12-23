package com.ltcn272.finny.presentation.features.budget.budget_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.domain.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ListBudgetUiState(
    val isRefreshingByUser: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class BudgetListViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListBudgetUiState())
    val uiState: StateFlow<ListBudgetUiState> = _uiState.asStateFlow()

    val budgetsPagingFlow: Flow<PagingData<Budget>> = budgetRepository.getBudgets()
        .cachedIn(viewModelScope)

    fun onUserPullToRefresh() {
        _uiState.update { it.copy(isRefreshingByUser = true) }
    }

    fun onRefreshFinished() {
        if (_uiState.value.isRefreshingByUser) {
            _uiState.update { it.copy(isRefreshingByUser = false) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
