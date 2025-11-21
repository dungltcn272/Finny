package com.ltcn272.finny.presentation.features.intro.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun IntroPage(
    scale: Float,
    alpha: Float,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .graphicsLayer {
                this.scaleY = scale
                this.scaleX = scale
                this.alpha = alpha
            }
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
        content = content
    )
}