package com.ltcn272.finny.presentation.features.budget.create_edit

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.domain.model.RecurringIntervalUnit
import com.ltcn272.finny.presentation.common.ui.FinnyDatePickerDialog
import com.ltcn272.finny.presentation.common.ui.SegmentedControl
import com.ltcn272.finny.presentation.common.util.formatDate
import com.ltcn272.finny.presentation.features.budget.create_edit.component.CreateEditTopBar
import com.ltcn272.finny.presentation.common.ui.SubmitButton
import com.ltcn272.finny.presentation.theme.BudgetBackgroundBrush
import dev.muazkadan.switchycompose.ISwitch
import java.time.ZoneId

@Composable
fun CreateEditBudgetScreen(
    viewModel: CreateEditBudgetViewModel = hiltViewModel(),
    budgetToEdit: Budget?,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            viewModel.errorShown()
        }
    }

    LaunchedEffect(uiState.finished) {
        if (uiState.finished) {
            val message = if (uiState.mode == BudgetMode.CREATE) "Tạo ngân sách thành công" else "Cập nhật thành công"
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            onBack()
        }
    }

    if (showDatePicker) {
        FinnyDatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onConfirm = { selectedDate ->
                viewModel.onStartDateSelected(selectedDate.atStartOfDay(ZoneId.systemDefault()))
                showDatePicker = false
            },
            initialDate = uiState.startDate.toLocalDate()
        )
    }

    val intervalUnitStrings = remember { mutableStateMapOf<RecurringIntervalUnit, String>() }
    intervalUnitStrings[RecurringIntervalUnit.DAY] = stringResource(R.string.unit_day)
    intervalUnitStrings[RecurringIntervalUnit.WEEK] = stringResource(R.string.unit_week)
    intervalUnitStrings[RecurringIntervalUnit.MONTH] = stringResource(R.string.unit_month)
    intervalUnitStrings[RecurringIntervalUnit.YEAR] = stringResource(R.string.unit_year)

    LaunchedEffect(Unit) {
        viewModel.initialize(budgetToEdit)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BudgetBackgroundBrush)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        CreateEditTopBar(onBack = onBack)

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                val title = if (uiState.mode == BudgetMode.CREATE)
                    stringResource(R.string.create_budget_title)
                else
                    stringResource(R.string.update_budget_title)
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Spacer(Modifier.height(16.dp))
            }

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
                                text = stringResource(R.string.budget_name_label),
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

            item {
                BasicTextField(
                    value = uiState.amount,
                    onValueChange = viewModel::onAmountChange,
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
                Spacer(Modifier.height(16.dp))
            }

            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.9f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(text = stringResource(R.string.recurring_top_up))
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

            item {
                AnimatedVisibility(visible = uiState.isRecurring) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        BasicTextField(
                            value = uiState.recurringTopupAmount,
                            onValueChange = viewModel::onRecurringTopupAmountChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .height(60.dp)
                                .background(Color.White, shape = RoundedCornerShape(16.dp)),
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
                                if (uiState.recurringTopupAmount.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.recurring_topup_amount),
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                innerTextField()
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(horizontal = 10.dp, vertical = 16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.cycle),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = stringResource(R.string.how_often_renew_budget),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 28.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                val items = remember { RecurringIntervalUnit.entries }
                                SegmentedControl(
                                    options = items,
                                    selected = uiState.recurringIntervalUnit,
                                    onOptionClicked = viewModel::onIntervalSelected,
                                    titleForItem = { intervalUnitStrings[it] ?: it.name },
                                    indicatorPadding = 2.dp
                                )
                            }
                        }

                        Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { showDatePicker = true }
                                    .padding(horizontal = 10.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = Color.Gray
                                    )
                                    Text(text = stringResource(R.string.start_date))
                                }
                                Box(
                                    modifier = Modifier
                                        .background(
                                            Color(0xFFEEEEEF),
                                            shape = RoundedCornerShape(15.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = formatDate(uiState.startDate),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        uiState.nextRunAt?.let { nextRunDate ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 12.dp),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(
                                        text = stringResource(R.string.next_top_up_on),
                                        color = Color.Gray,
                                        fontSize = 13.sp
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = formatDate(nextRunDate),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        SubmitButton(
            mode = uiState.mode,
            isSubmitting = uiState.isSubmitting,
            onClick = viewModel::submit
        )
    }
}

