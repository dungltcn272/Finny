package com.ltcn272.finny.presentation.features.snackbar

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class TopSnackbarType {
    SUCCESS, ERROR, WARNING, INFO
}

enum class TopSnackbarDuration(val millis: Long) {
    SHORT(3000),
    LONG(5000)
}

@Composable
fun FinnySnackbar(
    visible: Boolean,
    message: String,
    type: TopSnackbarType = TopSnackbarType.INFO,
    duration: TopSnackbarDuration = TopSnackbarDuration.SHORT,
    action: (@Composable () -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val (backgroundColor, icon) = snackbarStyle(type)
    val coroutineScope = rememberCoroutineScope()
    val offsetY = remember { Animatable(0f) }

    LaunchedEffect(visible) {
        if (visible) {
            offsetY.snapTo(0f)
            delay(duration.millis)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it - 50 }
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { -it - 50 }
        ) + fadeOut()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(0, offsetY.value.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            if (dragAmount.y < 0) {
                                coroutineScope.launch {
                                    offsetY.snapTo(offsetY.value + dragAmount.y)
                                }
                            }
                        },
                        onDragEnd = {
                            coroutineScope.launch {
                                if (offsetY.value < -size.height / 2) {
                                    onDismiss()
                                } else {
                                    offsetY.animateTo(0f, animationSpec = tween(300))
                                }
                            }
                        }
                    )
                }
                .padding(
                    WindowInsets.statusBars
                        .asPaddingValues()
                )
                .padding(top = 8.dp)
                .padding(horizontal = 16.dp)
                .background(
                    backgroundColor,
                    RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = message,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )

            if (action != null) {
                Spacer(modifier = Modifier.width(12.dp))
                action()
            }
        }
    }
}

@Composable
private fun snackbarStyle(type: TopSnackbarType): Pair<Color, ImageVector> {
    return when (type) {
        TopSnackbarType.SUCCESS -> Color(0xFF2E7D32) to Icons.Default.CheckCircle
        TopSnackbarType.ERROR -> Color(0xFFC62828) to Icons.Default.Error
        TopSnackbarType.WARNING -> Color(0xFFF9A825) to Icons.Default.Warning
        TopSnackbarType.INFO -> Color(0xFF1565C0) to Icons.Default.Info
    }
}

