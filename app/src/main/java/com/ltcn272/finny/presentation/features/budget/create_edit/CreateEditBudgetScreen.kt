package com.ltcn272.finny.presentation.features.budget.create_edit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.domain.model.RecurringIntervalUnit
import com.ltcn272.finny.presentation.common.ui.FinnyDatePickerDialog
import com.ltcn272.finny.presentation.common.ui.ScreenMode
import com.ltcn272.finny.presentation.common.ui.SubmitButton
import com.ltcn272.finny.presentation.features.budget.create_edit.component.BudgetNameAndAmountInputs
import com.ltcn272.finny.presentation.features.budget.create_edit.component.CreateEditTopBar
import com.ltcn272.finny.presentation.features.budget.create_edit.component.RecurringConfigSection
import com.ltcn272.finny.presentation.features.budget.create_edit.component.RecurringToggleSection
import com.ltcn272.finny.presentation.features.snackbar.LocalSnackbarManager
import com.ltcn272.finny.presentation.theme.BudgetBackgroundBrush
import java.time.ZoneId

@Composable
fun CreateEditBudgetScreen(
    viewModel: CreateEditBudgetViewModel = hiltViewModel(),
    budgetToEdit: Budget?,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    val snackbarManager = LocalSnackbarManager.current

    LaunchedEffect(uiState.finished) {
        if (uiState.finished) {
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

    Box(modifier = Modifier.fillMaxSize()) {
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    val titleRes = if (uiState.mode == ScreenMode.CREATE)
                        R.string.create_budget_title
                    else
                        R.string.update_budget_title
                    Text(
                        text = stringResource(titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }

                item {
                    BudgetNameAndAmountInputs(
                        name = uiState.name,
                        onNameChange = viewModel::onNameChange,
                        amount = uiState.amount,
                        onAmountChange = viewModel::onAmountChange
                    )
                }

                item {
                    RecurringToggleSection(
                        isRecurring = uiState.isRecurring,
                        onRecurringToggled = viewModel::onRecurringToggled
                    )
                }

                item {
                    AnimatedVisibility(visible = uiState.isRecurring) {
                        RecurringConfigSection(
                            recurringTopupAmount = uiState.recurringTopupAmount,
                            onRecurringTopupAmountChange = viewModel::onRecurringTopupAmountChange,
                            intervalUnit = uiState.recurringIntervalUnit,
                            intervalUnitStrings = intervalUnitStrings,
                            onIntervalSelected = viewModel::onIntervalSelected,
                            startDate = uiState.startDate,
                            onStartDateClick = { showDatePicker = true },
                            nextRunAt = uiState.nextRunAt
                        )
                    }
                }
            }

            SubmitButton(
                mode = uiState.mode,
                isSubmitting = uiState.isSubmitting,
                onClick = { viewModel.submit(snackbarManager) }
            )
        }
    }
}
