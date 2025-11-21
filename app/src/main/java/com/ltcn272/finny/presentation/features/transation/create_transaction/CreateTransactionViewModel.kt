package com.ltcn272.finny.presentation.features.transation.create_transaction

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ltcn272.finny.core.navigation.NavArgs
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.domain.model.Location
import com.ltcn272.finny.domain.model.Transaction
import com.ltcn272.finny.domain.model.TransactionCategory
import com.ltcn272.finny.domain.model.TransactionType
import com.ltcn272.finny.domain.repository.BudgetRepository
import com.ltcn272.finny.domain.repository.TransactionRepository
import com.ltcn272.finny.domain.util.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import androidx.core.net.toUri

sealed class CreateTransactionUiState {
    data object Idle : CreateTransactionUiState()
    data object Loading : CreateTransactionUiState()
    data object Success : CreateTransactionUiState()
    data class Error(val message: String) : CreateTransactionUiState()
}

// *** THAY ĐỔI 1: imageUri thành Uri? để làm việc nhất quán ***
data class CreateTransactionFormState(
    val name: String = "",
    val description: String = "",
    val amount: String = "0",
    val type: TransactionType = TransactionType.OUTCOME,
    val category: TransactionCategory = TransactionCategory.FOOD,
    val dateTime: LocalDateTime = LocalDateTime.now(),
    val selectedBudgetId: String? = null,
    val selectedBudgetName: String = "Select Budget",
    val showBudgetDialog: Boolean = false,
    val showCategoryDialog: Boolean = false,
    val showDateTimePicker: Boolean = false,
    val imageUri: Uri? = null, // Đổi từ String? sang Uri?
    val location: Location? = null,
    val showLocationPicker: Boolean = false
)

@HiltViewModel
class CreateTransactionViewModel @Inject constructor(
    // *** THAY ĐỔI 2: Inject ApplicationContext để sử dụng trong hàm copy ***
    @ApplicationContext private val context: Context,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val transactionId: String? = savedStateHandle[NavArgs.TRANSACTION_ID]
    private val initialBudgetId: String? = savedStateHandle[NavArgs.BUDGET_ID]
    private val initialDate: String? = savedStateHandle[NavArgs.DATE]

    private val _createUiState = MutableStateFlow<CreateTransactionUiState>(CreateTransactionUiState.Idle)
    val createUiState: StateFlow<CreateTransactionUiState> = _createUiState.asStateFlow()

    private val _formState = MutableStateFlow(CreateTransactionFormState())
    val formState: StateFlow<CreateTransactionFormState> = _formState.asStateFlow()

    val budgets: StateFlow<List<Budget>> = budgetRepository.getLocalBudgets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        if (transactionId != null) {
            loadTransactionDetails(transactionId)
        } else {
            prefillFromArgs()
        }
    }

    private fun prefillFromArgs() {
        if (initialBudgetId != null || initialDate != null) {
            viewModelScope.launch {
                val allBudgets = budgets.firstOrNull { it.isNotEmpty() } ?: budgets.value
                val budget = initialBudgetId?.let { id -> allBudgets.find { b -> b.id == id } }
                val parsedDate = initialDate?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME) }

                _formState.update {
                    it.copy(
                        selectedBudgetId = budget?.id,
                        selectedBudgetName = budget?.name ?: it.selectedBudgetName,
                        dateTime = parsedDate ?: it.dateTime
                    )
                }
            }
        }
    }

    private fun loadTransactionDetails(id: String) {
        viewModelScope.launch {
            when (val result = transactionRepository.getTransactionById(id)) {
                is AppResult.Success -> {
                    result.data?.let { transactionData ->
                        val allBudgets = budgets.first { it.isNotEmpty() }
                        val budgetName = allBudgets.find { b -> b.id == transactionData.budgetId }?.name ?: "Select Budget"

                        _formState.update {
                            it.copy(
                                name = transactionData.name,
                                description = transactionData.description ?: "",
                                amount = transactionData.amount.toLong().toString(),
                                type = transactionData.type,
                                category = transactionData.category,
                                dateTime = transactionData.dateTime.toLocalDateTime(),
                                selectedBudgetId = transactionData.budgetId,
                                selectedBudgetName = budgetName,
                                // *** THAY ĐỔI 3: Chuyển đổi String từ DB thành Uri để hiển thị ***
                                imageUri = transactionData.localImagePath?.toUri(),
                                location = transactionData.location
                            )
                        }
                    }
                }
                is AppResult.Error -> {
                    _createUiState.value = CreateTransactionUiState.Error(result.message )
                }
                else -> Unit
            }
        }
    }

    // *** THAY ĐỔI 4: Logic sao chép ảnh đã được tích hợp đúng ***
    fun onImageSelected(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val persistentUri = copyUriToInternalStorage(context, uri)
            if (persistentUri != null) {
                // Cập nhật state với URI an toàn, trỏ tới bộ nhớ trong của app
                _formState.update { it.copy(imageUri = persistentUri) }
            } else {
                launch(Dispatchers.Main) {
                    _createUiState.value = CreateTransactionUiState.Error("Could not save image")
                }
            }
        }
    }

    fun saveTransaction() {
        viewModelScope.launch {
            _createUiState.value = CreateTransactionUiState.Loading

            val currentState = _formState.value
            val amount = currentState.amount.toDoubleOrNull()

            if (amount == null || amount <= 0) {
                _createUiState.value = CreateTransactionUiState.Error("Invalid amount.")
                return@launch
            }
            if (currentState.name.isBlank()) {
                _createUiState.value = CreateTransactionUiState.Error("Transaction name cannot be empty.")
                return@launch
            }
            if (currentState.selectedBudgetId == null) {
                _createUiState.value = CreateTransactionUiState.Error("Please select a budget.")
                return@launch
            }

            // *** THAY ĐỔI 5: Chuyển Uri thành String để lưu vào DB ***
            val transaction = Transaction(
                id = transactionId ?: UUID.randomUUID().toString(),
                name = currentState.name.trim(),
                budgetId = currentState.selectedBudgetId,
                type = currentState.type,
                description = currentState.description.trim(),
                userId = null, // Sẽ được xử lý bởi tầng repository/API
                category = currentState.category,
                amount = amount,
                dateTime = currentState.dateTime.atZone(ZoneId.systemDefault()),
                image = null, // image (String?) có thể là URL online, chưa dùng đến
                localImagePath = currentState.imageUri?.toString(), // Lưu đường dẫn file local
                location = currentState.location,
                createdAt = null,
                updatedAt = null
            )

            val result = if (transactionId == null) {
                transactionRepository.addTransactionLocally(transaction)
            } else {
                transactionRepository.updateTransactionLocally(transaction)
            }

            when (result) {
                is AppResult.Success -> _createUiState.value = CreateTransactionUiState.Success
                is AppResult.Error -> _createUiState.value =
                    CreateTransactionUiState.Error(result.message)
                else -> Unit
            }
        }
    }

    // --- Các hàm xử lý UI State khác (không thay đổi) ---

    fun onNameChange(name: String) = _formState.update { it.copy(name = name) }
    fun onDescriptionChange(description: String) = _formState.update { it.copy(description = description) }
    fun onAmountKeyClick(key: String) {
        _formState.update {
            val currentAmount = it.amount
            it.copy(amount = if (currentAmount == "0") key else currentAmount + key)
        }
    }
    fun onAmountBackspace() {
        _formState.update {
            val currentAmount = it.amount
            it.copy(amount = if (currentAmount.length > 1) currentAmount.dropLast(1) else "0")
        }
    }
    fun onTransactionTypeChange(type: TransactionType) = _formState.update { it.copy(type = type) }
    fun onCategoryChange(category: TransactionCategory) = _formState.update { it.copy(category = category, showCategoryDialog = false) }
    fun onDateTimeChange(dateTime: LocalDateTime) = _formState.update { it.copy(dateTime = dateTime, showDateTimePicker = false) }
    fun onBudgetSelect(budgetName: String) {
        val selectedBudget = budgets.value.firstOrNull { it.name == budgetName }
        _formState.update {
            it.copy(
                selectedBudgetId = selectedBudget?.id,
                selectedBudgetName = selectedBudget?.name ?: "Select Budget",
                showBudgetDialog = false
            )
        }
    }
    fun onLocationSelected(location: Location) = _formState.update { it.copy(location = location, showLocationPicker = false) }
    fun clearImage() = _formState.update { it.copy(imageUri = null) }
    fun openLocationPicker() = _formState.update { it.copy(showLocationPicker = true) }
    fun dismissLocationPicker() = _formState.update { it.copy(showLocationPicker = false) }
    fun openBudgetDialog() = _formState.update { it.copy(showBudgetDialog = true) }
    fun dismissBudgetDialog() = _formState.update { it.copy(showBudgetDialog = false) }
    fun openCategoryDialog() = _formState.update { it.copy(showCategoryDialog = true) }
    fun dismissCategoryDialog() = _formState.update { it.copy(showCategoryDialog = false) }
    fun openDateTimePicker() = _formState.update { it.copy(showDateTimePicker = true) }
    fun dismissDateTimePicker() = _formState.update { it.copy(showDateTimePicker = false) }
    fun resetCreateState() = _createUiState.update { CreateTransactionUiState.Idle }
}

// *** THAY ĐỔI 6: Hàm helper được giữ lại, không thay đổi ***
private fun copyUriToInternalStorage(context: Context, uri: Uri): Uri? {
    return try {
        val imageDir = File(context.filesDir, "transaction_images").apply { mkdirs() }
        val fileName = "${UUID.randomUUID()}.jpg"
        val destinationFile = File(imageDir, fileName)

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(destinationFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        Uri.fromFile(destinationFile)
    } catch (e: Exception) {
        Log.e("CopyUriError", "Failed to copy URI to internal storage", e)
        null
    }
}
