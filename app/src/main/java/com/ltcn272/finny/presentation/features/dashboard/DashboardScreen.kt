package com.ltcn272.finny.presentation.features.dashboard

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ltcn272.finny.presentation.common.ui.DateRangePickerDialog
import com.ltcn272.finny.presentation.common.ui.ModeSwitcherBubble
import com.ltcn272.finny.presentation.features.dashboard.overview.OverviewScreen
import com.ltcn272.finny.presentation.features.dashboard.report.ReportScreen
import com.ltcn272.finny.presentation.features.snackbar.LocalSnackbarManager
import com.ltcn272.finny.presentation.features.snackbar.TopSnackbarType
import com.ltcn272.finny.presentation.theme.MainBackgroundBrush
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarManager = LocalSnackbarManager.current

    LaunchedEffect(Unit) {
        viewModel.dashboardErrorEvent.collectLatest { errorMessage ->
            snackbarManager.showMessage(errorMessage, TopSnackbarType.ERROR)
        }
    }

    if (uiState.showDatePicker) {
        DateRangePickerDialog(
            onDismissRequest = viewModel::onDismissDatePicker,
            onConfirm = viewModel::setDateRange
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MainBackgroundBrush)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Crossfade(
                targetState = uiState.dashboardMode,
                label = "dashboard_mode_switcher"
            ) { mode ->
                when (mode) {
                    DashboardMode.OVERVIEW -> OverviewScreen(
                        uiState = uiState,
                        onPeriodSelected = viewModel::onPeriodSelected
                    )

                    DashboardMode.REPORT -> ReportScreen(
                        uiState = uiState,
                        onTabSelected = viewModel::onTabSelected,
                        onBackDateRange = viewModel::previousDateRange,
                        onNextDateRange = viewModel::nextDateRange,
                        onTitleClick = viewModel::onShowDatePicker
                    )
                }
            }
        }

        ModeSwitcherBubble(
            onLongPress = viewModel::toggleDashboardMode
        )
    }
}
