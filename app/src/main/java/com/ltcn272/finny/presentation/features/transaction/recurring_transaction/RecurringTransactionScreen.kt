package com.ltcn272.finny.presentation.features.transaction.recurring_transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.RecurringTransaction
import com.ltcn272.finny.presentation.common.ui.CircleNavigationButton
import com.ltcn272.finny.presentation.common.ui.CreateNewButton
import com.ltcn272.finny.presentation.features.transaction.recurring_transaction.component.RecurringTransactionItem
import com.ltcn272.finny.presentation.features.transaction.recurring_transaction.component.RecurringTransactionItemShimmer
import com.ltcn272.finny.presentation.theme.TransactionBackgroundBrush


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RecurringTransactionScreen(
    viewModel: RecurringTransactionViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onTransactionClick: (RecurringTransaction) -> Unit,
    onCreateNew: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val lazyTransactions = viewModel.recurringTransactionsPagingFlow.collectAsLazyPagingItems()

    val isInitialLoading = lazyTransactions.loadState.refresh is LoadState.Loading && lazyTransactions.itemCount == 0

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
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TransactionBackgroundBrush)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        RecurringTransactionTopBar(
            onBack = onBack,
            title = stringResource(id = R.string.recurring_transactions_title)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    CreateNewButton(
                        onClick = onCreateNew
                    )
                }

                if (isInitialLoading) {
                    items(5) {
                        RecurringTransactionItemShimmer()
                    }
                }

                if (!isInitialLoading) {
                    if (lazyTransactions.itemCount == 0) {
                        item {
                            Text(
                                text = stringResource(R.string.no_recurring_transactions_found),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 50.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(
                            count = lazyTransactions.itemCount,
                            key = { index -> lazyTransactions.peek(index)?.serverId ?: index }
                        ) { index ->
                            val transaction = lazyTransactions[index]
                            transaction?.let {
                                RecurringTransactionItem(
                                    transaction = it,
                                    currencyCode = uiState.currencyCode,
                                    onClick = { onTransactionClick(it) }
                                )
                            }
                        }
                    }
                }

                if (lazyTransactions.loadState.append is LoadState.Loading) {
                    item { RecurringTransactionItemShimmer() }
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

@Composable
private fun RecurringTransactionTopBar(
    onBack: () -> Unit,
    title: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        CircleNavigationButton(
            icon = Icons.AutoMirrored.Default.KeyboardArrowLeft,
            onClick = onBack
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
