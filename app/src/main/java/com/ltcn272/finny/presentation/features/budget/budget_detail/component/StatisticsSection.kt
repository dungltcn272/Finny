package com.ltcn272.finny.presentation.features.budget.budget_detail.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.R
import com.ltcn272.finny.presentation.common.ui.LineChartData
import com.ltcn272.finny.presentation.common.ui.SegmentedControl
import com.ltcn272.finny.presentation.features.budget.budget_detail.ChartTimeRange

@Composable
internal fun StatisticsSection(
    selectedTimeRange: ChartTimeRange,
    onTimeRangeSelected: (ChartTimeRange) -> Unit,
    incomeData: List<LineChartData>,
    outcomeData: List<LineChartData>,
    modifier: Modifier = Modifier,
    isLoading: Boolean // THÊM THAM SỐ NÀY
) {
    val timeRangeOptions = remember { ChartTimeRange.entries.toList() }
    val weekString = stringResource(R.string.week)
    val monthString = stringResource(R.string.month)

    // Animation cho hiệu ứng mờ
    val alpha by animateFloatAsState(
        targetValue = if (isLoading) 0.5f else 1f,
        label = "StatisticsAlphaAnimation"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.statistics),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            SegmentedControl(
                options = timeRangeOptions,
                selected = selectedTimeRange,
                onOptionClicked = onTimeRangeSelected,
                titleForItem = {
                    when (it) {
                        ChartTimeRange.WEEK -> weekString
                        ChartTimeRange.MONTH -> monthString
                    }
                },
                modifier = Modifier.fillMaxWidth(0.5f),
                indicatorPadding = 2.dp
            )
        }

        // Áp dụng hiệu ứng mờ vào các biểu đồ
        Column(
            modifier = Modifier.alpha(alpha),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ChartCard(
                title = stringResource(R.string.incoming),
                data = incomeData,
                lineColor = Color(0xFF4CAF50)
            )
            ChartCard(
                title = stringResource(R.string.outcoming),
                data = outcomeData,
                lineColor = Color(0xFFF44336)
            )
        }
    }
}
