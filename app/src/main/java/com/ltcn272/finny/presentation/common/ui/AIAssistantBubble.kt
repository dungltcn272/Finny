package com.ltcn272.finny.presentation.common.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class MessageType {
    ASSISTANT, ERROR
}

@Composable
fun AIAssistantBubble(
    modifier: Modifier = Modifier,
    initialOffsetX: Float = 0f,
    initialOffsetY: Float = 500f,
    fullMessage: String,
    isLoading: Boolean,
    messageType: MessageType = MessageType.ASSISTANT,
    bubbleSize: Int = 60,
    paragraphDelayMs: Long = 2500
) {
    val offsetX = remember { Animatable(initialOffsetX) }
    val offsetY = remember { Animatable(initialOffsetY) }
    val coroutineScope = rememberCoroutineScope()

    var isMessageVisible by remember { mutableStateOf(true) }
    var displayedParagraphIndex by remember { mutableStateOf(0) }
    var paragraphs by remember { mutableStateOf<List<String>>(emptyList()) }

    val configuration = LocalConfiguration.current
    val screenWidthPx = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }
    val bubbleSizePx = with(LocalDensity.current) { bubbleSize.dp.toPx() }


    LaunchedEffect(fullMessage, isLoading) {
        if (isLoading) {
            paragraphs = emptyList()
            displayedParagraphIndex = 0
        } else if (fullMessage.isNotEmpty() && paragraphs.isEmpty()) {
            paragraphs = fullMessage.split("\n\n").filter { it.isNotBlank() }
            while (displayedParagraphIndex < paragraphs.size) {
                delay(paragraphDelayMs)
                if (displayedParagraphIndex < paragraphs.size - 1) {
                    displayedParagraphIndex++
                } else {
                    break
                }
            }
        }
    }


    Box(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
                .padding(8.dp),
        ) {
            Box(
                modifier = Modifier
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
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { isMessageVisible = !isMessageVisible }
                    )
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                val targetX =
                                    if (offsetX.value + bubbleSizePx / 2 < screenWidthPx / 2) 0f
                                    else screenWidthPx - bubbleSizePx - 32.dp.toPx()
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
                    imageVector = Icons.Default.Assistant,
                    contentDescription = "AI Assistant",
                    tint = Color.White,
                    modifier = Modifier.size((bubbleSize / 2).dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            AnimatedVisibility(
                visible = isMessageVisible,
                enter = scaleIn(transformOrigin = TransformOrigin(0f, 1f)) + fadeIn(),
                exit = scaleOut(transformOrigin = TransformOrigin(0f, 1f)) + fadeOut()
            ) {
                val surfaceColor: Color
                val borderColor: Color
                if (messageType == MessageType.ASSISTANT) {
                    surfaceColor = Color(0xFFE8F5E9).copy(alpha = 0.95f)
                    borderColor = Color(0xFF2E7D32)
                } else {
                    surfaceColor = Color(0xFFFFEBEE).copy(alpha = 0.95f)
                    borderColor = Color(0xFFC62828)
                }

                Surface(
                    shape = RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 16.dp,
                        bottomEnd = 16.dp,
                        bottomStart = 16.dp
                    ),
                    color = surfaceColor,
                    shadowElevation = 4.dp,
                    border = BorderStroke(1.dp, borderColor.copy(alpha = 0.7f)),
                ) {
                    if (isLoading) {
                        ThinkingIndicator(modifier = Modifier.padding(12.dp))
                    } else if (paragraphs.isNotEmpty()) {
                        AnimatedContent(
                            targetState = displayedParagraphIndex,
                            transitionSpec = {
                                (slideInVertically { height -> height } + fadeIn()).togetherWith(
                                    slideOutVertically { height -> -height } + fadeOut())
                            },
                            label = "paragraph_switcher"
                        ) { index ->
                            Text(
                                text = paragraphs.getOrElse(index) { "" },
                                modifier = Modifier
                                    .widthIn(max = 250.dp)
                                    .padding(12.dp),
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 20.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThinkingIndicator(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "thinking_indicator")
    val dots = List(3) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1000,
                    delayMillis = index * 150,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ), label = "dot_$index"
        )
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        dots.forEach { anim ->
            Box(
                Modifier
                    .padding(horizontal = 3.dp)
                    .size(8.dp)
                    .scale(if (anim.value < 0.5f) (anim.value * 2) else (1 - (anim.value - 0.5f) * 2))
                    .alpha(if (anim.value < 0.1f || anim.value > 0.9f) 0.5f else 1f)
                    .background(Color.Gray, CircleShape)
            )
        }
    }
}
