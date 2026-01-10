package com.ltcn272.finny.presentation.common.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.snapTo
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
import kotlin.math.log10
import kotlin.math.pow
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
        if (maxEntryValue <= 0f) return@remember 1000f

        val exponent = log10(maxEntryValue.toDouble()).toInt()
        val magnitude = 10.0.pow(exponent.toDouble()).toFloat()
        val firstDigit = maxEntryValue / magnitude

        val roundedFactor = when {
            firstDigit <= 1.2f -> 1.6f
            firstDigit <= 2.0f -> 2.4f
            firstDigit <= 3.0f -> 4.0f
            firstDigit <= 4.5f -> 6.0f
            firstDigit <= 6.0f -> 8.0f
            else -> 10.0f
        }

        var result = roundedFactor * magnitude
        if (result < maxEntryValue) result = magnitude * 10f
        result
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
            val value = (maxValue / Y_AXIS_LABEL_COUNT) * i
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
        for (i in 0..Y_AXIS_LABEL_COUNT) {
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
                brush = Brush.verticalGradient(
                    listOf(
                        color,
                        color.copy(alpha = 0.7f)
                    )
                ),
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
            )
    )
}

@Composable
private fun formatAmountShort(value: Double): String {
    val absValue = abs(value)
    if (absValue == 0.0) return "0"
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
                for (i in 0..Y_AXIS_LABEL_COUNT) {
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

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Bottom
            ) {
                repeat(visibleItemsCount) { index ->
                    val duration1 = remember { Random.nextInt(600, 1000) }
                    val delay1 = remember { Random.nextInt(0, 150) }
                    val targetValue1 = remember { Random.nextDouble(0.5, 0.9).toFloat() }

                    val heightFraction by infiniteTransition.animateFloat(
                        initialValue = 0.1f,
                        targetValue = targetValue1,
                        animationSpec = infiniteRepeatable(
                            animation = tween(duration1, easing = LinearEasing, delayMillis = delay1),
                            repeatMode = RepeatMode.Reverse
                        ), label = "shimmer_bar_1_$index"
                    )

                    val duration2 = remember { Random.nextInt(600, 1000) }
                    val delay2 = remember { Random.nextInt(0, 150) }
                    val targetValue2 = remember { Random.nextDouble(0.5, 0.9).toFloat() }

                    val heightFraction2 by infiniteTransition.animateFloat(
                        initialValue = 0.1f,
                        targetValue = targetValue2,
                        animationSpec = infiniteRepeatable(
                            animation = tween(duration2, easing = LinearEasing, delayMillis = delay2),
                            repeatMode = RepeatMode.Reverse
                        ), label = "shimmer_bar_2_$index"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(BAR_SPACING, Alignment.CenterHorizontally)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(BAR_WIDTH)
                                    .fillMaxHeight(fraction = heightFraction)
                                    .background(
                                        incomeColor.copy(alpha = 0.5f),
                                        RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .width(BAR_WIDTH)
                                    .fillMaxHeight(fraction = heightFraction2)
                                    .background(
                                        outcomeColor.copy(alpha = 0.5f),
                                        RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ColumnChartPreview() {
    val sampleEntries = remember {
        listOf(
            ColumnChartEntry("01", "T2", 86000f, 12000f),
            ColumnChartEntry("02", "T3", 35000f, 22000f),
            ColumnChartEntry("03", "T4", 15000f, 8000f),
            ColumnChartEntry("04", "T5", 45000f, 32000f),
            ColumnChartEntry("05", "T6", 20000f, 15000f),
            ColumnChartEntry("06", "T7", 55000f, 42000f),
            ColumnChartEntry("07", "CN", 8000f, 5000f)
        )
    }

    MaterialTheme {
        Card(
            modifier = Modifier.padding(16.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Báo cáo Thu-Chi Tuần",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))
                ColumnChart(
                    entries = sampleEntries,
                    mode = ColumnChartMode.WEEK,
                    incomeColor = Color(0xFF20C997),
                    outcomeColor = Color(0xFFFA5A7D),
                )
            }
        }
    }
}
