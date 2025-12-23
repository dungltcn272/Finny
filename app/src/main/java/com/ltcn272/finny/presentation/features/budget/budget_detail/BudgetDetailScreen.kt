package com.ltcn272.finny.presentation.features.budget.budget_detail

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.domain.model.Transaction
import com.ltcn272.finny.presentation.common.ui.AnimatedMoreMenu
import com.ltcn272.finny.presentation.common.ui.CircleNavigationButton
import com.ltcn272.finny.presentation.common.ui.TransactionItem
import com.ltcn272.finny.presentation.common.ui.TransactionItemShimmer
import com.ltcn272.finny.presentation.features.budget.budget_detail.component.BudgetInfoCard
import com.ltcn272.finny.presentation.features.budget.budget_detail.component.StatisticsSection
import com.ltcn272.finny.presentation.theme.BudgetBackgroundBrush
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BudgetDetailScreen(
    budget: Budget,
    viewModel: BudgetDetailViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onEditBudget: (Budget) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is BudgetDetailEvent.DeleteSuccess -> {
                    Toast.makeText(context, "Đã xóa ngân sách", Toast.LENGTH_SHORT).show()
                    onBack()
                }

                is BudgetDetailEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    LaunchedEffect(budget) {
        viewModel.initialize(budget)
    }

    val uiState by viewModel.uiState.collectAsState()
    val transactions = viewModel.transactionsPagingFlow.collectAsLazyPagingItems()

    val budgetToShow = uiState.budget ?: budget
    val isPagingTransactionLoading =
        transactions.loadState.refresh is LoadState.Loading && transactions.itemCount == 0

    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshingByUser,
        onRefresh = { viewModel.onUserPullToRefresh() }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BudgetBackgroundBrush)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleNavigationButton(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                onClick = onBack
            )

            Box {
                CircleNavigationButton(
                    icon = Icons.Default.MoreVert,
                    onClick = { menuExpanded = true })
                AnimatedMoreMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    onEditClick = {
                        menuExpanded = false
                        onEditBudget(budgetToShow)
                    },
                    onDeleteClick = {
                        menuExpanded = false
                        viewModel.deleteBudget()
                    },
                    offset = DpOffset(x = 0.dp, y = 38.dp)
                )
            }
        }

        Box(Modifier.pullRefresh(pullRefreshState)) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    Column {
                        AnimatedVisibility(visible = uiState.isStatisticsLoading) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                        BudgetInfoCard(
                            name = budgetToShow.name,
                            transactionCount = uiState.transactionCount,
                            netAmount = uiState.netAmount,
                            currency = uiState.currency,
                            modifier = Modifier.padding(top = if (uiState.isStatisticsLoading) 8.dp else 0.dp)
                        )
                    }
                }

                item {
                    StatisticsSection(
                        selectedTimeRange = uiState.timeRange,
                        onTimeRangeSelected = viewModel::onTimeRangeSelected,
                        incomeData = uiState.incomeChartData,
                        outcomeData = uiState.outcomeChartData,
                        isLoading = uiState.isStatisticsLoading
                    )
                }

                item {
                    Text(
                        text = stringResource(R.string.transactions_label),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isPagingTransactionLoading) {
                    items(3) { TransactionItemShimmer() }
                } else {
                    items(
                        count = transactions.itemCount,
                        key = { index -> transactions.peek(index)?.serverId ?: index }
                    ) { index ->
                        transactions[index]?.let { transaction ->
                            TransactionItem(
                                transaction = transaction,
                                currencyCode = uiState.currency,
                                onClick = { onTransactionClick(transaction) }
                            )
                        }
                    }

                    if (transactions.loadState.append is LoadState.Loading) {
                        item { TransactionItemShimmer() }
                    }
                }
            }
            PullRefreshIndicator(
                refreshing = uiState.isRefreshingByUser,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}
