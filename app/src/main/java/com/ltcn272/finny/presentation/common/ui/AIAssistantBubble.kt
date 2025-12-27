package com.ltcn272.finny.presentation.common.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ltcn272.finny.R
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
    initialOffsetY: Float = 200f,
    fullMessage: String,
    messageType: MessageType = MessageType.ASSISTANT,
    bubbleSize: Int = 60,
    typingDelayMs: Long = 40
) {
    val offsetX = remember { Animatable(initialOffsetX) }
    val offsetY = remember { Animatable(initialOffsetY) }
    val coroutineScope = rememberCoroutineScope()

    var displayedText by remember { mutableStateOf("") }
    var isMessageVisible by remember { mutableStateOf(true) }

    // --- LOGIC MỚI: Chỉ typing lần đầu ---
    var hasTypedOnce by remember { mutableStateOf(false) }
    // ------------------------------------

    val configuration = LocalConfiguration.current
    val screenWidthPx = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }
    val bubbleSizePx = with(LocalDensity.current) { bubbleSize.dp.toPx() }

    var targetWidth by remember { mutableStateOf(0.dp) }
    var targetHeight by remember { mutableStateOf(0.dp) }
    var measured by remember { mutableStateOf(false) }
    val density = LocalDensity.current

    val animatedWidth by animateDpAsState(
        targetValue = if (measured) targetWidth else 0.dp,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "bubble_width_anim"
    )
    val animatedHeight by animateDpAsState(
        targetValue = if (measured) targetHeight else 0.dp,
        animationSpec = spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow),
        label = "bubble_height_anim"
    )

    // Hiệu ứng gõ chữ
    LaunchedEffect(fullMessage, measured, hasTypedOnce) {
        if (!measured || hasTypedOnce) return@LaunchedEffect

        displayedText = ""
        fullMessage.forEachIndexed { index, char ->
            displayedText += char
            if (index > 0) {
                delay(typingDelayMs)
            }
        }
        // Đánh dấu đã gõ xong
        hasTypedOnce = true
    }

    Box(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
                .padding(8.dp),
        ) {
            // 1. Bong bóng Chat (Avatar AI)
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
                        onClick = {
                            isMessageVisible = !isMessageVisible
                            // Nếu đã typing xong, chỉ cần hiển thị lại text đầy đủ
                            if (isMessageVisible && hasTypedOnce) {
                                displayedText = fullMessage
                            }
                        }
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

            // 2. Khung Text (Lời thoại)
            AnimatedVisibility(
                visible = isMessageVisible,
                enter = scaleIn(transformOrigin = TransformOrigin(0f, 1f)) + fadeIn(),
                exit = scaleOut(transformOrigin = TransformOrigin(0f, 1f)) + fadeOut()
            ) {
                Box {
                    val surfaceColor: Color
                    val borderColor: Color
                    if (messageType == MessageType.ASSISTANT) {
                        surfaceColor = Color(0xFFE8F5E9).copy(alpha = 0.95f) // Xanh lá nhạt
                        borderColor = Color(0xFF2E7D32) // Xanh lá đậm
                    } else {
                        surfaceColor = Color(0xFFFFEBEE).copy(alpha = 0.95f) // Đỏ nhạt
                        borderColor = Color(0xFFC62828) // Đỏ đậm
                    }

                    // Hộp thoại ẩn để đo kích thước
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 4.dp,
                            topEnd = 16.dp,
                            bottomEnd = 16.dp,
                            bottomStart = 16.dp
                        ),
                        modifier = Modifier
                            .widthIn(max = 250.dp)
                            .alpha(0f)
                            .onSizeChanged {
                                if (!measured) {
                                    targetWidth = with(density) { it.width.toDp() }
                                    targetHeight = with(density) { it.height.toDp() }
                                    measured = true
                                }
                            }
                    ) {
                        Text(
                            text = fullMessage,
                            modifier = Modifier.padding(12.dp),
                            style = TextStyle(fontSize = 14.sp, lineHeight = 20.sp)
                        )
                    }

                    // Hộp thoại thật, sử dụng kích thước đã animate
                    if (measured) {
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
                            modifier = Modifier
                                .width(animatedWidth)
                                .height(animatedHeight)
                        ) {
                            Text(
                                text = displayedText,
                                modifier = Modifier.padding(12.dp),
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
