package com.ltcn272.finny.presentation.common.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp

@Composable
fun GradientFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fabGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF8A2BE2), Color(0xFF4B0082)),
    )
    val shadowColor = Color.Black.copy(alpha = 0.4f)

    val infiniteTransition = rememberInfiniteTransition(label = "fab_pulse_transition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = { it * it }),
            repeatMode = RepeatMode.Restart
        ), label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Restart
        ), label = "pulse_alpha"
    )

    Box(
        modifier = modifier
            .size(64.dp)
            .drawBehind {
                scale(scale = pulseScale) {
                    drawCircle(
                        color = Color(0xFF8A2BE2),
                        radius = size.width / 2,
                        alpha = pulseAlpha
                    )
                }
                translate(top = 10f) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(shadowColor, Color.Transparent),
                            radius = size.width / 1.8f
                        )
                    )
                }
            }
            .clip(CircleShape)
            .background(fabGradient)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Transaction",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }
}
