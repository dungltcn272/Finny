package com.ltcn272.finny.presentation.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

@Composable
fun FinnyProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF2EC4B6),
    trackColor: Color = Color(0xFFEAEAEA)
) {
    val safeProgress = progress.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(100))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(safeProgress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(100))
                .background(color)
        )
    }
}