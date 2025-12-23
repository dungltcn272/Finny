package com.ltcn272.finny.presentation.features.budget.budget_detail.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.R
import com.ltcn272.finny.presentation.common.ui.LineChartData
import com.ltcn272.finny.presentation.common.ui.SmoothLineChart

@Composable
internal fun ChartCard(
    title: String,
    data: List<LineChartData>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = lineColor
            )
            Spacer(Modifier.height(8.dp))
            if (data.isNotEmpty()) {
                SmoothLineChart(data = data, lineColor = lineColor)
            } else {
                Text(
                    text = stringResource(R.string.no_data_available),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
        }
    }
}
