package com.ltcn272.finny.presentation.features.transaction.create_edit.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.TransactionType

@Composable
fun TypeSelectionPopup(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit,
    offset: DpOffset = DpOffset(0.dp, 0.dp)
) {
    val types = TransactionType.entries
    val density = LocalDensity.current

    // Animation state
    val progress = remember { Animatable(0f) }
    var shouldShowPopup by remember { mutableStateOf(false) }

    LaunchedEffect(expanded) {
        if (expanded) {
            shouldShowPopup = true
            progress.snapTo(0f)
            progress.animateTo(
                1f,
                animationSpec = tween(durationMillis = 280, easing = Easing { 1f - (1f - it) * (1f - it) })
            )
        } else {
            progress.animateTo(0f, animationSpec = tween(durationMillis = 220))
            shouldShowPopup = false
        }
    }

    if (shouldShowPopup) {
        Popup(
            alignment = Alignment.TopEnd, // Căn chỉnh theo vị trí của view cha
            onDismissRequest = onDismissRequest,
            offset = with(density) { IntOffset(offset.x.roundToPx(), offset.y.roundToPx()) },
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
                        transformOrigin = TransformOrigin(1f, 0f) // Animation từ góc trên-phải
                    }
                    .width(IntrinsicSize.Max)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    types.forEach { type ->
                        TypeMenuItem(
                            type = type,
                            isSelected = type == selectedType,
                            onClick = { onTypeSelected(type) }
                        )
                    }
                }
            }
        }
    }
}

// Composable TypeMenuItem giữ nguyên
@Composable
private fun TypeMenuItem(    type: TransactionType,
                             isSelected: Boolean,
                             onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    val icon: ImageVector
    val text: String
    val iconColor: Color
    val iconBgColor: Color

    if (type == TransactionType.INCOME) {
        icon = Icons.Default.ArrowDownward
        text = stringResource(R.string.incoming)
        iconColor = Color(0xFF2E7D32) // Màu xanh đậm cho icon
        iconBgColor = Color(0xFFC8E6C9) // Màu xanh nhạt cho nền
    } else {
        icon = Icons.Default.ArrowUpward
        text = stringResource(R.string.outcoming)
        iconColor = Color(0xFFC62828) // Màu đỏ đậm cho icon
        iconBgColor = Color(0xFFFFCDD2) // Màu đỏ nhạt cho nền
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = interactionSource
            )
            .padding(horizontal = 16.dp, vertical = 8.dp), // Giảm padding dọc
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- DẤU TÍCH XANH (BÊN TRÁI) ---
        Box(modifier = Modifier.size(24.dp)) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color(0xFF4CAF50), // Màu xanh lá
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))

        // --- ICON MỚI (TRONG BOX TRÒN) ---
        Box(
            modifier = Modifier
                .size(28.dp) // Kích thước nền tròn
                .background(iconBgColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = iconColor,
                modifier = Modifier.size(16.dp) // Kích thước icon bên trong
            )
        }
        Spacer(modifier = Modifier.width(12.dp))

        // --- TEXT ---
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
    }
}
