package com.ltcn272.finny.presentation.features.dashboard.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.BudgetReportDetail
import com.ltcn272.finny.domain.model.CategoryReportDetail
import com.ltcn272.finny.presentation.common.ui.DonutChart
import com.ltcn272.finny.presentation.common.ui.DonutChartWaiting
import com.ltcn272.finny.presentation.common.ui.SegmentedControl
import com.ltcn272.finny.presentation.common.util.formatCurrency
import com.ltcn272.finny.presentation.features.dashboard.DashboardUiState
import com.ltcn272.finny.presentation.features.dashboard.ReportTab
import com.ltcn272.finny.presentation.features.dashboard.report.component.ReportDetailItem
import com.ltcn272.finny.presentation.features.dashboard.report.component.ReportDetailItemShimmer
import com.ltcn272.finny.presentation.features.dashboard.report.component.ReportTopBar
import com.ltcn272.finny.presentation.features.dashboard.report.component.TotalStatsCard
import com.ltcn272.finny.presentation.features.dashboard.report.component.TotalStatsCardShimmer

@Composable
fun ReportScreen(
    uiState: DashboardUiState,
    onTabSelected: (ReportTab) -> Unit,
    onBackDateRange: () -> Unit,
    onNextDateRange: () -> Unit,
    onTitleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tabOptions = remember { ReportTab.entries }
    val tabTitles = remember {
        mapOf(
            ReportTab.BUDGETS to R.string.budgets,
            ReportTab.CATEGORIES to R.string.categories
        )
    }.mapValues { stringResource(id = it.value) }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        ReportTopBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            title = uiState.timeRangeTitle,
            onBack = onBackDateRange,
            onNext = onNextDateRange,
            onTitleClick = onTitleClick
        )
        val reportData = uiState.activeReportData
        val donutData = uiState.activeDonutChartData
        if (uiState.isLoading) {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = 4.dp,
                    bottom = 160.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
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
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        DonutChartWaiting(
                            modifier = Modifier.fillMaxWidth(0.7f),
                            strokeWidthDp = 32.dp
                        )
                    }
                }
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            repeat(3) { index ->
                                ReportDetailItemShimmer()
                                if (index < 2) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outline.copy(
                                            alpha = 0.1f
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = 4.dp,
                    bottom = 160.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    reportData?.totals?.let { totals ->
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
                                formatCurrency(
                                    reportData?.totals?.net ?: 0.0,
                                    "VND"
                                )
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        DonutChart(
                            items = donutData,
                            totalLabel = stringResource(R.string.total_spent),
                            totalAmount = donutData.sumOf { it.value.toDouble() },
                            modifier = Modifier.fillMaxWidth(0.7f),
                            strokeWidthDp = 32.dp
                        )
                    }
                }

                item {
                    SegmentedControl(
                        options = tabOptions,
                        selected = uiState.selectedTab,
                        onOptionClicked = onTabSelected,
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
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            val detailItems = reportData?.detailItems
                                ?: emptyList()
                            if (detailItems.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.no_data_available),
                                    modifier = Modifier
                                        .padding(vertical = 32.dp)
                                        .align(Alignment.CenterHorizontally),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                detailItems.forEachIndexed { index, item ->
                                    when (item) {
                                        is BudgetReportDetail -> ReportDetailItem(
                                            name = item.name,
                                            amountText = "${
                                                formatCurrency(
                                                    item.outcome,
                                                    "VND"
                                                )
                                            }/${formatCurrency(item.limit, "VND")}",
                                            progress = item.ratio.toFloat()
                                        )

                                        is CategoryReportDetail -> ReportDetailItem(
                                            name = item.name,
                                            amountText = formatCurrency(item.outcome, "VND"),
                                            progress = -1f
                                        )
                                    }
                                    if (index < detailItems.lastIndex) {
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
