package com.ltcn272.finny.presentation.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.ltcn272.finny.R
import com.ltcn272.finny.presentation.common.ui.CircleIconButton
import com.ltcn272.finny.presentation.features.home.component.BalanceCard
import com.ltcn272.finny.presentation.features.home.component.ExpenseBreakdownCard
import com.ltcn272.finny.presentation.theme.MainBackgroundBrush


@Composable
fun HomeScreen(
    onSettingsClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.hi_user, "Viet"),
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            CircleIconButton(
                onClick = onSettingsClick,
                icon = R.drawable.ic_settings,
                size = 36.dp,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant
            )
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