// Trong file presentation/features/budget/create_budget/CreateBudgetScreen.kt

package com.ltcn272.finny.presentation.features.budget.create_budget

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ltcn272.finny.R
import com.ltcn272.finny.presentation.common.ui.CircleIconButton
import com.ltcn272.finny.presentation.common.ui.FinnyDatePickerDialog
import com.ltcn272.finny.presentation.common.ui.SubmitButtonWithTitle
import com.ltcn272.finny.presentation.theme.BudgetBackground
import java.time.ZoneId

@Composable
fun CreateBudgetScreen(
    viewModel: CreateBudgetViewModel = hiltViewModel(),
    onBack: () -> Unit, // Bỏ giá trị mặc định để bắt buộc truyền vào
) {

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            val message = if (uiState.isEditMode) "Budget updated!" else "Budget created!"
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            onBack()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    if (uiState.isDatePickerVisible) {
        FinnyDatePickerDialog(
            onDismissRequest = { viewModel.dismissDatePicker() },
            onConfirm = { viewModel.onDateSelected(it) },
            initialDate = uiState.selectedDate
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = BudgetBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {

        CircleIconButton(onClick = onBack, icon = R.drawable.ic_left)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BudgetForm(
                isEditMode = uiState.isEditMode, // Truyền isEditMode
                name = uiState.name,
                onNameChange = viewModel::onNameChange,
                amount = uiState.amount,
                onAmountChange = viewModel::onAmountChange,
                dateTimeMillis = uiState.selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                onOpenDateTimePicker = { viewModel.showDatePicker() },
                period = uiState.selectedPeriod,
                onPeriodChange = { viewModel.onPeriodSelected(it) },
            )
        }

        if (uiState.saveInProgress) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            SubmitButtonWithTitle(
                modifier = Modifier.align(Alignment.BottomCenter),
                title = if (uiState.isEditMode) "Update" else "Save", // Cập nhật tiêu đề nút
                onClick = { viewModel.saveBudget() }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
