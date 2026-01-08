package com.ltcn272.finny.presentation.common.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun ModeSwitcherBubble(
    modifier: Modifier = Modifier,
    initialOffsetX: Float = 0f,
    initialOffsetY: Float = 900f,
    onLongPress: () -> Unit,
    bubbleSize: Int = 80
) {
    val offsetX = remember { Animatable(initialOffsetX) }
    val offsetY = remember { Animatable(initialOffsetY) }
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val configuration = LocalConfiguration.current
    val screenWidthPx = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }
    val bubbleSizePx = with(LocalDensity.current) { bubbleSize.dp.toPx() }

    var isFireMode by remember { mutableStateOf(false) }

    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.Asset(if (isFireMode) "fire.json" else "snow.json")
    )

    Box(modifier = modifier.fillMaxSize()) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
                .size(bubbleSize.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isFireMode = !isFireMode
                            onLongPress()
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
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
                }
        )
    }
}
