package com.ltcn272.finny.presentation.common.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

data class LineChartData(
    val label: String,
    val value: Float
)

@Composable
fun SmoothLineChart(
    data: List<LineChartData>,
    lineColor: Color = Color(0xFF4CAF50),
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val labelTextSize = with(density) { 11.sp.toPx() }

    Box(modifier = modifier.aspectRatio(1.8f)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val labelSpacingY = size.width * 0.10f
            val labelSpacingX = size.height * 0.10f

            val chartWidth = size.width - labelSpacingY
            val chartHeight = size.height - labelSpacingX
            val spacePerPoint = chartWidth / (data.size - 1)

            val maxValue = data.maxOfOrNull { it.value } ?: 0f
            val yAxisStepCount = 4

            val rawStep = maxValue / yAxisStepCount
            val magnitude = 10.0.pow(floor(log10(rawStep.toDouble()))).toFloat()
            val step = ceil((rawStep / magnitude).toDouble()).toFloat() * magnitude
            val upperValue = step * yAxisStepCount

            (0..yAxisStepCount).forEach { i ->
                val y = chartHeight - (i * (chartHeight / yAxisStepCount))
                val valueAtStep = i * step

                drawLine(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    start = Offset(0f, y),
                    end = Offset(chartWidth, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                drawContext.canvas.nativeCanvas.drawText(
                    formatCurrencyShort(valueAtStep),
                    chartWidth + 12f,
                    y + (labelTextSize / 3f),
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = labelTextSize
                        textAlign = android.graphics.Paint.Align.LEFT
                        isAntiAlias = true
                    }
                )
            }

            data.forEachIndexed { index, _ ->
                val x = index * spacePerPoint
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    start = Offset(x, 0f),
                    end = Offset(x, chartHeight),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }

            val points = data.mapIndexed { index, entry ->
                val x = index * spacePerPoint
                val ratio = if (upperValue > 0) entry.value / upperValue else 0f
                val y = chartHeight - (ratio * chartHeight)
                Offset(x, y)
            }

            val path = Path().apply {
                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    if (i == 0) moveTo(p0.x, p0.y)
                    cubicTo(
                        (p0.x + p1.x) / 2f, p0.y,
                        (p0.x + p1.x) / 2f, p1.y,
                        p1.x, p1.y
                    )
                }
            }

            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )

            points.forEachIndexed { index, offset ->
                drawCircle(color = lineColor, radius = 3.5.dp.toPx(), center = offset)

                drawContext.canvas.nativeCanvas.drawText(
                    data[index].label,
                    offset.x,
                    chartHeight + labelSpacingX * 0.8f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = labelTextSize
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }
                )
            }
        }
    }
}

fun formatCurrencyShort(value: Float): String {
    return when {
        value >= 1_000_000_000f -> "${(value / 1_000_000_000f).toInt()}B"
        value >= 1_000_000f -> "${(value / 1_000_000f).toInt()}M"
        value >= 1_000f -> "${(value / 1_000f).toInt()}k"
        else -> value.toInt().toString()
    }
}
@Preview
@Composable
fun ReportScreen() {
    val lineData = listOf(
        LineChartData("Mon", 1_250_000f),
        LineChartData("Tue", 5_890_000f),
        LineChartData("Wed", 12_400_000f),
        LineChartData("Thu", 8_150_000f),
        LineChartData("Fri", 15_900_000f),
        LineChartData("Sat", 4_200_000f),
        LineChartData("Sun", 7_500_000f)
    )

    Column {

        Text("Income", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
        SmoothLineChart(data = lineData, lineColor = Color(0xFF4CAF50))

        Spacer(Modifier.height(16.dp))

        Text("Outcome", color = Color(0xFFF44336), fontWeight = FontWeight.Bold)
        SmoothLineChart(data = lineData, lineColor = Color(0xFFF44336))
    }
}