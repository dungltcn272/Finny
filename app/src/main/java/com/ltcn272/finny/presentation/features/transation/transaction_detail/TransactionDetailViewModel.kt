package com.ltcn272.finny.presentation.features.transation.transaction_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ltcn272.finny.core.navigation.NavArgs
import com.ltcn272.finny.domain.model.Transaction
import com.ltcn272.finny.domain.repository.TransactionRepository
import com.ltcn272.finny.domain.util.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionDetailUiState(
    val transaction: Transaction? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val showDeleteConfirmation: Boolean = false,
    val isDeleting: Boolean = false
)

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val transactionId: String = savedStateHandle.get<String>(NavArgs.TRANSACTION_ID) ?: ""

    private val _showDeleteConfirmation = MutableStateFlow(false)
    private val _isDeleting = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    private val _navigateBack = MutableSharedFlow<Unit>()
    val navigateBack = _navigateBack.asSharedFlow()

    private val transactionFlow = if (transactionId.isNotEmpty()) {
        transactionRepository.getLocalTransactionByIdFlow(transactionId).distinctUntilChanged()
    } else {
        flowOf(null)
    }

    // expose transaction as a StateFlow so UI or other consumers can collect it directly if needed
    private val transactionState: StateFlow<Transaction?> = transactionFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // combined UI state
    val uiState: StateFlow<TransactionDetailUiState> = combine(
        transactionState,
        _showDeleteConfirmation,
        _isDeleting,
        _error
    ) { txn, showDelete, deleting, err ->
        TransactionDetailUiState(
            transaction = txn,
            isLoading = (txn == null && !deleting && err == null),
            error = err,
            showDeleteConfirmation = showDelete,
            isDeleting = deleting
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TransactionDetailUiState())

    init {
        // navigate back when transaction removed elsewhere
        transactionFlow.onEach { txn ->
            if (txn == null) _navigateBack.emit(Unit)
        }.launchIn(viewModelScope)
    }

    fun onDeleteConfirmation() {
        _showDeleteConfirmation.value = true
    }

    fun onDismissDeleteDialog() {
        _showDeleteConfirmation.value = false
    }

    fun onDeleteTransaction() {
        viewModelScope.launch {
            _isDeleting.value = true
            _showDeleteConfirmation.value = false
            val result = transactionRepository.deleteTransactionLocally(transactionId)
            if (result is AppResult.Success) {
                _navigateBack.emit(Unit)
            } else if (result is AppResult.Error) {
                _error.value = result.message
            }
            _isDeleting.value = false
        }
    }
}