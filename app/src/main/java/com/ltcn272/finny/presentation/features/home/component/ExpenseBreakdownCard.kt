package com.ltcn272.finny.presentation.features.home.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ltcn272.finny.presentation.common.ui.PagerIndicator
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.Pie

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpenseBreakdownCard(
    categoryPieData: List<Pie>,
    budgetPieData: List<Pie>,
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val pageTitles = listOf("By Category", "By Budget", "Future")

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF2A2A37),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Expense Breakdown: ${pageTitles[pagerState.currentPage]}",
                color = Color.White,
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
                    0 -> CategoryPieChartPage(initialData = categoryPieData)
                    1 -> BudgetPieChartPage(initialData = budgetPieData)
                    2 -> PlaceholderPage()
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
private fun CategoryPieChartPage(initialData: List<Pie>) {
    // Use `remember(initialData)` to reset the state when the initial data changes.
    var data by remember(initialData) { mutableStateOf(initialData) }

    if (data.isEmpty()) {
        EmptyState()
        return
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RotatedPieChart(modifier = Modifier.size(150.dp)) {
            PieChart(
                data = data,
                style = Pie.Style.Fill, // Corrected Style
                spaceDegree = 7f,
                selectedPaddingDegree = 4f,
                onPieClick = {
                    val pieIndex = data.indexOf(it)
                    data =
                        data.mapIndexed { mapIndex, pie -> pie.copy(selected = pieIndex == mapIndex) }
                },
                selectedScale = 1.1f,
                scaleAnimEnterSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
            )
        }
        Spacer(modifier = Modifier.width(24.dp))
        Legend(pieData = data, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun BudgetPieChartPage(initialData: List<Pie>) {
    // Use `remember(initialData)` to reset the state when the initial data changes.
    var data by remember(initialData) { mutableStateOf(initialData) }

    if (data.isEmpty()) {
        EmptyState()
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RotatedPieChart(modifier = Modifier.size(150.dp)) {
            PieChart(
                data = data,
                style = Pie.Style.Stroke(width = 35.dp), // Corrected Style
                onPieClick = {
                    val pieIndex = data.indexOf(it)
                    data =
                        data.mapIndexed { mapIndex, pie -> pie.copy(selected = pieIndex == mapIndex) }
                },
                
                selectedScale = 1.1f,
                scaleAnimEnterSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        Spacer(modifier = Modifier.width(24.dp))
        Legend(pieData = data, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun RotatedPieChart(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier.rotate(-90f)) {
        content()
    }
}

@Composable
private fun PlaceholderPage() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Coming soon!", color = Color.Gray)
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No expense data for this period.", color = Color.Gray)
    }
}

@Composable
private fun Legend(pieData: List<Pie>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(150.dp)
            .verticalScroll(rememberScrollState())
    ) {
        val totalExpense = remember(pieData) { pieData.sumOf { it.data } }
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
                    text = pie.label ?: "",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                val percentage = if (totalExpense > 0) (pie.data / totalExpense * 100) else 0.0
                Text(
                    text = "${String.format("%.0f", percentage)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.LightGray
                )
            }
        }
    }
}