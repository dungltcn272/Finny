package com.ltcn272.finny.presentation.features.transaction.create_edit

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.*
import com.ltcn272.finny.domain.repository.BudgetRepository
import com.ltcn272.finny.domain.repository.CategoryRepository
import com.ltcn272.finny.domain.repository.TransactionRepository
import com.ltcn272.finny.domain.util.AppResult
import com.ltcn272.finny.domain.util.ErrorType
import com.ltcn272.finny.presentation.common.ui.ScreenMode
import com.ltcn272.finny.presentation.features.snackbar.SnackbarManager
import com.ltcn272.finny.presentation.features.snackbar.TopSnackbarType
import com.ltcn272.finny.util.createFileFromUri
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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

data class CreateEditTransactionUiState(
    val mode: ScreenMode = ScreenMode.CREATE,
    val isSubmitting: Boolean = false,
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
    val isBudgetSelectionHidden: Boolean = false,
    val isUploadingImage: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    val transactionNameToDelete: String = "",
    val isDeleting: Boolean = false
)

@HiltViewModel
class CreateEditTransactionViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateEditTransactionUiState())
    val uiState = _uiState.asStateFlow()

    private var transactionIdToEdit: String? = null
    private var budgetIdToResolve: String? = null
    private var originalTransaction: Transaction? = null

    val budgetsPagingFlow: Flow<PagingData<Budget>> =
        budgetRepository.getBudgets().cachedIn(viewModelScope)
    val categoriesPagingFlow: Flow<PagingData<Category>> =
        categoryRepository.getCategories().cachedIn(viewModelScope)

    fun initialize(
        transaction: Transaction?,
        predefinedBudgetId: String?,
        predefinedDate: LocalDate?
    ) {
        if (originalTransaction != null) return

        if (transaction != null) {
            this.transactionIdToEdit = transaction.serverId
            this.budgetIdToResolve = transaction.budgetId
            this.originalTransaction = transaction

            val initialDateTime = if (transaction.isRecurring) {
                transaction.recurringInfo?.startDate ?: ZonedDateTime.now()
            } else {
                transaction.dateTime
            }

            _uiState.update {
                it.copy(
                    mode = ScreenMode.EDIT,
                    name = transaction.name,
                    amount = transaction.amount.toString().removeSuffix(".0"),
                    transactionType = transaction.type,
                    dateTime = initialDateTime,
                    description = transaction.description,
                    imageUri = transaction.image,
                    selectedCategory = transaction.category,
                    isRecurring = transaction.isRecurring,
                    recurringStartDate = transaction.recurringInfo?.startDate
                        ?: ZonedDateTime.now(),
                    recurringIntervalUnit = transaction.recurringInfo?.intervalUnit
                        ?: RecurringIntervalUnit.MONTH,
                    recurringIntervalValue = transaction.recurringInfo?.intervalValue ?: 1,
                )
            }
        } else {
            this.budgetIdToResolve = predefinedBudgetId
            this.originalTransaction = null
            val initialDate =
                predefinedDate?.atTime(LocalTime.now())?.atZone(ZoneId.systemDefault())
                    ?: ZonedDateTime.now()

            _uiState.update {
                it.copy(
                    mode = ScreenMode.CREATE,
                    dateTime = initialDate,
                    isBudgetSelectionHidden = (predefinedBudgetId != null)
                )
            }
        }
    }

    fun resolveDependencies(allBudgets: List<Budget>, allCategories: List<Category>) {
        _uiState.update { currentState ->
            val finalBudget =
                currentState.selectedBudget ?: allBudgets.find { it.serverId == budgetIdToResolve }
                ?: allBudgets.firstOrNull()
            val finalCategory = currentState.selectedCategory
                ?: if (currentState.mode == ScreenMode.CREATE) allCategories.firstOrNull() else null

            currentState.copy(
                selectedBudget = finalBudget,
                selectedCategory = finalCategory
            )
        }
    }

    fun uploadImage(uri: Uri, snackbarManager: SnackbarManager) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingImage = true) }

            val file = createFileFromUri(context, uri, snackbarManager)
                ?: run {
                    _uiState.update { it.copy(isUploadingImage = false) }
                    return@launch
                }

            transactionRepository.uploadImage(file).collectLatest { result ->
                when (result) {
                    is AppResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isUploadingImage = false,
                                imageUri = result.data
                            )
                        }
                    }

                    is AppResult.Error -> {
                        _uiState.update { it.copy(isUploadingImage = false) }
                        snackbarManager.showMessage(
                            context.getString(R.string.error_upload_image),
                            TopSnackbarType.ERROR
                        )
                    }

                    is AppResult.Loading -> {
                    }
                }
            }
            file.delete()
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onAmountChange(amount: String) {
        val cleanValue = amount.filter { it.isDigit() }
        _uiState.update { it.copy(amount = cleanValue) }
    }

    fun onTypeSelected(type: TransactionType) {
        _uiState.update { it.copy(transactionType = type) }
    }

    fun onDateTimeChange(dateTime: ZonedDateTime) {
        _uiState.update { it.copy(dateTime = dateTime) }
    }

    fun onBudgetSelected(budget: Budget) {
        _uiState.update { it.copy(selectedBudget = budget) }
    }

    fun onCategorySelected(category: Category) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onDescriptionChange(description: String?) {
        _uiState.update { it.copy(description = description) }
    }

    fun onDeleteImageClick() {
        _uiState.update { it.copy(imageUri = null) }
    }

    fun onRecurringToggled(isRecurring: Boolean) {
        _uiState.update { it.copy(isRecurring = isRecurring) }
    }

    fun onRecurringStartDateChange(date: ZonedDateTime) {
        _uiState.update { it.copy(recurringStartDate = date) }
    }

    fun onRecurringIntervalUnitSelected(unit: RecurringIntervalUnit) {
        _uiState.update { it.copy(recurringIntervalUnit = unit) }
    }

    fun requestDeleteTransaction() {
        originalTransaction?.let {
            _uiState.update {
                it.copy(
                    showDeleteConfirmDialog = true,
                    transactionNameToDelete = it.name
                )
            }
        }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(showDeleteConfirmDialog = false) }
    }

    fun confirmDeleteTransaction(snackbarManager: SnackbarManager) {
        val id = transactionIdToEdit ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(showDeleteConfirmDialog = false, isDeleting = true) }
            transactionRepository.deleteTransaction(id).collectLatest { result ->
                when (result) {
                    is AppResult.Success -> {
                        snackbarManager.showMessage(
                            context.getString(R.string.transaction_deleted_successfully),
                            TopSnackbarType.SUCCESS
                        )
                        _uiState.update { it.copy(finished = true, isDeleting = false) }
                    }

                    is AppResult.Error -> {
                        snackbarManager.showMessage(
                            context.getString(R.string.error_deleting_transaction),
                            TopSnackbarType.ERROR
                        )
                        _uiState.update { it.copy(isDeleting = false) }
                    }

                    is AppResult.Loading -> {}
                }
            }
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
            val utcDateTime = current.dateTime.withZoneSameInstant(ZoneId.of("UTC"))
                .format(DateTimeFormatter.ISO_INSTANT)
            updateMap["date_time"] = utcDateTime
        }
        if (original.description?.trim() != currentDescription) {
            updateMap["description"] = currentDescription
        }
        if (original.isRecurring != current.isRecurring) {
            updateMap["is_recurring"] = current.isRecurring
        }
        if (original.image != current.imageUri) {
            updateMap["image"] = current.imageUri
        }

        return updateMap
    }

    fun submit(snackbarManager: SnackbarManager) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val currentState = _uiState.value

            val name = currentState.name.trim()
            val amount = currentState.amount.toDoubleOrNull()
            val budget = currentState.selectedBudget
            val category = currentState.selectedCategory

            if (name.isBlank()) {
                _uiState.update { it.copy(isSubmitting = false) }
                snackbarManager.showMessage(
                    context.getString(R.string.error_transaction_name_empty),
                    TopSnackbarType.WARNING
                )
                return@launch
            }
            if (amount == null || amount <= 0) {
                _uiState.update { it.copy(isSubmitting = false) }
                snackbarManager.showMessage(
                    context.getString(R.string.error_amount_must_be_positive),
                    TopSnackbarType.WARNING
                )
                return@launch
            }
            if (budget == null) {
                _uiState.update { it.copy(isSubmitting = false) }
                snackbarManager.showMessage(
                    context.getString(R.string.error_select_budget),
                    TopSnackbarType.WARNING
                )
                return@launch
            }
            if (category == null) {
                _uiState.update { it.copy(isSubmitting = false) }
                snackbarManager.showMessage(
                    context.getString(R.string.error_select_category),
                    TopSnackbarType.WARNING
                )
                return@launch
            }

            val resultFlow: Flow<AppResult<Transaction>> =
                if (currentState.mode == ScreenMode.CREATE) {
                    val transactionToCreate = Transaction(
                        serverId = "",
                        name = name,
                        amount = amount,
                        budgetId = budget.serverId!!,
                        type = currentState.transactionType,
                        dateTime = currentState.dateTime,
                        description = currentState.description,
                        image = currentState.imageUri,
                        category = category,
                        isRecurring = currentState.isRecurring,
                        recurringInfo = if (currentState.isRecurring) RecurringTransactionInfo(
                            startDate = currentState.recurringStartDate,
                            intervalUnit = currentState.recurringIntervalUnit,
                            intervalValue = currentState.recurringIntervalValue
                        ) else null
                    )
                    transactionRepository.createTransaction(transactionToCreate)
                } else {
                    val updateData = createUpdateMap()
                    transactionRepository.updateTransaction(transactionIdToEdit!!, updateData)
                }

            resultFlow.collectLatest { result ->
                when (result) {
                    is AppResult.Success -> {
                        val message =
                            if (currentState.mode == ScreenMode.CREATE) context.getString(R.string.transaction_created_successfully)
                            else context.getString(R.string.transaction_updated_successfully)
                        snackbarManager.showMessage(message, TopSnackbarType.SUCCESS)
                        _uiState.update { it.copy(isSubmitting = false, finished = true) }
                    }

                    is AppResult.Error -> {
                        snackbarManager.showMessage(
                            mapErrorToString(result.errorType),
                            TopSnackbarType.ERROR
                        )
                        _uiState.update { it.copy(isSubmitting = false) }
                    }

                    is AppResult.Loading -> {}
                }
            }
        }
    }

    private fun mapErrorToString(errorType: ErrorType): String {
        return when (errorType) {
            ErrorType.NETWORK -> context.getString(R.string.error_network)
            ErrorType.TIMEOUT -> context.getString(R.string.error_timeout)
            ErrorType.UNAUTHORIZED -> context.getString(R.string.error_unauthorized)
            ErrorType.SERVER_ERROR -> context.getString(R.string.error_server)
            ErrorType.UNKNOWN -> context.getString(R.string.error_unknown)
        }
    }
}
