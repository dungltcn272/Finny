package com.ltcn272.finny.presentation.features.transaction.transaction_list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.DaySection
import com.ltcn272.finny.domain.model.Transaction
import com.ltcn272.finny.domain.model.TransactionType
import com.ltcn272.finny.presentation.common.ui.DateRangePickerDialog
import com.ltcn272.finny.presentation.features.transaction.transaction_list.component.BudgetFilterRow
import com.ltcn272.finny.presentation.features.transaction.transaction_list.component.BudgetFilterRowShimmer
import com.ltcn272.finny.presentation.features.transaction.transaction_list.component.RecurringTransactionLink
import com.ltcn272.finny.presentation.features.transaction.transaction_list.component.TransactionDayGroup
import com.ltcn272.finny.presentation.features.transaction.transaction_list.component.TransactionDayGroupShimmer
import com.ltcn272.finny.presentation.features.transaction.transaction_list.component.TransactionTopBar
import com.ltcn272.finny.presentation.theme.MainBackgroundBrush
import java.time.LocalDate

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TransactionScreen(
    viewModel: TransactionViewModel = hiltViewModel(),
    onTransactionClick: (Transaction) -> Unit = {},
    onNavigateToRecurring: () -> Unit = {},
    onNavigateToBudgetSettings: () -> Unit = {},
    onAddTransaction: (LocalDate?) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val lazyBudgets = viewModel.budgetsPagingFlow.collectAsLazyPagingItems()
    val lazyTransactions = viewModel.transactionsPagingFlow.collectAsLazyPagingItems()

    val isInitialTransactionLoading =
        lazyTransactions.loadState.refresh is LoadState.Loading && lazyTransactions.itemCount == 0
    val isInitialBudgetLoading =
        lazyBudgets.loadState.refresh is LoadState.Loading && lazyBudgets.itemCount == 0

    LaunchedEffect(lazyTransactions.loadState.refresh) {
        if (lazyTransactions.loadState.refresh !is LoadState.Loading) {
            viewModel.onRefreshFinished()
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshingByUser,
        onRefresh = {
            viewModel.onUserPullToRefresh()
            lazyTransactions.refresh()
            lazyBudgets.refresh()
        }
    )

    val groupedTransactions = remember(lazyTransactions.itemSnapshotList) {
        lazyTransactions.itemSnapshotList.items
            .groupBy { it.dateTime.toLocalDate() }
            .map { (date, dailyTransactions) ->
                val total =
                    dailyTransactions.sumOf { if (it.type == TransactionType.INCOME) it.amount else -it.amount }
                DaySection(
                    date = date,
                    totalAmount = total,
                    transactions = dailyTransactions.sortedByDescending { it.dateTime }
                )
            }
            .sortedByDescending { it.date }
    }

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
        TransactionTopBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            title = uiState.timeRangeTitle,
            onBack = viewModel::navigateToPreviousWeek,
            onNext = viewModel::navigateToNextWeek,
            onTitleClick = viewModel::onShowDatePicker
        )

        if (isInitialBudgetLoading) {
            BudgetFilterRowShimmer(
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 4.dp
                )
            )
        } else {
            BudgetFilterRow(
                modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                budgets = lazyBudgets.itemSnapshotList.items,
                selectedBudget = uiState.selectedBudget,
                onBudgetSelected = viewModel::setBudgetFilter,
                onSettingClick = onNavigateToBudgetSettings
            )
        }

        Spacer(Modifier.height(8.dp))
        RecurringTransactionLink(
            modifier = Modifier.padding(horizontal = 16.dp),
            onClick = onNavigateToRecurring
        )

        Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 160.dp
                ),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (isInitialTransactionLoading) {
                    items(3) { TransactionDayGroupShimmer() }
                }

                if (!isInitialTransactionLoading) {
                    if (groupedTransactions.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.no_transactions_in_period),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(30.dp),
                                style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        items(
                            count = groupedTransactions.size,
                            key = { index -> groupedTransactions[index].date }
                        ) { index ->
                            val daySection = groupedTransactions[index]
                            TransactionDayGroup(
                                daySection = daySection,
                                currencyCode = uiState.currency,
                                onAddTransactionForDate = { date -> onAddTransaction(date) },
                                onTransactionClick = { transaction -> onTransactionClick(transaction) }
                            )
                        }
                    }
                }

                if (lazyTransactions.loadState.append is LoadState.Loading) {
                    item { TransactionDayGroupShimmer(modifier = Modifier.padding(top = 8.dp)) }
                }
            }

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
