package com.ltcn272.finny.presentation.common.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

@Composable
fun AnimatedCurrencyMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    currencies: List<String>,
    selectedCurrency: String,
    onCurrencyClick: (String) -> Unit,
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
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(
                        alpha = 0.9f
                    )
                ),
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
                    currencies.forEach { currency ->
                        CurrencyMenuItemContent(
                            text = currency,
                            isSelected = currency == selectedCurrency,
                            onClick = { onCurrencyClick(currency) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrencyMenuItemContent(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
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
        Box(modifier = Modifier.size(24.dp)) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.Green,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}
