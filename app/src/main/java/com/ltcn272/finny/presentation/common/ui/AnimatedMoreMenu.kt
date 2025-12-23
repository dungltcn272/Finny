package com.ltcn272.finny.presentation.common.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.DpOffset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedMoreMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    offset: DpOffset = DpOffset(0.dp, 0.dp)
) {
    val density = LocalDensity.current

    var shouldShowPopup by remember { mutableStateOf(false) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(expanded) {
        if (expanded) {
            shouldShowPopup = true
            progress.snapTo(0f)
            progress.animateTo(
                1f,
                animationSpec = tween(
                    durationMillis = 280,
                    easing = Easing { fraction -> 1f - (1f - fraction) * (1f - fraction) }
                )
            )
        } else {
            progress.animateTo(
                0f,
                animationSpec = tween(durationMillis = 220)
            )
            shouldShowPopup = false
        }
    }

    if (shouldShowPopup) {
        Popup(
            alignment = Alignment.TopEnd,
            onDismissRequest = onDismissRequest,
            offset = with(density) {
                IntOffset(offset.x.roundToPx(), offset.y.roundToPx())
            },
            properties = PopupProperties(focusable = true)
        ) {
            val scale = 0.8f + (0.2f * progress.value)
            val alpha = progress.value
            val translateYPx = with(density) { (-8).dp.toPx() * (1f - progress.value) }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                        translationY = translateYPx
                        transformOrigin = TransformOrigin(1f, 0f)
                    }
                    .width(IntrinsicSize.Max)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {

                    MenuItemContent(
                        icon = Icons.Default.Edit,
                        text = "Edit",
                        onClick = onEditClick,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    MenuItemContent(
                        icon = Icons.Default.Delete,
                        text = "Delete",
                        onClick = onDeleteClick,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuItemContent(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    tint: Color
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = interactionSource
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = tint,
            modifier = Modifier.size(23.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}
