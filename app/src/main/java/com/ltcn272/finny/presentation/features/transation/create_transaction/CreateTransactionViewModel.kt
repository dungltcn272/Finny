//package com.ltcn272.finny.presentation.features.transation.create_transaction
//
//import android.content.Context
//import android.net.Uri
//import android.util.Log
//import androidx.core.net.toUri
//import androidx.lifecycle.SavedStateHandle
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.ltcn272.finny.R
//import com.ltcn272.finny.core.navigation.NavArgs
//import com.ltcn272.finny.domain.model.Budget
//import com.ltcn272.finny.domain.model.Location
//import com.ltcn272.finny.domain.model.Transaction
//import com.ltcn272.finny.domain.model.TransactionCategory
//import com.ltcn272.finny.domain.model.TransactionType
//import com.ltcn272.finny.domain.repository.BudgetRepository
//import com.ltcn272.finny.domain.repository.TransactionRepository
//import com.ltcn272.finny.domain.util.AppResult
//import dagger.hilt.android.lifecycle.HiltViewModel
//import dagger.hilt.android.qualifiers.ApplicationContext
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.SharingStarted
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.stateIn
//import kotlinx.coroutines.flow.update
//import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.launch
//import java.io.File
//import java.io.FileOutputStream
//import java.time.LocalDateTime
//import java.time.ZoneId
//import java.time.format.DateTimeFormatter
//import java.util.UUID
//import javax.inject.Inject
//
//sealed class CreateTransactionUiState {
//    object Idle : CreateTransactionUiState()
//    object Loading : CreateTransactionUiState()
//    object Success : CreateTransactionUiState()
//    data class Error(val message: String) : CreateTransactionUiState()
//}
//
//data class CreateTransactionFormState(
//    val name: String = "",
//    val description: String = "",
//    val amount: String = "0",
//    val type: TransactionType = TransactionType.OUTCOME,
//    val category: TransactionCategory = TransactionCategory.FOOD,
//    val dateTime: LocalDateTime = LocalDateTime.now(),
//    val selectedBudget: Budget? = null,
//    val imageUri: Uri? = null,
//    val location: Location? = null,
//    val showBudgetDialog: Boolean = false,
//    val showCategoryDialog: Boolean = false,
//    val showDateTimePicker: Boolean = false,
//    val showLocationPicker: Boolean = false,
//    val isEditing: Boolean = false
//)
//
//enum class DialogType {
//    BUDGET, CATEGORY, DATETIME, LOCATION
//}
//
//@HiltViewModel
//class CreateTransactionViewModel @Inject constructor(
//    @ApplicationContext private val context: Context,
//    private val transactionRepository: TransactionRepository,
//    private val budgetRepository: BudgetRepository,
//    savedStateHandle: SavedStateHandle,
//) : ViewModel() {
//
//    private val transactionId: String? = savedStateHandle[NavArgs.TRANSACTION_ID]
//
//    private val _createUiState =
//        MutableStateFlow<CreateTransactionUiState>(CreateTransactionUiState.Idle)
//    val createUiState: StateFlow<CreateTransactionUiState> = _createUiState.asStateFlow()
//
//    private val _formState = MutableStateFlow(CreateTransactionFormState())
//    val formState: StateFlow<CreateTransactionFormState> = _formState.asStateFlow()
//
//    val budgets: StateFlow<List<Budget>> = budgetRepository.getLocalBudgets()
//        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
//
//    init {
//        val isEditing = transactionId != null
//        _formState.update { it.copy(isEditing = isEditing) }
//
//        if (isEditing) {
//            loadTransactionDetails(transactionId!!)
//        } else {
//            val initialBudgetId: String? = savedStateHandle[NavArgs.BUDGET_ID]
//            val initialDate: String? = savedStateHandle[NavArgs.DATE]
//            prefillFromArgs(initialBudgetId, initialDate)
//        }
//
//        viewModelScope.launch {
//            val list = budgets.first { it.isNotEmpty() }
//            if (_formState.value.selectedBudget == null && !isEditing) {
//                _formState.update { it.copy(selectedBudget = list.first()) }
//            }
//        }
//    }
//
//    private fun prefillFromArgs(budgetId: String?, dateStr: String?) {
//        if (budgetId == null && dateStr == null) return
//
//        val parsedDate = try {
//            dateStr?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME) }
//        } catch (e: Exception) {
//            Log.e("PrefillArgs", "Invalid date format: $dateStr", e)
//            null
//        }
//
//        viewModelScope.launch {
//            val budget = budgetId?.let { id -> budgetRepository.getBudgetById(id) }
//            _formState.update {
//                it.copy(
//                    selectedBudget = budget ?: it.selectedBudget,
//                    dateTime = parsedDate ?: it.dateTime
//                )
//            }
//        }
//    }
//
//    private fun loadTransactionDetails(id: String) {
//        viewModelScope.launch {
//            val transaction = transactionRepository.getLocalTransactionByIdFlow(id).first()
//            transaction?.let { t ->
//                val budget = budgetRepository.getBudgetById(t.budgetId)
//                _formState.update {
//                    it.copy(
//                        name = t.name,
//                        description = t.description ?: "",
//                        amount = t.amount.toString(),
//                        type = t.type,
//                        category = t.category,
//                        dateTime = t.dateTime.toLocalDateTime(),
//                        selectedBudget = budget,
//                        imageUri = t.localImagePath?.toUri(),
//                        location = t.location
//                    )
//                }
//            }
//        }
//    }
//
//    fun onSaveTransaction() {
//        if (_createUiState.value is CreateTransactionUiState.Loading) return
//
//        viewModelScope.launch {
//            _createUiState.value = CreateTransactionUiState.Loading
//            val currentState = _formState.value
//            val amount = currentState.amount.toDoubleOrNull()
//
//            if (amount == null || amount <= 0) {
//                _createUiState.value =
//                    CreateTransactionUiState.Error(context.getString(R.string.invalid_amount))
//                return@launch
//            }
//            if (currentState.name.isBlank()) {
//                _createUiState.value =
//                    CreateTransactionUiState.Error(context.getString(R.string.transaction_name_empty))
//                return@launch
//            }
//            if (currentState.selectedBudget == null) {
//                _createUiState.value =
//                    CreateTransactionUiState.Error(context.getString(R.string.select_budget_prompt))
//                return@launch
//            }
//
//            val transactionToSave = Transaction(
//                id = transactionId ?: UUID.randomUUID().toString(),
//                name = currentState.name.trim(),
//                budgetId = currentState.selectedBudget.id,
//                type = currentState.type,
//                description = currentState.description.trim(),
//                userId = null,
//                category = currentState.category,
//                amount = amount,
//                dateTime = currentState.dateTime.atZone(ZoneId.systemDefault()),
//                image = null,
//                localImagePath = currentState.imageUri?.toString(),
//                location = currentState.location,
//                createdAt = null,
//                updatedAt = null
//            )
//
//            val result = if (currentState.isEditing) {
//                transactionRepository.updateTransactionLocally(transactionToSave)
//            } else {
//                transactionRepository.addTransactionLocally(transactionToSave)
//            }
//
//            when (result) {
//                is AppResult.Success -> _createUiState.value = CreateTransactionUiState.Success
//                is AppResult.Error -> _createUiState.value =
//                    CreateTransactionUiState.Error(result.message)
//
//                AppResult.Loading -> Log.w(
//                    "CreateTransaction",
//                    context.getString(R.string.unexpected_loading_state)
//                )
//            }
//        }
//    }
//
//    fun onNameChange(name: String) = _formState.update { it.copy(name = name) }
//    fun onDescriptionChange(description: String) =
//        _formState.update { it.copy(description = description) }
//
//    fun onTransactionTypeChange(type: TransactionType) = _formState.update { it.copy(type = type) }
//    fun onCategoryChange(category: TransactionCategory) =
//        _formState.update { it.copy(category = category, showCategoryDialog = false) }
//
//    fun onDateTimeChange(dateTime: LocalDateTime) =
//        _formState.update { it.copy(dateTime = dateTime, showDateTimePicker = false) }
//
//    fun onBudgetSelect(budget: Budget) =
//        _formState.update { it.copy(selectedBudget = budget, showBudgetDialog = false) }
//
//    fun onLocationSelected(location: Location) =
//        _formState.update { it.copy(location = location, showLocationPicker = false) }
//
//    fun onImageSelected(uri: Uri?) {
//        if (uri == null) {
//            _formState.update { it.copy(imageUri = null) }
//            return
//        }
//        viewModelScope.launch(Dispatchers.IO) {
//            val persistentUri = copyUriToInternalStorage(context, uri)
//            if (persistentUri == null) {
//                Log.e("CreateTransaction", "Failed to save image locally.")
//                _createUiState.value =
//                    CreateTransactionUiState.Error(context.getString(R.string.failed_to_save_image))
//                _formState.update { it.copy(imageUri = null) }
//            } else {
//                _formState.update { it.copy(imageUri = persistentUri) }
//            }
//        }
//    }
//
//    fun onAmountKeyPress(key: String) {
//        _formState.update {
//            val currentAmount = it.amount
//            if (key == "." && currentAmount.contains(".")) return@update it
//            val newAmount = if (currentAmount == "0" && key != ".") key else currentAmount + key
//            if (newAmount.length > 15) return@update it
//            it.copy(amount = newAmount)
//        }
//    }
//
//    fun onAmountClear() = _formState.update { it.copy(amount = "0") }
//
//    fun onAmountBackspace() {
//        _formState.update {
//            val currentAmount = it.amount
//            it.copy(amount = if (currentAmount.length > 1) currentAmount.dropLast(1) else "0")
//        }
//    }
//
//    fun setDialogVisibility(dialog: DialogType, visible: Boolean) {
//        _formState.update {
//            when (dialog) {
//                DialogType.BUDGET -> it.copy(showBudgetDialog = visible)
//                DialogType.CATEGORY -> it.copy(showCategoryDialog = visible)
//                DialogType.DATETIME -> it.copy(showDateTimePicker = visible)
//                DialogType.LOCATION -> it.copy(showLocationPicker = visible)
//            }
//        }
//    }
//
//    fun resetCreateState() = _createUiState.update { CreateTransactionUiState.Idle }
//}
//
//private fun copyUriToInternalStorage(context: Context, uri: Uri): Uri? {
//    return try {
//        val imageDir = File(context.filesDir, "transaction_images").apply { mkdirs() }
//        val fileName = "${UUID.randomUUID()}.jpg"
//        val destinationFile = File(imageDir, fileName)
//        context.contentResolver.openInputStream(uri)?.use { inputStream ->
//            FileOutputStream(destinationFile).use { outputStream -> inputStream.copyTo(outputStream) }
//        }
//        Uri.fromFile(destinationFile)
//    } catch (e: Exception) {
//        Log.e("CopyUriError", "Failed to copy URI to internal storage", e)
//        null
//    }
//}