package com.ltcn272.finny.presentation.common.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

data class DonutData(
    val label: String, val value: Float, val amountText: String, val color: Color? = null
)

object ChartTheme {
    private val palette = listOf(
        Color(0xFFFF9800),
        Color(0xFFFFD600),
        Color(0xFF2196F3),
        Color(0xFFBA68C8),
        Color(0xFFF44336),
        Color(0xFF4CAF50)
    )

    fun getColor(index: Int) = palette[index % palette.size]
}

@Composable
fun DonutChart(
    items: List<DonutData>,
    totalLabel: String,
    totalAmount: Double,
    modifier: Modifier = Modifier,
    gapDegree: Float = 2f,
    strokeWidthDp: Dp = 22.dp
) {
    if (items.isEmpty()) return

    val totalValue = items.sumOf { it.value.toDouble() }.toFloat()
    val density = LocalDensity.current
    val strokeWidthPx = with(density) { strokeWidthDp.toPx() }

    val initialTextStyle = MaterialTheme.typography.titleLarge
    var amountTextStyle by remember(initialTextStyle) { mutableStateOf(initialTextStyle) }

    val percentageFormatter = remember { DecimalFormat("#.#") }

    // --- CHART ANIMATION ---
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(items) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2000)
        )
    }

    Box(
        modifier = modifier.aspectRatio(1f), contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(35.dp)
        ) {
            val radius = size.minDimension / 2
            var startAngle = -90f

            items.forEachIndexed { index, item ->
                val sweepAngle = (item.value / totalValue) * 360f * animationProgress.value
                val color = item.color ?: ChartTheme.getColor(index)

                drawArc(
                    color = color,
                    startAngle = startAngle + (gapDegree / 2),
                    sweepAngle = sweepAngle - gapDegree,
                    useCenter = false,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt)
                )

                if (animationProgress.value > 0.8f) {
                    val middleAngle = startAngle + (sweepAngle / 2)
                    val angleRad = Math.toRadians(middleAngle.toDouble())
                    val textDistanceFromCenter = radius + strokeWidthDp.toPx()
                    val x = (center.x + textDistanceFromCenter * cos(angleRad)).toFloat()
                    val y = (center.y + textDistanceFromCenter * sin(angleRad)).toFloat()

                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            this.color = color.toArgb()
                            textSize = 34f
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                            isAntiAlias = true
                            val normalizedAngle = (middleAngle + 360) % 360
                            textAlign = if (normalizedAngle > 90 && normalizedAngle < 270) {
                                android.graphics.Paint.Align.RIGHT
                            } else {
                                android.graphics.Paint.Align.LEFT
                            }
                            alpha = ((animationProgress.value - 0.8f) / 0.2f * 255).toInt()
                        }

                        if (sweepAngle > 8f) {
                            val fontMetrics = paint.fontMetrics
                            val centeredY = y - (fontMetrics.ascent + fontMetrics.descent) / 2
                            val percentageValue = (item.value / totalValue) * 100
                            val percentageString = "${percentageFormatter.format(percentageValue)}%"
                            drawText(percentageString, x, centeredY, paint)
                        }
                    }
                }
                startAngle += (item.value / totalValue) * 360f * animationProgress.value
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(0.5f)
        ) {
            Text(
                text = totalLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))

            Text(
                text = formatAmountShort(totalAmount),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = amountTextStyle,
                onTextLayout = { textLayoutResult ->
                    if (textLayoutResult.hasVisualOverflow) {
                        amountTextStyle =
                            amountTextStyle.copy(fontSize = amountTextStyle.fontSize * 0.8f)
                    }
                }
            )
        }
    }
}


@Composable
fun ChartLegend(
    modifier: Modifier = Modifier,
    items: List<DonutData>, maxVisibleItems: Int = 3
) {
    Box(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.align(Alignment.Center)
        ) {
            val visibleItems = items.take(maxVisibleItems)
            val remainingCount = items.size - maxVisibleItems

            visibleItems.forEachIndexed { index, item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(item.color ?: ChartTheme.getColor(index), CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = item.amountText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (remainingCount > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.Gray, CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "$remainingCount more items...",
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


fun formatAmountShort(value: Double, useApproximateSymbol: Boolean = true): String {
    val absValue = abs(value)
    if (absValue < 1000) return value.toLong().toString()

    val formatter = DecimalFormat("#.#") // Format to one decimal place

    return when {
        absValue >= 1_000_000_000 -> {
            val billions = value / 1_000_000_000.0
            val formatted = formatter.format(billions)
            val prefix = if (useApproximateSymbol && value % 1_000_000_000.0 != 0.0) "~" else ""
            "$prefix${formatted}B"
        }

        absValue >= 1_000_000 -> {
            val millions = value / 1_000_000.0
            val formatted = formatter.format(millions)
            val prefix = if (useApproximateSymbol && value % 1_000_000.0 != 0.0) "~" else ""
            "$prefix${formatted}M"
        }

        absValue >= 1_000 -> {
            val thousands = value / 1_000.0
            val formatted = thousands.toLong()
            val prefix = if (useApproximateSymbol && value % 1_000.0 != 0.0) "~" else ""
            "$prefix${formatted}k"
        }

        else -> value.toLong().toString()
    }
}


@Composable
fun DonutChartWaiting(
    modifier: Modifier = Modifier,
    strokeWidthDp: Dp = 22.dp,
    gapDegree: Float = 15f
) {
    val density = LocalDensity.current
    val strokeWidthPx = with(density) { strokeWidthDp.toPx() }

    val fakeData = remember {
        listOf(
            Pair(Color(0xFF42A5F5), 0.3f), // Blue
            Pair(Color(0xFFFFCA28), 0.4f), // Yellow
            Pair(Color(0xFFFFA726), 0.2f), // Orange
            Pair(Color(0xFFE0E0E0), 0.1f)  // Gray
        )
    }

    val rotationAngle = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        rotationAngle.animateTo(
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(35.dp)
        ) {
            // --- FIX HERE ---
            rotate(rotationAngle.value) { // Use the rotate function from the drawScope
                var startAngle = -90f
                fakeData.forEach { (color, ratio) ->
                    val sweepAngle = ratio * 360f
                    drawArc(
                        color = color,
                        startAngle = startAngle + (gapDegree / 2),
                        sweepAngle = sweepAngle - gapDegree,
                        useCenter = false,
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt)
                    )
                    startAngle += sweepAngle
                }
            }
            // --------------------
        }
    }
}
