package com.ltcn272.finny.presentation.features.budget.budget_list

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
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.presentation.common.ui.BudgetItem
import com.ltcn272.finny.presentation.common.ui.BudgetItemShimmer
import com.ltcn272.finny.presentation.common.ui.CircleNavigationButton
import com.ltcn272.finny.presentation.common.ui.CreateNewButton
import com.ltcn272.finny.presentation.theme.BudgetBackgroundBrush

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BudgetListScreen(
    viewModel: BudgetListViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onCreateNew: () -> Unit,
    onBudgetClick: (Budget) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val lazyBudgets = viewModel.budgetsPagingFlow.collectAsLazyPagingItems()

    val isInitialLoading =
        lazyBudgets.loadState.refresh is LoadState.Loading && lazyBudgets.itemCount == 0

    LaunchedEffect(lazyBudgets.loadState.refresh) {
        if (lazyBudgets.loadState.refresh !is LoadState.Loading) {
            viewModel.onRefreshFinished()
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshingByUser,
        onRefresh = {
            viewModel.onUserPullToRefresh()
            lazyBudgets.refresh()
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BudgetBackgroundBrush)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        BudgetListTopBar(
            onBack = onBack,
            title = stringResource(id = R.string.budgets_title)
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
                    CreateNewButton(onClick = onCreateNew)
                }

                if (isInitialLoading) {
                    items(4) {
                        BudgetItemShimmer()
                    }
                }

                if (!isInitialLoading) {
                    if (lazyBudgets.itemCount == 0) {
                        item {
                            Text(
                                text = stringResource(R.string.no_budgets_found),
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
                            count = lazyBudgets.itemCount,
                            key = { index -> lazyBudgets.peek(index)?.serverId ?: index }
                        ) { index ->
                            val budget = lazyBudgets[index]
                            budget?.let {
                                BudgetItem(
                                    budget = it,
                                    onClick = { onBudgetClick(it) }
                                )
                            }
                        }
                    }
                }

                if (lazyBudgets.loadState.append is LoadState.Loading) {
                    item {
                        BudgetItemShimmer()
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

@Composable
private fun BudgetListTopBar(
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
