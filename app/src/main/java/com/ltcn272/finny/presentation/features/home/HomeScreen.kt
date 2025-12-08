package com.ltcn272.finny.presentation.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ltcn272.finny.R
import com.ltcn272.finny.presentation.common.ui.CircleIconButton
import com.ltcn272.finny.presentation.common.ui.BudgetItem
import com.ltcn272.finny.presentation.common.ui.TransactionItem
import com.ltcn272.finny.presentation.features.home.component.BalanceCard
import com.ltcn272.finny.presentation.features.home.component.ExpenseBreakdownCard
import com.ltcn272.finny.presentation.theme.MainBackgroundBrush
import com.ltcn272.finny.domain.model.TransactionType
import com.ltcn272.finny.presentation.common.ui.SliderFilterRow


@Composable
fun HomeScreen(
    onNotificationClick: () -> Unit = {},
    onSeeAllBudgets: () -> Unit = {},
    onSeeAllTransactions: () -> Unit = {},
    onBudgetClick: (String) -> Unit = {},
    onTransactionClick: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val username by viewModel.username.collectAsState()
    val topBudgets by viewModel.topBudgets.collectAsState()
    val latestTransactions by viewModel.latestTransactions.collectAsState()
    val transactionFilter by viewModel.transactionFilter.collectAsState()
    val scrollState = rememberScrollState()

    val displayName =
        username?.takeIf { it.isNotBlank() } ?: stringResource(id = R.string.user_name_placeholder)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MainBackgroundBrush)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.hi_user, displayName),
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            CircleIconButton(
                onClick = { viewModel.addFakeData() },
                icon = R.drawable.ic_add,
                size = 36.dp
            )
            CircleIconButton(
                onClick = onNotificationClick,
                icon = R.drawable.ic_notification,
                size = 36.dp
            )
        }
        if (uiState.isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Spacer(modifier = Modifier.height(24.dp))

        BalanceCard(
            balance = uiState.balance,
            income = uiState.income,
            expense = uiState.expense,
            currencyCode = uiState.currency,
            selectedTimeFilter = uiState.selectedTimeFilter,
            onCycleFilter = viewModel::cycleTimeFilter
        )

        Spacer(modifier = Modifier.height(24.dp))

        ExpenseBreakdownCard(
            categoryBubbleData = uiState.categoryBubbleData,
            budgetPieData = uiState.budgetExpenseBreakdown,
            trendTitle = uiState.trendTitle,
            trendLabels = uiState.trendLabels,
            trendLines = uiState.trendLines
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Your Budgets header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(id = R.string.your_budgets),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = stringResource(id = R.string.see_all),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.clickable { onSeeAllBudgets() }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Top 3 budgets
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            topBudgets.take(3).forEach { bd ->
                BudgetItem(budgetDetails = bd, onClick = { onBudgetClick(bd.budget.id) })
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Latest Transactions header + filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(id = R.string.latest_transactions),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = stringResource(id = R.string.see_all),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.clickable { onSeeAllTransactions() }
            )
        }

        // Filter pills using SliderFilterRow
        val filterItems = listOf(TransactionType.INCOME, TransactionType.OUTCOME)
        // Precompute labels (call stringResource in composable scope)
        val filterLabels = mapOf(
            TransactionType.INCOME to stringResource(id = R.string.income),
            TransactionType.OUTCOME to stringResource(id = R.string.outgoing)
        )

        SliderFilterRow(
            items = filterItems,
            selectedItem = transactionFilter,
            onItemSelected = { viewModel.setTransactionFilter(it) },
            itemToString = { filterLabels[it] ?: it.name },
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            latestTransactions.forEach { tx ->
                TransactionItem(
                    transaction = tx,
                    currency = uiState.currency,
                    onClick = { transaction -> onTransactionClick(transaction.id) })
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = uiState.trendTitle ?: stringResource(R.string.income_vs_expense),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (uiState.trendTitle == null) {
                    Text(
                        text = stringResource(R.string.line_chart_placeholder),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}