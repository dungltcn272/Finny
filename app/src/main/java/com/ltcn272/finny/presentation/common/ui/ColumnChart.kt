package com.ltcn272.finny.presentation.common.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.R
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.random.Random

data class ColumnChartEntry(
    val date: String,
    val label: String,
    val income: Float,
    val outcome: Float
)

enum class ColumnChartMode {
    WEEK, MONTH
}

private const val Y_AXIS_LABEL_COUNT = 4
private val BAR_WIDTH = 12.dp
private val BAR_SPACING = 6.dp
private val CHART_HEIGHT = 180.dp

@Composable
fun ColumnChart(
    entries: List<ColumnChartEntry>,
    mode: ColumnChartMode,
    incomeColor: Color,
    outcomeColor: Color,
    modifier: Modifier = Modifier
) {
    if (entries.isEmpty()) {
        Box(
            modifier = modifier
                .height(CHART_HEIGHT)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text("Không có dữ liệu để hiển thị")
        }
        return
    }

    val maxValue = remember(entries) {
        val maxEntryValue = entries.maxOfOrNull { maxOf(it.income, it.outcome) } ?: 0f
        (ceil(maxEntryValue / 1_000_000f) * 1_000_000f).coerceAtLeast(1_000_000f)
    }

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(entries) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 1000, easing = LinearEasing))
    }

    Row(modifier = modifier.height(CHART_HEIGHT)) {
        YAxisLabels(maxValue = maxValue, modifier = Modifier.padding(end = 8.dp))

        Box(modifier = Modifier.fillMaxSize()) {
            YAxisGrid(modifier = Modifier.fillMaxSize())

            if (mode == ColumnChartMode.WEEK) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.Bottom
                ) {
                    entries.forEach { entry ->
                        ChartColumnGroup(
                            modifier = Modifier.weight(1f),
                            entry = entry,
                            maxValue = maxValue,
                            incomeColor = incomeColor,
                            outcomeColor = outcomeColor,
                            animationProgress = animationProgress.value
                        )
                    }
                }
            } else {
                val groupSpacing = 16.dp
                LazyRow(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = groupSpacing / 2)
                ) {
                    items(entries, key = { it.date }) { entry ->
                        ChartColumnGroup(
                            entry = entry,
                            maxValue = maxValue,
                            incomeColor = incomeColor,
                            outcomeColor = outcomeColor,
                            animationProgress = animationProgress.value,
                            modifier = Modifier.padding(horizontal = groupSpacing / 2)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun YAxisLabels(maxValue: Float, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.End
    ) {
        for (i in Y_AXIS_LABEL_COUNT downTo 0) {
            val value = maxValue / Y_AXIS_LABEL_COUNT * i
            Text(
                text = formatAmountShort(value.toDouble()),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun YAxisGrid(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        for (i in 1..Y_AXIS_LABEL_COUNT) {
            val y = size.height * (1f - i.toFloat() / Y_AXIS_LABEL_COUNT)
            drawLine(
                color = Color.LightGray.copy(alpha = 0.3f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = pathEffect
            )
        }
    }
}


@Composable
private fun ChartColumnGroup(
    entry: ColumnChartEntry,
    maxValue: Float,
    incomeColor: Color,
    outcomeColor: Color,
    animationProgress: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(IntrinsicSize.Min),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(BAR_SPACING, Alignment.CenterHorizontally)
        ) {
            ChartBar(
                value = entry.income,
                maxValue = maxValue,
                color = incomeColor,
                animationProgress = animationProgress
            )
            ChartBar(
                value = entry.outcome,
                maxValue = maxValue,
                color = outcomeColor,
                animationProgress = animationProgress
            )
        }
        Text(
            text = entry.label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ChartBar(
    value: Float,
    maxValue: Float,
    color: Color,
    animationProgress: Float,
    modifier: Modifier = Modifier
) {
    val barHeightFraction = if (maxValue > 0) value / maxValue else 0f
    Box(
        modifier = modifier
            .width(BAR_WIDTH)
            .fillMaxHeight(fraction = barHeightFraction * animationProgress)
            .background(
                brush = Brush.verticalGradient(listOf(color.copy(alpha = 0.7f), color)),
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
            )
    )
}

@Composable
private fun formatAmountShort(value: Double): String {
    val absValue = abs(value)
    if (absValue < 1000) return value.toLong().toString()

    val formatter = remember { DecimalFormat("#.#") }

    val billionSuffix = stringResource(id = R.string.amount_billion_short)
    val millionSuffix = stringResource(id = R.string.amount_million_short)
    val thousandSuffix = stringResource(id = R.string.amount_thousand_short)

    return when {
        absValue >= 1_000_000_000 -> {
            val billions = value / 1_000_000_000.0
            formatter.maximumFractionDigits = if (billions % 1.0 == 0.0) 0 else 1
            "${formatter.format(billions)}${billionSuffix}"
        }
        absValue >= 1_000_000 -> {
            val millions = value / 1_000_000.0
            formatter.maximumFractionDigits = if (millions % 1.0 == 0.0) 0 else 1
            "${formatter.format(millions)}${millionSuffix}"
        }
        absValue >= 1_000 -> {
            val thousands = value / 1_000.0
            formatter.maximumFractionDigits = if (thousands % 1.0 == 0.0) 0 else 1
            "${formatter.format(thousands)}${thousandSuffix}"
        }
        else -> value.toLong().toString()
    }
}

@Composable
fun ColumnChartWaiting(
    modifier: Modifier = Modifier,
    visibleItemsCount: Int = 7,
    incomeColor: Color = Color(0xFF20C997),
    outcomeColor: Color = Color(0xFFFA5A7D)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waiting_chart_transition")

    Row(modifier = modifier.height(CHART_HEIGHT)) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(end = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            repeat(Y_AXIS_LABEL_COUNT + 1) {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(10.dp)
                        .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                )
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                for (i in 1..Y_AXIS_LABEL_COUNT) {
                    val y = size.height - (size.height / Y_AXIS_LABEL_COUNT * i)
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = pathEffect
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Bottom
            ) {
                repeat(visibleItemsCount) { index ->
                    val duration1 = remember { Random.nextInt(800, 1200) }
                    val delay1 = remember { Random.nextInt(0, 200) }
                    val targetValue1 = remember { Random.nextDouble(0.4, 0.8).toFloat() }

                    val heightFraction by infiniteTransition.animateFloat(
                        initialValue = 0.1f,
                        targetValue = targetValue1,
                        animationSpec = infiniteRepeatable(
                            animation = tween(duration1, easing = LinearEasing, delayMillis = delay1 * index),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "waiting_bar_height_$index"
                    )

                    val duration2 = remember { Random.nextInt(800, 1200) }
                    val delay2 = remember { Random.nextInt(0, 200) }
                    val targetValue2 = remember { Random.nextDouble(0.3, 0.7).toFloat() }

                    val secondaryHeightFraction by infiniteTransition.animateFloat(
                        initialValue = 0.2f,
                        targetValue = targetValue2,
                        animationSpec = infiniteRepeatable(
                            animation = tween(duration2, easing = LinearEasing, delayMillis = delay2 * index + 100),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "waiting_bar_height_secondary_$index"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(BAR_SPACING, Alignment.CenterHorizontally)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(BAR_WIDTH)
                                    .fillMaxHeight(fraction = heightFraction)
                                    .background(
                                        incomeColor.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .width(BAR_WIDTH)
                                    .fillMaxHeight(fraction = secondaryHeightFraction)
                                    .background(
                                        outcomeColor.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(12.dp)
                                .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun ColumnChartWeekPreview() {
    val chartData = listOf(
        ColumnChartEntry(date = "2026-01-01", label = "T2", income = 1150000f, outcome = 850000f),
        ColumnChartEntry(date = "2026-01-02", label = "T3", income = 900000f, outcome = 1050000f),
        ColumnChartEntry(date = "2026-01-03", label = "T4", income = 1500000f, outcome = 700000f),
        ColumnChartEntry(date = "2026-01-04", label = "T5", income = 1000000f, outcome = 950000f),
        ColumnChartEntry(date = "2026-01-05", label = "T6", income = 1800000f, outcome = 1250000f),
        ColumnChartEntry(date = "2026-01-06", label = "T7", income = 2200000f, outcome = 1750000f),
        ColumnChartEntry(date = "2026-01-07", label = "CN", income = 750000f, outcome = 900000f),
    )

    MaterialTheme {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Thu chi theo ngày",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                ColumnChart(
                    entries = chartData,
                    mode = ColumnChartMode.WEEK,
                    incomeColor = Color(0xFF20C997),
                    outcomeColor = Color(0xFFFA5A7D)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
fun ColumnChartWaitingPreview() {
    MaterialTheme {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Đang tải dữ liệu...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                ColumnChartWaiting(visibleItemsCount = 7)
            }
        }
    }
}

