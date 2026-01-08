package com.ltcn272.finny.presentation.features.transaction.create_edit

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.Transaction
import com.ltcn272.finny.domain.model.TransactionType
import com.ltcn272.finny.presentation.common.ui.AnimatedMoreMenu
import com.ltcn272.finny.presentation.common.ui.CircleNavigationButton
import com.ltcn272.finny.presentation.common.ui.DeleteConfirmationDialog
import com.ltcn272.finny.presentation.common.ui.FinnyDatePickerDialog
import com.ltcn272.finny.presentation.common.ui.ScreenMode
import com.ltcn272.finny.presentation.features.snackbar.LocalSnackbarManager
import com.ltcn272.finny.presentation.common.ui.SelectionDialog
import com.ltcn272.finny.presentation.common.ui.SubmitButton
import com.ltcn272.finny.presentation.common.ui.WheelDateTimePickerDialog
import com.ltcn272.finny.presentation.features.transaction.create_edit.component.*
import com.ltcn272.finny.presentation.theme.TransactionBackgroundBrush
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun CreateEditTransactionScreen(
    viewModel: CreateEditTransactionViewModel = hiltViewModel(),
    transactionToEdit: Transaction? = null,
    predefinedBudgetId: String? = null,
    predefinedDate: LocalDate? = null,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarManager = LocalSnackbarManager.current

    var showMoreMenu by remember { mutableStateOf(false) }

    if (uiState.showDeleteConfirmDialog) {
        DeleteConfirmationDialog(
            title = stringResource(R.string.delete_transaction_confirmation_title),
            text = stringResource(R.string.delete_transaction_confirmation_text, uiState.transactionNameToDelete),
            onConfirm = { viewModel.confirmDeleteTransaction(snackbarManager) },
            onDismiss = { viewModel.cancelDelete() }
        )
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let { viewModel.uploadImage(it, snackbarManager) } }
    )

    val lazyBudgets = viewModel.budgetsPagingFlow.collectAsLazyPagingItems()
    val lazyCategories = viewModel.categoriesPagingFlow.collectAsLazyPagingItems()

    var showDateTimePicker by remember { mutableStateOf(false) }
    var showBudgetPicker by remember { mutableStateOf(false) }
    var showTypePicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showRecurringIntervalPicker by remember { mutableStateOf(false) }
    var showRecurringStartDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(transactionToEdit, predefinedBudgetId, predefinedDate) {
        viewModel.initialize(
            transaction = transactionToEdit,
            predefinedBudgetId = predefinedBudgetId,
            predefinedDate = predefinedDate
        )
    }

    val budgets = lazyBudgets.itemSnapshotList.items
    val categories = lazyCategories.itemSnapshotList.items
    LaunchedEffect(budgets, categories) {
        if (budgets.isNotEmpty() && categories.isNotEmpty()) {
            viewModel.resolveDependencies(budgets, categories)
        }
    }

    LaunchedEffect(uiState.finished) {
        if (uiState.finished) {
            onBack()
        }
    }

    if (showDateTimePicker) {
        WheelDateTimePickerDialog(
            startDateTime = uiState.dateTime.toLocalDateTime(),
            onDismiss = { showDateTimePicker = false },
            onConfirm = {
                viewModel.onDateTimeChange(it.atZone(ZoneId.systemDefault()))
                showDateTimePicker = false
            }
        )
    }

    SelectionDialog(
        title = stringResource(R.string.select_budget),
        items = lazyBudgets,
        visible = showBudgetPicker,
        onDismiss = { showBudgetPicker = false },
        onSelect = viewModel::onBudgetSelected,
        itemToString = { it.name },
        initialSelection = uiState.selectedBudget
    )

    SelectionDialog(
        title = stringResource(R.string.select_category),
        items = lazyCategories,
        visible = showCategoryPicker,
        onDismiss = { showCategoryPicker = false },
        onSelect = viewModel::onCategorySelected,
        itemToString = { it.name.toString() },
        initialSelection = uiState.selectedCategory
    )

    if (showRecurringStartDatePicker) {
        FinnyDatePickerDialog(
            initialDate = uiState.recurringStartDate.toLocalDate(),
            onDismissRequest = { showRecurringStartDatePicker = false },
            onConfirm = {
                viewModel.onRecurringStartDateChange(it.atStartOfDay(ZoneId.systemDefault()))
                showRecurringStartDatePicker = false
            }
        )
    }


    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(TransactionBackgroundBrush)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                CircleNavigationButton(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                if (uiState.mode == ScreenMode.EDIT) {
                    Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                        CircleNavigationButton(icon = Icons.Default.MoreHoriz, onClick = {
                            showMoreMenu = true
                        })
                        AnimatedMoreMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false },
                            onDeleteClick = {
                                showMoreMenu = false
                                viewModel.requestDeleteTransaction()
                            },
                            offset = DpOffset(x = 0.dp, y = 38.dp)
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    val titleRes =
                        if (uiState.mode == ScreenMode.CREATE) R.string.create_transaction_title else R.string.update_transaction_title
                    Text(
                        text = stringResource(titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Spacer(Modifier.height(16.dp))
                }

                item { NameInput(name = uiState.name, onNameChange = viewModel::onNameChange) }

                item {
                    AmountInput(
                        amount = uiState.amount,
                        onAmountChange = viewModel::onAmountChange
                    )
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Column {
                            if (!uiState.isBudgetSelectionHidden) {
                                ValueSelectionRow(
                                    icon = Icons.Default.AccountBalanceWallet,
                                    label = stringResource(R.string.budget),
                                    value = uiState.selectedBudget?.name
                                        ?: stringResource(R.string.select),
                                    onClick = { showBudgetPicker = true }
                                )
                            }
                            Box {
                                ValueSelectionRow(
                                    icon = Icons.AutoMirrored.Filled.List,
                                    label = stringResource(R.string.type),
                                    value = if (uiState.transactionType == TransactionType.INCOME) stringResource(
                                        R.string.incoming
                                    ) else stringResource(R.string.outcoming),
                                    onClick = { showTypePicker = true }
                                )
                                TypeSelectionPopup(
                                    expanded = showTypePicker,
                                    onDismissRequest = { showTypePicker = false },
                                    selectedType = uiState.transactionType,
                                    onTypeSelected = {
                                        viewModel.onTypeSelected(it)
                                        showTypePicker = false
                                    },
                                    offset = DpOffset(x = 0.dp, y = (56).dp)
                                )
                            }
                            TimeSelectionRow(
                                dateTime = uiState.dateTime,
                                onClick = { showDateTimePicker = true }
                            )
                            ValueSelectionRow(
                                icon = Icons.Default.Category,
                                label = stringResource(R.string.category),
                                value = uiState.selectedCategory?.name
                                    ?: stringResource(R.string.select),
                                onClick = { showCategoryPicker = true }
                            )
                        }
                    }
                }

                item {
                    RecurringSection(
                        isRecurring = uiState.isRecurring,
                        recurringStartDate = uiState.recurringStartDate,
                        recurringIntervalUnit = uiState.recurringIntervalUnit,
                        showIntervalPicker = showRecurringIntervalPicker,
                        onRecurringToggled = viewModel::onRecurringToggled,
                        onStartDateClick = { showRecurringStartDatePicker = true },
                        onIntervalClick = { showRecurringIntervalPicker = true },
                        onIntervalPickerDismiss = { showRecurringIntervalPicker = false },
                        onIntervalUnitSelected = {
                            viewModel.onRecurringIntervalUnitSelected(it)
                            showRecurringIntervalPicker = false
                        }
                    )
                }

                item {
                    DescriptionSection(
                        description = uiState.description,
                        onDescriptionChange = viewModel::onDescriptionChange
                    )
                }

                item {
                    AttachmentSection(
                        imageUri = uiState.imageUri,
                        isUploading = uiState.isUploadingImage,
                        onChooseImageClick = { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        onDeleteImageClick = viewModel::onDeleteImageClick
                    )
                }

            }

            SubmitButton(
                mode = uiState.mode,
                isSubmitting = uiState.isSubmitting,
                onClick = { viewModel.submit(snackbarManager) }
            )
        }

        AnimatedVisibility(
            visible = uiState.isDeleting,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val composition by rememberLottieComposition(LottieCompositionSpec.Asset("delete_anim.json"))
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f))
                    .clickable(enabled = false, onClick = {}),
                contentAlignment = Alignment.Center
            ) {
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.size(200.dp)
                )
            }
        }
    }
}
