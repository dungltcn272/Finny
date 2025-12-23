package com.ltcn272.finny.presentation.features.transaction.create_edit

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.RecurringIntervalUnit
import com.ltcn272.finny.domain.model.Transaction
import com.ltcn272.finny.domain.model.TransactionType
import com.ltcn272.finny.presentation.common.ui.CircleNavigationButton
import com.ltcn272.finny.presentation.common.ui.FinnyDatePickerDialog
import com.ltcn272.finny.presentation.common.ui.SelectionDialog
import com.ltcn272.finny.presentation.common.ui.SubmitButton
import com.ltcn272.finny.presentation.common.ui.WheelDateTimePickerDialog
import com.ltcn272.finny.presentation.features.budget.create_edit.BudgetMode
import com.ltcn272.finny.presentation.features.transaction.create_edit.component.ActionSelectionRow
import com.ltcn272.finny.presentation.features.transaction.create_edit.component.DateSelectionRow
import com.ltcn272.finny.presentation.features.transaction.create_edit.component.RecurringIntervalPickerPopup
import com.ltcn272.finny.presentation.features.transaction.create_edit.component.TimeSelectionRow
import com.ltcn272.finny.presentation.features.transaction.create_edit.component.TypeSelectionPopup
import com.ltcn272.finny.presentation.features.transaction.create_edit.component.ValueSelectionRow
import com.ltcn272.finny.presentation.theme.TransactionBackgroundBrush
import dev.muazkadan.switchycompose.ISwitch
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
    val context = LocalContext.current

    val lazyBudgets = viewModel.budgetsPagingFlow.collectAsLazyPagingItems()
    val lazyCategories = viewModel.categoriesPagingFlow.collectAsLazyPagingItems()

    var showDateTimePicker by remember { mutableStateOf(false) }
    var showBudgetPicker by remember { mutableStateOf(false) }
    var showTypePicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showRecurringIntervalPicker by remember { mutableStateOf(false) }
    var showRecurringStartDatePicker by remember { mutableStateOf(false) }


    // --- SỬA LẠI HOÀN TOÀN LOGIC KHỞI TẠO Ở ĐÂY ---

    // 1. Gọi initialize ngay lập tức với dữ liệu có sẵn ban đầu
    LaunchedEffect(transactionToEdit, predefinedBudgetId, predefinedDate) {
        viewModel.initialize(
            transaction = transactionToEdit,
            predefinedBudgetId = predefinedBudgetId,
            predefinedDate = predefinedDate
        )
    }

    // 2. Khi Paging tải xong, gọi hàm resolveDependencies để cập nhật nốt budget/category
    val budgets = lazyBudgets.itemSnapshotList.items
    val categories = lazyCategories.itemSnapshotList.items
    LaunchedEffect(budgets, categories) {
        // Chỉ gọi khi danh sách không rỗng để đảm bảo có dữ liệu
        if (budgets.isNotEmpty() && categories.isNotEmpty()) {
            viewModel.resolveDependencies(budgets, categories)
        }
    }
    // ----------------------------------------------------


    // Hiển thị lỗi
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.errorShown()
        }
    }

    // Xử lý khi hoàn thành (tạo/sửa thành công)
    LaunchedEffect(uiState.finished) {
        if (uiState.finished) {
            val message =
                if (uiState.mode == TransactionMode.CREATE) "Tạo giao dịch thành công" else "Cập nhật thành công"
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            onBack()
        }
    }


    // --- Khai báo các Dialog ---
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
        items = lazyBudgets.itemSnapshotList.items, // Lấy list từ Paging
        visible = showBudgetPicker,
        onDismiss = { showBudgetPicker = false },
        onSelect = viewModel::onBudgetSelected,
        itemToString = { it.name }
    )

    SelectionDialog(
        title = stringResource(R.string.select_category),
        items = lazyCategories.itemSnapshotList.items, // Lấy list từ Paging
        visible = showCategoryPicker,
        onDismiss = { showCategoryPicker = false },
        onSelect = viewModel::onCategorySelected,
        itemToString = { it.name.toString() }
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

    // --- Giao diện chính ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TransactionBackgroundBrush)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // ... (Top Bar giữ nguyên)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            CircleNavigationButton(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                onClick = onBack
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                val title = if (uiState.mode == TransactionMode.CREATE)
                    stringResource(R.string.create_transaction_title)
                else
                    stringResource(R.string.update_transaction_title)
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Spacer(Modifier.height(16.dp))
            }

            // Tên giao dịch
            item {
                BasicTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                ) { innerTextField ->
                    Box(contentAlignment = Alignment.Center) {
                        if (uiState.name.isEmpty()) {
                            Text(
                                text = stringResource(R.string.transaction_name_label),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }
                        innerTextField()
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Số tiền
            item {
                BasicTextField(
                    value = uiState.amount,
                    onValueChange = viewModel::onAmountChange,
                    // ... (các modifier còn lại giữ nguyên)
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(
                            Color.White.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = TextStyle(
                        textAlign = TextAlign.Center,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                ) { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.amount.isEmpty()) {
                            Text(
                                text = stringResource(R.string.amount),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        innerTextField()
                    }
                }
            }

            // Budget
            if (!uiState.isBudgetSelectionHidden) {
                item {
                    Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                        ValueSelectionRow(
                            icon = Icons.Default.AccountBalanceWallet,
                            label = stringResource(R.string.budget),
                            value = uiState.selectedBudget?.name ?: stringResource(R.string.select),
                            onClick = { showBudgetPicker = true }
                        )
                    }
                }
            }

            // Loại
            item {
                Box {
                    Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                        ValueSelectionRow(
                            icon = Icons.AutoMirrored.Filled.List,
                            label = stringResource(R.string.type),
                            value = if (uiState.transactionType == TransactionType.INCOME)
                                stringResource(R.string.incoming)
                            else
                                stringResource(R.string.outcoming),
                            onClick = { showTypePicker = true }
                        )
                    }
                    TypeSelectionPopup(
                        expanded = showTypePicker,
                        onDismissRequest = { showTypePicker = false },
                        selectedType = uiState.transactionType,
                        onTypeSelected = {
                            viewModel.onTypeSelected(it)
                            showTypePicker = false
                        },
                        // Offset so với item cha
                        offset = DpOffset(x = 0.dp, y = (56).dp)
                    )
                }
            }

            // Thời gian
            item {
                Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                    TimeSelectionRow(
                        dateTime = uiState.dateTime,
                        onClick = { showDateTimePicker = true }
                    )
                }
            }

            // Danh mục
            item {
                Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                    ValueSelectionRow(
                        icon = Icons.Default.Category,
                        label = stringResource(R.string.category),
                        value = uiState.selectedCategory?.name ?: stringResource(R.string.select),
                        onClick = { showCategoryPicker = true }
                    )
                }
            }

            // ... (Các item còn lại được kết nối tương tự với state và event handlers từ ViewModel)

            // Giao dịch định kỳ
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Sync,
                                null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.recurring_transaction),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        ISwitch(
                            checked = uiState.isRecurring,
                            onCheckedChange = viewModel::onRecurringToggled,
                            buttonHeight = 28.dp,
                            innerPadding = 3.dp
                        )
                    }
                }
            }

            // Các tùy chọn cho giao dịch định kỳ
            item {
                AnimatedVisibility(visible = uiState.isRecurring) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                            DateSelectionRow(
                                icon = Icons.Default.CalendarViewDay,
                                label = stringResource(id = R.string.recurring_start_date),
                                date = uiState.recurringStartDate,
                                onClick = { showRecurringStartDatePicker = true }
                            )
                        }
                        Box {
                            Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                                ValueSelectionRow(
                                    icon = Icons.Default.Schedule,
                                    label = stringResource(id = R.string.recurring_interval),
                                    value = "1 ${ // Tạm thời vẫn là 1
                                        when (uiState.recurringIntervalUnit) {
                                            RecurringIntervalUnit.DAY -> stringResource(id = R.string.unit_day)
                                            RecurringIntervalUnit.WEEK -> stringResource(id = R.string.unit_week)
                                            RecurringIntervalUnit.MONTH -> stringResource(id = R.string.unit_month)
                                            RecurringIntervalUnit.YEAR -> stringResource(id = R.string.unit_year)
                                        }
                                    }",
                                    onClick = { showRecurringIntervalPicker = true }
                                )
                            }
                            RecurringIntervalPickerPopup(
                                expanded = showRecurringIntervalPicker,
                                onDismissRequest = { showRecurringIntervalPicker = false },
                                selectedUnit = uiState.recurringIntervalUnit,
                                onUnitSelected = {
                                    viewModel.onRecurringIntervalChange(it, uiState.recurringIntervalValue)
                                    showRecurringIntervalPicker = false
                                },
                                // Offset so với item cha
                                offset = DpOffset(x = 0.dp, y = (56).dp)
                            )
                        }
                    }
                }
            }

            // Mô tả
            item {
                Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                    if (uiState.description == null) {
                        ActionSelectionRow(
                            icon = Icons.Default.Notes,
                            label = stringResource(R.string.description),
                            actionText = stringResource(R.string.add_description),
                            onClick = { viewModel.onDescriptionChange("") }
                        )
                    } else {
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    stringResource(R.string.description),
                                    fontWeight = FontWeight.SemiBold
                                )
                                TextButton(onClick = { viewModel.onDescriptionChange(null) }) {
                                    Text(stringResource(R.string.delete), color = Color.Red)
                                }
                            }
                            OutlinedTextField(
                                value = uiState.description!!,
                                onValueChange = viewModel::onDescriptionChange,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 80.dp),
                                placeholder = { Text("Ngon lắm nhé...") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(
                                        alpha = 0.5f
                                    ),
                                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.7f)
                                )
                            )
                        }
                    }
                }
            }

            // Ảnh
            item {
                Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ReceiptLong, null, tint = Color.Gray)
                                Text(
                                    stringResource(R.string.attachments),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(
                                        Color.LightGray.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { /* Mở trình chọn ảnh */ }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.choose_image),
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.select_and_upload_receipt),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        androidx.compose.animation.AnimatedVisibility(visible = uiState.imageUri != null) {
                            // Image(...)
                        }
                    }
                }
            }
        }

        SubmitButton(
            mode = if (uiState.mode == TransactionMode.CREATE) BudgetMode.CREATE else BudgetMode.EDIT,
            isSubmitting = uiState.isSubmitting,
            onClick = viewModel::submit
        )
    }
}
