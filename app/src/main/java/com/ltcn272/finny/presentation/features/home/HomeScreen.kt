package com.ltcn272.finny.presentation.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.domain.model.Transaction
import com.ltcn272.finny.domain.model.TransactionType
import com.ltcn272.finny.presentation.common.ui.BudgetItem
import com.ltcn272.finny.presentation.common.ui.SliderFilterRow
import com.ltcn272.finny.presentation.common.ui.TransactionItem
import com.ltcn272.finny.presentation.features.home.component.BudgetDistributionCard
import com.ltcn272.finny.presentation.features.home.component.FeaturedBudgetCard
import com.ltcn272.finny.presentation.features.home.component.HomeHeader
import com.ltcn272.finny.presentation.features.home.component.HomeScreenShimmer
import com.ltcn272.finny.presentation.features.home.component.ListHeader
import com.ltcn272.finny.presentation.theme.FinnyTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onNotificationClick: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onSeeAllBudgets: () -> Unit = {},
    onSeeAllTransactions: () -> Unit = {},
    onBudgetClick: (Budget) -> Unit = {},
    onTransactionClick: (Transaction) -> Unit = {},
    onCreateTransactionClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshingByUser,
        onRefresh = viewModel::refreshDataFromPull
    )

    val incomeString = stringResource(id = R.string.incoming)
    val outgoingString = stringResource(id = R.string.outcome)
    val currencyCode = uiState.currency

    FinnyTheme {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .pullRefresh(pullRefreshState) // Modifier vẫn giữ nguyên
        ) {
            if (uiState.isLoading && uiState.topBudgets.isEmpty()) {
                HomeScreenShimmer(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Header
                    item {
                        HomeHeader(
                            username = uiState.username ?: "...",
                            currentDate = uiState.currentDate,
                            onNotificationClick = onNotificationClick,
                            onSettingsClick = onSettingsClick
                        )
                    }

                    // Các item còn lại giữ nguyên...
                    uiState.featuredBudget?.let { featuredBudget ->
                        item {
                            FeaturedBudgetCard(
                                budget = featuredBudget,
                                onCardClick = { onBudgetClick(featuredBudget) }
                            )
                        }
                    }

                    uiState.chartDataSet?.let { chartDataSet ->
                        item {
                            BudgetDistributionCard(
                                chartDataSet = chartDataSet,
                                legendData = uiState.legendData,
                                totalRemainder = uiState.totalRemainder,
                                currencyCode = currencyCode,
                                pieColors = uiState.pieColors
                            )
                        }
                    }

                    if (uiState.topBudgets.isNotEmpty()) {
                        item {
                            ListHeader(
                                title = stringResource(R.string.your_budgets),
                                onSeeAllClick = onSeeAllBudgets
                            )
                        }
                        items(uiState.topBudgets, key = { it.serverId!! }) { budget ->
                            BudgetItem(
                                budget = budget,
                                onClick = { onBudgetClick(budget) }
                            )
                        }
                    }

                    if (uiState.transactionsToShow.isNotEmpty()) {
                        item {
                            ListHeader(
                                title = stringResource(R.string.latest_transactions),
                                onSeeAllClick = onSeeAllTransactions
                            )
                        }
                        item {
                            val filterItems = remember { TransactionType.entries }
                            SliderFilterRow(
                                items = filterItems,
                                selectedItem = uiState.transactionFilterType,
                                onItemSelected = viewModel::setTransactionFilter,
                                itemToString = { type ->
                                    when (type) {
                                        TransactionType.INCOME -> incomeString
                                        TransactionType.OUTCOME -> outgoingString
                                    }
                                },
                                selectedBg = MaterialTheme.colorScheme.primary,
                                unselectedBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                selectedText = MaterialTheme.colorScheme.onPrimary,
                                unselectedText = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        items(uiState.transactionsToShow, key = { it.serverId }) { transaction ->
                            TransactionItem(
                                transaction = transaction,
                                currencyCode = currencyCode,
                                onClick = { onTransactionClick(transaction) }
                            )
                        }
                    } else if (!uiState.isLoading) {
                        item {
                            Text(
                                text = "Không có giao dịch nào.",
                                modifier = Modifier.padding(vertical = 20.dp)
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }


            // Floating Action Button
            FloatingActionButton(
                onClick = onCreateTransactionClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_transaction)
                )
            }

            // Pull to refresh indicator
            PullRefreshIndicator(
                refreshing = uiState.isRefreshingByUser,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}
