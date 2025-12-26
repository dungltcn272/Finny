package com.ltcn272.finny.presentation.features.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.BudgetReportDetail
import com.ltcn272.finny.domain.model.CategoryReportDetail
import com.ltcn272.finny.presentation.common.ui.DateRangePickerDialog
import com.ltcn272.finny.presentation.common.ui.DonutChart
import com.ltcn272.finny.presentation.common.ui.DonutChartWaiting
import com.ltcn272.finny.presentation.common.ui.SegmentedControl
import com.ltcn272.finny.presentation.common.util.formatCurrency
import com.ltcn272.finny.presentation.features.dashboard.component.DashboardTopBar
import com.ltcn272.finny.presentation.features.dashboard.component.ReportDetailItem
import com.ltcn272.finny.presentation.features.dashboard.component.ReportDetailItemShimmer
import com.ltcn272.finny.presentation.features.dashboard.component.TotalStatsCard
import com.ltcn272.finny.presentation.features.dashboard.component.TotalStatsCardShimmer
import com.ltcn272.finny.presentation.theme.MainBackgroundBrush

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val tabOptions = remember { ReportTab.entries }
    val tabTitles = remember {
        mapOf(
            ReportTab.BUDGETS to R.string.budgets,
            ReportTab.CATEGORIES to R.string.categories
        )
    }.mapValues { stringResource(id = it.value) }

    if (uiState.showDatePicker) {
        DateRangePickerDialog(
            onDismissRequest = viewModel::onDismissDatePicker,
            onConfirm = viewModel::setDateRange
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MainBackgroundBrush)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        DashboardTopBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            title = uiState.timeRangeTitle,
            onBack = viewModel::previousDateRange,
            onNext = viewModel::nextDateRange,
            onTitleClick = viewModel::onShowDatePicker
        )

        if (uiState.isLoading) {
            LazyColumn(
                contentPadding = PaddingValues(top = 4.dp, bottom = 160.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                userScrollEnabled = false
            ) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        TotalStatsCardShimmer(modifier = Modifier.weight(1f))
                        TotalStatsCardShimmer(modifier = Modifier.weight(1f))
                    }
                }
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        DonutChartWaiting(modifier = Modifier.fillMaxWidth(0.7f), strokeWidthDp = 32.dp)
                    }
                }
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            repeat(3) { index ->
                                ReportDetailItemShimmer()
                                if (index < 2) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                }
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(top = 4.dp, bottom = 160.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    uiState.totals?.let { totals ->
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            TotalStatsCard(
                                modifier = Modifier.weight(1f),
                                label = stringResource(R.string.outcoming),
                                amount = totals.outcome,
                                change = totals.outcome - totals.prevOutcome,
                                isIncome = false
                            )
                            TotalStatsCard(
                                modifier = Modifier.weight(1f),
                                label = stringResource(R.string.incoming),
                                amount = totals.income,
                                change = totals.income - totals.prevIncome,
                                isIncome = true
                            )
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(
                                R.string.net_income_expense,
                                formatCurrency(uiState.totals?.net ?: 0.0, "VND")
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        DonutChart(
                            items = uiState.donutChartData,
                            totalLabel = stringResource(R.string.total_outcome),
                            totalAmount = uiState.totals?.outcome ?: 0.0,
                            modifier = Modifier.fillMaxWidth(0.7f),
                            strokeWidthDp = 32.dp
                        )
                    }
                }

                item {
                    SegmentedControl(
                        options = tabOptions,
                        selected = uiState.selectedTab,
                        onOptionClicked = viewModel::onTabSelected,
                        titleForItem = { tab -> tabTitles[tab] ?: "" },
                        indicatorPadding = 3.dp
                    )
                }

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            if (uiState.reportDetailItems.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.no_data_available),
                                    modifier = Modifier
                                        .padding(vertical = 32.dp)
                                        .align(Alignment.CenterHorizontally),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                uiState.reportDetailItems.forEachIndexed { index, item ->
                                    when (item) {
                                        is BudgetReportDetail -> ReportDetailItem(
                                            name = item.name,
                                            amountText = "${formatCurrency(item.outcome, "VND")}/${
                                                formatCurrency(
                                                    item.limit,
                                                    "VND"
                                                )
                                            }",
                                            progress = item.ratio.toFloat()
                                        )
                                        is CategoryReportDetail -> ReportDetailItem(
                                            name = item.name,
                                            amountText = formatCurrency(item.outcome, "VND"),
                                            progress = -1f
                                        )
                                    }
                                    if (index < uiState.reportDetailItems.lastIndex) {
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                            thickness = 1.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
