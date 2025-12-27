package com.ltcn272.finny.presentation.features.category

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.Category
import com.ltcn272.finny.domain.repository.CategoryRepository
import com.ltcn272.finny.domain.util.AppResult
import com.ltcn272.finny.domain.util.ErrorType
// --- IMPORT CÁC THÀNH PHẦN MỚI ---
import com.ltcn272.finny.presentation.features.snackbar.SnackbarManager
import com.ltcn272.finny.presentation.features.snackbar.TopSnackbarType
// ---------------------------------
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryUiState(
    val isEditMode: Boolean = false,
    val categoryToDelete: Category? = null,
    val isCreating: Boolean = false,
    val newCategoryName: String = ""
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _isEditMode = MutableStateFlow(false)
    private val _categoryToDelete = MutableStateFlow<Category?>(null)
    private val _isCreating = MutableStateFlow(false)
    private val _newCategoryName = MutableStateFlow("")

    val uiState: StateFlow<CategoryUiState> = combine(
        _isEditMode,
        _categoryToDelete,
        _isCreating,
        _newCategoryName
    ) { isEditMode, categoryToDelete, isCreating, newCategoryName ->
        CategoryUiState(
            isEditMode = isEditMode,
            categoryToDelete = categoryToDelete,
            isCreating = isCreating,
            newCategoryName = newCategoryName
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CategoryUiState()
    )

    val categories: Flow<PagingData<Category>> = categoryRepository.getCategories()
        .cachedIn(viewModelScope)

    private val _refreshTrigger = MutableSharedFlow<Unit>(replay = 1)
    val refreshTrigger = _refreshTrigger.asSharedFlow()

    init {
        triggerRefresh()
    }

    private fun triggerRefresh() {
        viewModelScope.launch {
            _refreshTrigger.emit(Unit)
        }
    }

    fun toggleEditMode() {
        _isEditMode.update { !it }
    }

    fun onNewCategoryNameChange(name: String) {
        _newCategoryName.value = name
    }

    fun requestDeleteCategory(category: Category, snackbarManager: SnackbarManager) {
        if (category.isDefault) {
            snackbarManager.showMessage(
                message = context.getString(R.string.cannot_delete_default_category),
                type = TopSnackbarType.WARNING
            )
            return
        }
        _categoryToDelete.value = category
    }

    fun confirmDeleteCategory(snackbarManager: SnackbarManager) {
        val categoryId = _categoryToDelete.value?.serverId ?: return
        _categoryToDelete.value = null

        viewModelScope.launch {
            categoryRepository.deleteCategory(categoryId).onEach { result ->
                when (result) {
                    is AppResult.Success -> {
                        triggerRefresh()
                        snackbarManager.showMessage(
                            message = context.getString(R.string.category_deleted_successfully),
                            type = TopSnackbarType.SUCCESS
                        )
                    }
                    is AppResult.Error -> {
                        snackbarManager.showMessage(
                            message = mapErrorToString(result.errorType),
                            type = TopSnackbarType.ERROR
                        )
                    }
                    is AppResult.Loading -> {}
                }
            }.launchIn(viewModelScope)
        }
    }

    fun cancelDelete() {
        _categoryToDelete.value = null
    }

    fun createCategory(snackbarManager: SnackbarManager) {
        val name = _newCategoryName.value.trim()
        if (name.isBlank()) {
            snackbarManager.showMessage(
                message = context.getString(R.string.category_name_cannot_be_empty),
                type = TopSnackbarType.WARNING
            )
            return
        }

        viewModelScope.launch {
            categoryRepository.createCategory(name).onEach { result ->
                when (result) {
                    is AppResult.Loading -> _isCreating.value = true
                    is AppResult.Success -> {
                        triggerRefresh()
                        _isCreating.value = false
                        _newCategoryName.value = ""
                        snackbarManager.showMessage(
                            message = context.getString(R.string.category_created_successfully),
                            type = TopSnackbarType.SUCCESS
                        )
                    }
                    is AppResult.Error -> {
                        _isCreating.value = false
                        snackbarManager.showMessage(
                            message = mapErrorToString(result.errorType),
                            type = TopSnackbarType.ERROR
                        )
                    }
                }
            }.launchIn(viewModelScope)
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
