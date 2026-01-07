package com.ltcn272.finny.presentation.features.dashboard.overview.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.OverviewBudgetSummary
import com.ltcn272.finny.domain.model.OverviewCategorySummary
import com.ltcn272.finny.presentation.common.util.generateHarmonicColors

private enum class SummaryTab {
    CATEGORY, BUDGET
}

@Composable
fun CategoryBudgetSummaryCard(
    modifier: Modifier = Modifier,
    categories: List<OverviewCategorySummary>,
    budgets: List<OverviewBudgetSummary>,
    totalExpense: Double,
) {
    var selectedTab by remember { mutableStateOf(SummaryTab.CATEGORY) }
    val categoryColors = remember(categories.size) { generateHarmonicColors(Color(0xFFFFA726), categories.size) }
    var isExpanded by remember { mutableStateOf(false) }

    val showSeeAllButton = when (selectedTab) {
        SummaryTab.CATEGORY -> categories.size > 4
        SummaryTab.BUDGET -> budgets.size > 4
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)) {

            OverViewTabRow(
                selectedTabIndex = selectedTab.ordinal,
                tabs = listOf(stringResource(R.string.category), stringResource(R.string.budget)),
                onTabSelected = { index ->
                    selectedTab = SummaryTab.entries[index]
                    isExpanded = false
                }
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                thickness = 1.dp
            )

            val itemsToShow = when (selectedTab) {
                SummaryTab.CATEGORY -> if (isExpanded) categories else categories.take(4)
                SummaryTab.BUDGET -> if (isExpanded) budgets else budgets.take(4)
            }
            val isListEmpty = itemsToShow.isEmpty()

            Column(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .animateContentSize(animationSpec = spring())
            ) {
                if (isListEmpty) {
                    val emptyText = if (selectedTab == SummaryTab.CATEGORY) {
                        stringResource(R.string.no_category_data)
                    } else {
                        stringResource(R.string.no_budget_data)
                    }
                    Text(
                        text = emptyText,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                } else {
                    when (selectedTab) {
                        SummaryTab.CATEGORY -> {
                            itemsToShow.forEachIndexed { index, item ->
                                if (item is OverviewCategorySummary) {
                                    val percentage = if (totalExpense > 0) (item.outcome / totalExpense).toFloat() else 0f
                                    CategorySummaryItem(
                                        name = item.name,
                                        amount = item.outcome,
                                        percentage = percentage,
                                        progressColor = categoryColors.getOrElse(index) { Color.Gray }
                                    )
                                }
                            }
                        }
                        SummaryTab.BUDGET -> {
                            itemsToShow.forEach { item ->
                                if (item is OverviewBudgetSummary) {
                                    BudgetSummaryItem(
                                        name = item.name,
                                        spent = item.spent,
                                        limit = item.limit,
                                        usagePercent = item.usagePercent
                                    )
                                }
                            }
                        }
                    }
                }
            }


            if (showSeeAllButton) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isExpanded) stringResource(R.string.show_less) else stringResource(R.string.see_all),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.padding(horizontal = 2.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) stringResource(R.string.show_less) else stringResource(R.string.see_all),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryBudgetSummaryCardShimmer(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)) {
            OverViewTabRow(
                selectedTabIndex = 0,
                tabs = listOf(stringResource(R.string.category), stringResource(R.string.budget)),
                onTabSelected = {}
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                thickness = 1.dp
            )

            Column(modifier = Modifier.padding(top = 12.dp)) {
                repeat(3) {
                    CategorySummaryItemShimmer()
                }
            }
        }
    }
}
