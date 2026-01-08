package com.ltcn272.finny.presentation.features.budget.budget_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.domain.model.Transaction
import com.ltcn272.finny.presentation.common.ui.AnimatedMoreMenu
import com.ltcn272.finny.presentation.common.ui.CircleNavigationButton
import com.ltcn272.finny.presentation.common.ui.DeleteConfirmationDialog
import com.ltcn272.finny.presentation.common.ui.TransactionItem
import com.ltcn272.finny.presentation.common.ui.TransactionItemShimmer
import com.ltcn272.finny.presentation.features.budget.budget_detail.component.BudgetInfoCard
import com.ltcn272.finny.presentation.features.budget.budget_detail.component.BudgetInfoCardShimmer
import com.ltcn272.finny.presentation.features.budget.budget_detail.component.IncomeOutcomeStatCard
import com.ltcn272.finny.presentation.features.budget.budget_detail.component.StatCardShimmer
import com.ltcn272.finny.presentation.features.budget.budget_detail.component.StatisticsSection
import com.ltcn272.finny.presentation.features.snackbar.LocalSnackbarManager
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
    val snackbarManager = LocalSnackbarManager.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is BudgetDetailEvent.DeleteSuccess -> {
                    onBack()
                }
            }
        }
    }

    LaunchedEffect(budget) {
        viewModel.initialize(budget)
    }

    if (uiState.showDeleteConfirmDialog) {
        DeleteConfirmationDialog(
            title = stringResource(R.string.delete_budget_confirmation_title),
            text = stringResource(
                R.string.delete_budget_confirmation_text,
                uiState.budget?.name ?: ""
            ),
            onConfirm = { viewModel.confirmDeleteBudget(snackbarManager) },
            onDismiss = viewModel::cancelDelete
        )
    }

    val budgetToShow = uiState.budget ?: budget
    val isOverallLoading = uiState.isStatisticsLoading && uiState.transactions.isEmpty()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshingByUser,
        onRefresh = { viewModel.onUserPullToRefresh() })

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
            CircleNavigationButton(icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft, onClick = onBack)
            Box {
                CircleNavigationButton(
                    icon = Icons.Default.MoreHoriz,
                    onClick = { menuExpanded = true })
                AnimatedMoreMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    onEditClick = {
                        menuExpanded = false; onEditBudget(budgetToShow)
                    },
                    onDeleteClick = {
                        menuExpanded = false; viewModel.requestDeleteBudget()
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
                    if (isOverallLoading) {
                        BudgetInfoCardShimmer()
                    } else {
                        BudgetInfoCard(
                            budget = budgetToShow,
                            transactionCount = uiState.transactionCount,
                            netAmount = uiState.netAmount,
                        )
                    }
                }

                item {
                    if (isOverallLoading) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            StatCardShimmer(modifier = Modifier.weight(1f))
                            StatCardShimmer(modifier = Modifier.weight(1f))
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            IncomeOutcomeStatCard(
                                modifier = Modifier.weight(1f),
                                label = stringResource(R.string.incoming),
                                amount = uiState.totalIncome,
                                currency = budget.currency,
                                isIncome = true
                            )
                            IncomeOutcomeStatCard(
                                modifier = Modifier.weight(1f),
                                label = stringResource(R.string.outcoming),
                                amount = uiState.totalOutcome,
                                currency = budget.currency,
                                isIncome = false
                            )
                        }
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

                if (isOverallLoading) {
                    items(3) { TransactionItemShimmer() }
                } else {
                    if (uiState.transactions.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.no_transactions_in_period),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                        }
                    } else {
                        items(items = uiState.transactions, key = { it.serverId }) { transaction ->
                            TransactionItem(
                                transaction = transaction,
                                currencyCode = uiState.currency,
                                onClick = { onTransactionClick(transaction) }
                            )
                        }
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
