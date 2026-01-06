package com.ltcn272.finny.presentation.features.dashboard.overview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrackChanges
import kotlin.text.isNullOrBlank
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.R
import com.ltcn272.finny.presentation.common.ui.ColumnChart
import com.ltcn272.finny.presentation.common.ui.ColumnChartEntry
import com.ltcn272.finny.presentation.common.ui.ColumnChartMode
import com.ltcn272.finny.presentation.common.ui.ColumnChartWaiting
import com.ltcn272.finny.presentation.common.util.formatCurrency
import com.ltcn272.finny.presentation.common.util.formatCurrencyShortVietnamese
import com.ltcn272.finny.presentation.features.dashboard.DashboardUiState
import com.ltcn272.finny.presentation.features.dashboard.OverviewPeriod
import com.ltcn272.finny.presentation.features.dashboard.overview.component.*

@Composable
fun OverviewScreen(
    uiState: DashboardUiState,
    onPeriodSelected: (OverviewPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        OverviewTopBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
            selectedPeriod = uiState.selectedPeriod,
            onPeriodSelected = onPeriodSelected,
        )

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 160.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                if (uiState.isLoading) {
                    BalanceCardShimmer()
                } else if (uiState.overviewData != null) {
                    val summary = uiState.overviewData.summary
                    OverviewBalanceCard(
                        currentBalance = summary.currentBalance,
                        totalIncome = summary.totalIncome,
                        totalExpenses = summary.totalExpenses
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (uiState.isLoading) {
                        OverviewStatCardShimmer(modifier = Modifier.weight(1f))
                        OverviewStatCardShimmer(modifier = Modifier.weight(1f))
                        OverviewStatCardShimmer(modifier = Modifier.weight(1f))
                    } else if (uiState.overviewData != null) {
                        val summary = uiState.overviewData.summary
                        OverviewStatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.SwapHoriz,
                            iconTint = Color.Blue.copy(alpha = 0.7f),
                            changeText = "+18",
                            changeColor = Color(0xFF28A745),
                            mainValue = summary.transactionCount.toString(),
                            subLabel = stringResource(R.string.transactions)
                        )
                        OverviewStatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            iconTint = Color(0xFFFFA726),
                            changeText = "-12%",
                            changeColor = Color(0xFFDC3545),
                            mainValue = formatCurrencyShortVietnamese(summary.avgPerDay),
                            subLabel = stringResource(R.string.avg_per_day)
                        )
                        OverviewStatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.TrackChanges,
                            iconTint = Color(0xFF20C997),
                            changeText = "Tốt",
                            changeColor = Color(0xFF28A745),
                            mainValue = "${summary.budgetUsagePercent}%",
                            subLabel = stringResource(R.string.budgets)
                        )
                    }
                }
            }

            if (uiState.isLoading) {
                item { InsightCardShimmer() }
            } else if (!uiState.aiInsight.isNullOrBlank()) {
                item { InsightCard(insight = uiState.aiInsight) }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.income_expense_by_day),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(20.dp))
                        if (uiState.isLoading) {
                            ColumnChartWaiting(visibleItemsCount = 7)
                        } else if (uiState.overviewData != null) {
                            ColumnChart(
                                entries = uiState.overviewData.chartData.map {
                                    ColumnChartEntry(it.date, it.label, it.income, it.outcome)
                                },
                                mode = if (uiState.selectedPeriod == OverviewPeriod.WEEK) ColumnChartMode.WEEK else ColumnChartMode.MONTH,
                                incomeColor = Color(0xFF20C997),
                                outcomeColor = Color(0xFFFA5A7D)
                            )
                        }
                    }
                }
            }

            item {
                if (uiState.isLoading) {
                    CategoryBudgetSummaryCardShimmer()
                } else if (uiState.overviewData != null) {
                    CategoryBudgetSummaryCard(
                        categories = uiState.overviewData.categories,
                        budgets = uiState.overviewData.budgets,
                        totalExpense = uiState.overviewData.summary.totalExpenses,
                        onSeeAllClick = { /* TODO: Navigate to another screen */ }
                    )
                }
            }
        }
    }
}
