package com.ltcn272.finny.presentation.features.dashboard.overview.component

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
fun FinnyTabIndicator(modifier: Modifier = Modifier, color: Color) {
    Spacer(
        modifier
            .padding(horizontal = 8.dp)
            .height(3.dp)
            .background(
                color,
                RoundedCornerShape(
                    topStartPercent = 100,
                    topEndPercent = 100
                )
            )
    )
}


@Composable
fun CategoryBudgetSummaryCard(
    modifier: Modifier = Modifier,
    categories: List<OverviewCategorySummary>,
    budgets: List<OverviewBudgetSummary>,
    totalExpense: Double,
    onSeeAllClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(SummaryTab.CATEGORY) }
    val categoryColors = remember(categories.size) { generateHarmonicColors(Color(0xFFFFA726), categories.size) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)) {
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = Color.Transparent,
                contentColor = Color.Black,
                divider = {},
                indicator = { tabPositions ->
                    if (selectedTab.ordinal < tabPositions.size) {
                        FinnyTabIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                            color = Color.Black
                        )
                    }
                }
            ) {
                Tab(
                    selected = selectedTab == SummaryTab.CATEGORY,
                    onClick = { selectedTab = SummaryTab.CATEGORY },
                    interactionSource = remember { MutableInteractionSource() },
                    text = {
                        Text(
                            stringResource(R.string.category),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (selectedTab == SummaryTab.CATEGORY) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == SummaryTab.CATEGORY) Color.Black else Color.Gray
                        )
                    }
                )
                Tab(
                    selected = selectedTab == SummaryTab.BUDGET,
                    onClick = { selectedTab = SummaryTab.BUDGET },
                    interactionSource = remember { MutableInteractionSource() },
                    text = {
                        Text(
                            stringResource(R.string.budget),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (selectedTab == SummaryTab.BUDGET) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == SummaryTab.BUDGET) Color.Black else Color.Gray
                        )
                    }
                )
            }

            Crossfade(
                targetState = selectedTab,
                label = "summary_tab_crossfade",
            ) { tab ->
                when (tab) {
                    SummaryTab.CATEGORY -> {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(vertical = 12.dp)) {
                            if (categories.isEmpty()) {
                                Text(stringResource(R.string.no_category_data), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 8.dp))
                            } else {
                                categories.take(4).forEachIndexed { index, category ->
                                    val percentage = if (totalExpense > 0) (category.outcome / totalExpense).toFloat() else 0f
                                    CategorySummaryItem(
                                        name = category.name,
                                        amount = category.outcome,
                                        percentage = percentage,
                                        progressColor = categoryColors.getOrElse(index) { Color.Gray }
                                    )
                                }
                            }
                        }
                    }
                    SummaryTab.BUDGET -> {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(vertical = 12.dp)) {
                            if (budgets.isEmpty()) {
                                Text(stringResource(R.string.no_budget_data), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 8.dp))
                            } else {
                                budgets.take(4).forEach { budget ->
                                    BudgetSummaryItem(
                                        name = budget.name,
                                        spent = budget.spent,
                                        limit = budget.limit,
                                        usagePercent = budget.usagePercent
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if ((selectedTab == SummaryTab.CATEGORY && categories.size > 4) || (selectedTab == SummaryTab.BUDGET && budgets.size > 4)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSeeAllClick() }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.see_all),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.padding(horizontal = 2.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.see_all),
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
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.category), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Spacer(modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(3.dp)
                        .background(
                            Color.Black,
                            RoundedCornerShape(
                                topStartPercent = 100,
                                topEndPercent = 100
                            )
                        )
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.budget), style = MaterialTheme.typography.bodyLarge, color = Color.Gray, fontWeight = FontWeight.Normal)
                    Spacer(Modifier.height(11.dp))
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
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
