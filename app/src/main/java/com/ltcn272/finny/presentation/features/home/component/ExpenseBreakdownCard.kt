package com.ltcn272.finny.presentation.features.home.component

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ltcn272.finny.R
import com.ltcn272.finny.presentation.common.ui.PagerIndicator
import com.ltcn272.finny.presentation.common.util.CurrencyUtils
import com.netguru.multiplatform.charts.ChartAnimation
import com.netguru.multiplatform.charts.bubble.Bubble
import com.netguru.multiplatform.charts.bubble.BubbleChart
import com.netguru.multiplatform.charts.pie.PieChart
import com.netguru.multiplatform.charts.pie.PieChartConfig
import com.netguru.multiplatform.charts.pie.PieChartData
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.GridProperties.AxisProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.PopupProperties


@Composable
fun ExpenseBreakdownCard(
    categoryBubbleData: List<Bubble>,
    budgetPieData: List<PieChartData>,
    // New trend parameters
    trendTitle: String?,
    trendLabels: List<String>,
    trendLines: List<Line>,
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val pageTitles = listOf(stringResource(id = R.string.by_budget), stringResource(id = R.string.by_category), trendTitle ?: stringResource(id = R.string.placeholder))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = stringResource(id = R.string.expense_breakdown, pageTitles[pagerState.currentPage]),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                when (it) {
                    0 -> BudgetPieChartPage(data = budgetPieData)
                    1 -> CategoryBubbleChartPage(data = categoryBubbleData)
                    2 -> TrendLineChartPage(title = trendTitle, labels = trendLabels, lines = trendLines)
                }
            }

            PagerIndicator(
                pagerState = pagerState,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 16.dp)
            )
        }
    }
}

@Composable
private fun CategoryBubbleChartPage(data: List<Bubble>) {
    if (data.isEmpty()) {
        EmptyState()
        return
    }
    key(data) {
        BubbleChart(
            bubbles = data,
            modifier = Modifier.fillMaxSize(),
            animation = ChartAnimation.Sequenced { tween(1500) },
            bubbleLabel = { bubble ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = bubble.icon,
                        contentDescription = bubble.name,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = CurrencyUtils.formatCurrencyShort(bubble.value.toDouble()),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                    )
                    Text(
                        text = bubble.name,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        )
    }
}

@Composable
private fun BudgetPieChartPage(data: List<PieChartData>) {
    if (data.isEmpty()) {
        EmptyState()
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        key(data) {
            PieChart(
                data = data,
                modifier = Modifier.size(150.dp),
                config = PieChartConfig(
                    thickness = 35.dp
                ),
                animation = ChartAnimation.Simple { tween(1500) }
            )
        }
        Spacer(modifier = Modifier.width(24.dp))
        Legend(pieData = data, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PlaceholderPage() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(id = R.string.coming_soon), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(id = R.string.no_expense_data), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
    }
}

@Composable
private fun Legend(pieData: List<PieChartData>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(150.dp)
            .verticalScroll(rememberScrollState())
    ) {
        val totalExpense = pieData.sumOf { it.value }
        pieData.forEach { pie ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(pie.color, shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = pie.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                val percentage = if (totalExpense > 0) (pie.value / totalExpense * 100) else 0.0
                Text(
                    text = "${String.format(java.util.Locale.getDefault(), "%.0f", percentage)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun TrendLineChartPage(title: String?, labels: List<String>, lines: List<Line>) {
    if (title == null || lines.isEmpty() || labels.isEmpty()) {
        PlaceholderPage()
        return
    }
    Column(Modifier.fillMaxSize()) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        LineChart(
            data = lines,
            modifier = Modifier.fillMaxSize(),
            indicatorProperties = HorizontalIndicatorProperties(
                contentBuilder = { value ->
                    CurrencyUtils.formatCurrencyShort(
                        value
                    )
                }
            ),
            popupProperties = PopupProperties(
                containerColor = Color.Magenta,
                contentBuilder = { value ->
                    CurrencyUtils.formatCurrencyShort(value.value)
                }),
            gridProperties = GridProperties(
                true,
                AxisProperties(lineCount = 2),
                AxisProperties(lineCount = 2)
            )
        )
    }
}
