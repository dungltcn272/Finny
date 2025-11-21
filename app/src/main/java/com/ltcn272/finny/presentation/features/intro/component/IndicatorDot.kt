package com.ltcn272.finny.ui.feature.on_boarding.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp


@Composable
fun DotIndicatorWithFade(
    pagerState: PagerState,
    pageCount: Int,
) {
    val dotSize = 9.dp
    val spacing = 16.dp

    val stepPx =
        with(androidx.compose.ui.platform.LocalDensity.current) { (dotSize + spacing).toPx() }
    val centerBias = (pageCount - 1) / 2f
    val progress = pagerState.currentPage + pagerState.currentPageOffsetFraction
    val tx = (progress - centerBias) * stepPx

    val alpha = 1f - pagerState.currentPageOffsetFraction.coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .wrapContentWidth()
            .graphicsLayer { this.alpha = alpha },
        contentAlignment = Alignment.Center
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
            repeat(pageCount) {
                IndicatorDot(color = Color.Gray)
            }
        }

        IndicatorDot(
            color = Color.Black,
            modifier = Modifier.graphicsLayer { translationX = tx }
        )
    }
}

@Composable
fun IndicatorDot(
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(9.dp)
            .clip(androidx.compose.foundation.shape.CircleShape)
            .background(color)
    )
}