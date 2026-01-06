package com.ltcn272.finny.presentation.features.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.domain.model.Transaction
import com.ltcn272.finny.domain.model.TransactionType
import com.ltcn272.finny.presentation.common.ui.BudgetItem
import com.ltcn272.finny.presentation.common.ui.BudgetItemShimmer
import com.ltcn272.finny.presentation.common.ui.SliderFilterRow
import com.ltcn272.finny.presentation.common.ui.TransactionItem
import com.ltcn272.finny.presentation.common.ui.TransactionItemShimmer
import com.ltcn272.finny.presentation.common.util.NetworkStatus
import com.ltcn272.finny.presentation.features.home.component.BudgetDistributionCard
import com.ltcn272.finny.presentation.features.home.component.BudgetDistributionCardShimmer
import com.ltcn272.finny.presentation.features.home.component.FeaturedBudgetCard
import com.ltcn272.finny.presentation.features.home.component.FeaturedBudgetCardShimmer
import com.ltcn272.finny.presentation.features.home.component.FeaturedBudgetEmptyStateCard
import com.ltcn272.finny.presentation.features.home.component.HomeHeader
import com.ltcn272.finny.presentation.features.home.component.ListHeader
import com.ltcn272.finny.presentation.features.snackbar.LocalSnackbarManager
import com.ltcn272.finny.presentation.features.snackbar.TopSnackbarType
import com.ltcn272.finny.presentation.theme.MainBackgroundBrush
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onNotificationClick: () -> Unit,
    onSeeAllBudgets: () -> Unit = {},
    onSeeAllTransactions: () -> Unit = {},
    onBankNotificationClick: () -> Unit = {},
    onCreateBudget: () -> Unit = {},
    onBudgetClick: (Budget) -> Unit = {},
    onTransactionClick: (Transaction) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarManager = LocalSnackbarManager.current

    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshingByUser,
        onRefresh = viewModel::refreshDataFromPull
    )

    LaunchedEffect(key1 = true) {
        viewModel.errorEvent.collectLatest { message ->
            snackbarManager.showMessage(message, TopSnackbarType.ERROR)
        }
    }

    val incomeString = stringResource(id = R.string.incoming)
    val outgoingString = stringResource(id = R.string.outcoming)
    val currencyCode = uiState.currency

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MainBackgroundBrush)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            NetworkStatusIndicator(networkStatus = uiState.networkStatus)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .pullRefresh(pullRefreshState)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 12.dp,
                        bottom = 160.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        HomeHeader(
                            username = uiState.username ?: "...",
                            currentDate = uiState.currentDate,
                            onNotificationClick = onNotificationClick,
                            bankNotificationCount = uiState.bankNotificationCount,
                            unreadNotificationCount = uiState.unreadNotificationCount,
                            onBankNotificationClick = onBankNotificationClick
                        )
                    }

                    if (uiState.isLoading) {
                        item { FeaturedBudgetCardShimmer() }
                        item { BudgetDistributionCardShimmer() }
                        item {
                            ListHeader(
                                title = stringResource(R.string.your_budgets),
                                onSeeAllClick = {})
                        }
                        items(2) { BudgetItemShimmer() }
                        item {
                            ListHeader(
                                title = stringResource(R.string.latest_transactions),
                                onSeeAllClick = {})
                        }
                        items(3) { TransactionItemShimmer() }
                    } else {
                        if (uiState.showCreateBudgetPrompt) {
                            item {
                                FeaturedBudgetEmptyStateCard(onCreateBudgetClick = onCreateBudget)
                            }
                        } else if (uiState.featuredBudget != null) {
                            item {
                                FeaturedBudgetCard(
                                    budget = uiState.featuredBudget!!,
                                    onCardClick = { onBudgetClick(uiState.featuredBudget!!) }
                                )
                            }
                        }

                        if (uiState.donutChartData.isNotEmpty()) {
                            item {
                                BudgetDistributionCard(
                                    reportData = uiState.donutChartData,
                                    totalAmount = uiState.donutChartTotalAmount
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

                        val transactions = uiState.allRecentTransactions
                        if (transactions != null) {
                            if (transactions.isNotEmpty()) {
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
                                        unselectedBg = MaterialTheme.colorScheme.surfaceVariant.copy(
                                            alpha = 0.5f
                                        ),
                                        selectedText = MaterialTheme.colorScheme.onPrimary,
                                        unselectedText = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (uiState.transactionsToShow.isNotEmpty()) {
                                    items(uiState.transactionsToShow, key = { it.serverId }) { transaction ->
                                        TransactionItem(
                                            transaction = transaction,
                                            currencyCode = currencyCode,
                                            onClick = { onTransactionClick(transaction) }
                                        )
                                    }
                                } else {
                                    item {
                                        Text(
                                            text = stringResource(R.string.no_transactions_for_filter),
                                            modifier = Modifier.padding(vertical = 20.dp)
                                        )
                                    }
                                }
                            } else if (!uiState.showCreateBudgetPrompt) {
                                item {
                                    Text(
                                        text = stringResource(R.string.no_transactions_yet),
                                        modifier = Modifier.padding(vertical = 20.dp)
                                    )
                                }
                            }
                        }
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
}

@Composable
private fun NetworkStatusIndicator(networkStatus: NetworkStatus) {
    val isOffline = networkStatus != NetworkStatus.Available
    AnimatedVisibility(
        visible = isOffline,
        enter = expandVertically(animationSpec = tween(300)),
        exit = shrinkVertically(animationSpec = tween(300))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.DarkGray)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = stringResource(R.string.offline_mode),
                color = Color.White,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
