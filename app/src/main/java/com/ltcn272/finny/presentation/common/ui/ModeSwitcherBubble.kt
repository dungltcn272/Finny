package com.ltcn272.finny.presentation.common.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * A draggable floating action button that can be long-pressed to trigger an action.
 * This is used to switch between different dashboard modes.
 */
@Composable
fun ModeSwitcherBubble(
    modifier: Modifier = Modifier,
    initialOffsetX: Float = 0f,
    initialOffsetY: Float = 900f,
    onLongPress: () -> Unit,
    bubbleSize: Int = 60
) {
    val offsetX = remember { Animatable(initialOffsetX) }
    val offsetY = remember { Animatable(initialOffsetY) }
    val coroutineScope = rememberCoroutineScope()

    val configuration = LocalConfiguration.current
    val screenWidthPx = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }
    val bubbleSizePx = with(LocalDensity.current) { bubbleSize.dp.toPx() }

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
                .size(bubbleSize.dp)
                .shadow(8.dp, CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    ),
                    CircleShape
                )
                .pointerInput(Unit) {
                    // Detect long press to switch mode
                    detectTapGestures(
                        onLongPress = { onLongPress() }
                    )
                }
                .pointerInput(Unit) {
                    // Detect drag gestures to move the bubble
                    detectDragGestures(
                        onDragEnd = {
                            // Animate to the nearest edge (left or right)
                            val targetX =
                                if (offsetX.value + bubbleSizePx / 2 < screenWidthPx / 2) 0f
                                else screenWidthPx - bubbleSizePx
                            coroutineScope.launch {
                                offsetX.animateTo(
                                    targetX,
                                    spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow)
                                )
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            coroutineScope.launch {
                                val newX = (offsetX.value + dragAmount.x).coerceIn(
                                    0f,
                                    screenWidthPx - bubbleSizePx
                                )
                                val newY = (offsetY.value + dragAmount.y)
                                offsetX.snapTo(newX)
                                offsetY.snapTo(newY)
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DonutLarge, // Icon represents switching views
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size((bubbleSize / 2).dp)
            )
        }
    }
}
