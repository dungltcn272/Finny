package com.ltcn272.finny.presentation.features.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.ltcn272.finny.R
import com.ltcn272.finny.presentation.common.ui.CircleNavigationButton
import com.ltcn272.finny.presentation.common.ui.DeleteConfirmationDialog
import com.ltcn272.finny.presentation.features.snackbar.LocalSnackbarManager
import com.ltcn272.finny.presentation.features.category.component.AddCategoryButton
import com.ltcn272.finny.presentation.features.category.component.CategoryListSection
import com.ltcn272.finny.presentation.features.category.component.CreateCategorySheet
import com.ltcn272.finny.presentation.theme.MainBackgroundBrush
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    viewModel: CategoryViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories = viewModel.categories.collectAsLazyPagingItems()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    val snackbarManager = LocalSnackbarManager.current

    LaunchedEffect(viewModel.refreshTrigger) {
        viewModel.refreshTrigger.collectLatest {
            categories.refresh()
        }
    }

    LaunchedEffect(uiState.isCreating) {
        if (!uiState.isCreating && uiState.newCategoryName.isEmpty() && showBottomSheet) {
            scope.launch { sheetState.hide() }.invokeOnCompletion {
                if (!sheetState.isVisible) {
                    showBottomSheet = false
                }
            }
        }
    }

    if (uiState.categoryToDelete != null) {
        DeleteConfirmationDialog(
            title = stringResource(R.string.delete_category_confirmation_title),
            text = stringResource(R.string.delete_category_confirmation_text, uiState.categoryToDelete?.name ?: ""),
            onConfirm = { viewModel.confirmDeleteCategory(snackbarManager) },
            onDismiss = { viewModel.cancelDelete() }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MainBackgroundBrush)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            CategoryTopBar(
                isEditMode = uiState.isEditMode,
                onBackClick = onBack,
                onEditClick = { viewModel.toggleEditMode() },
                onDoneClick = { viewModel.toggleEditMode() }
            )
            Text(
                text = stringResource(R.string.your_categories),
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp).padding(top = 8.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    CategoryListSection(
                        categories = categories,
                        isEditMode = uiState.isEditMode,
                        onDeleteClick = { viewModel.requestDeleteCategory(it, snackbarManager) }
                    )
                }

                item {
                    AddCategoryButton(onClick = { showBottomSheet = true })
                }
            }
        }

        if (showBottomSheet) {
            CreateCategorySheet(
                sheetState = sheetState,
                newCategoryName = uiState.newCategoryName,
                isCreating = uiState.isCreating,
                onDismissRequest = { showBottomSheet = false },
                onNameChange = viewModel::onNewCategoryNameChange,
                onCreateClick = { viewModel.createCategory(snackbarManager) }
            )
        }

    }
}

@Composable
fun CategoryTopBar(
    isEditMode: Boolean,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDoneClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        CircleNavigationButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.CenterStart)
        )

        Text(
            text = stringResource(id = R.string.category_management),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (isEditMode) {
            Text(
                text = stringResource(id = R.string.done),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable(onClick = onDoneClick)
                    .padding(8.dp),
                textAlign = TextAlign.End,
                fontWeight = FontWeight.SemiBold
            )
        } else {
            Text(
                text = stringResource(id = R.string.edit),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable(onClick = onEditClick)
                    .padding(8.dp),
                textAlign = TextAlign.End,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
