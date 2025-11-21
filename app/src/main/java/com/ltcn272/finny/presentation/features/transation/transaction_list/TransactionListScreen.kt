package com.ltcn272.finny.presentation.features.transation.transaction_list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ltcn272.finny.presentation.common.ui.DateRangePickerDialog
import com.ltcn272.finny.presentation.features.transation.transaction_list.component.BudgetFilterRow
import com.ltcn272.finny.presentation.features.transation.transaction_list.component.BudgetFilterRowShimmer
import com.ltcn272.finny.presentation.features.transation.transaction_list.component.DaySectionItem
import com.ltcn272.finny.presentation.features.transation.transaction_list.component.DaySectionItemShimmer
import com.ltcn272.finny.presentation.features.transation.transaction_list.component.TransactionTopBar
import com.ltcn272.finny.presentation.theme.MainBackgroundBrush

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TransactionListScreen(
    viewModel: TransactionViewModel = hiltViewModel(),
    onCreateTransactionClick: () -> Unit = {},
    onTransactionClick: (String) -> Unit = {},
    onOpenListBudget: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshing,
        onRefresh = viewModel::refreshData
    )

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
            .padding(horizontal = 16.dp)
    ) {
        TransactionTopBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            title = uiState.timeRangeTitle,
            onBack = viewModel::navigateToPreviousWeek,
            onNext = viewModel::navigateToNextWeek,
            onTitleClick = viewModel::onShowDatePicker
        )

        if (uiState.isBudgetLoading) {
            BudgetFilterRowShimmer()
        } else {
            BudgetFilterRow(
                budgets = uiState.budgets,
                selectedBudget = uiState.selectedBudget,
                onBudgetSelected = viewModel::setBudgetFilter,
                onSettingClick = onOpenListBudget
            )
        }

        Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
            LazyColumn(
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.isTransactionLoading or uiState.isRefreshing) {
                    items(1) {
                        DaySectionItemShimmer()
                    }
                } else if (uiState.groupedTransactions.isEmpty()) {
                    item {
                        Text(
                            text = "No more transactions found in this period.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp),
                            style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    items(items = uiState.groupedTransactions, key = { it.date }) { daySection ->
                        DaySectionItem(
                            daySection = daySection,
                            currency = uiState.currency,
                            onCreateNewClick = onCreateTransactionClick,
                            onTransactionClick = onTransactionClick
                        )
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = uiState.isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}
