package com.ltcn272.finny.presentation.common.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.DpOffset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.R
import kotlinx.coroutines.delay

@Composable
fun AnimatedMoreMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    offset: DpOffset = DpOffset(0.dp, 0.dp)
) {
    val density = LocalDensity.current

    // Giữ popup sống đủ lâu để animate khi ĐÓNG
    var shouldShowPopup by remember { mutableStateOf(false) }

    // Tiến trình animation 0f..1f
    val progress = remember { Animatable(0f) }

    LaunchedEffect(expanded) {
        if (expanded) {
            // Bật popup lên trước rồi mới animate vào
            shouldShowPopup = true
            progress.snapTo(0f) // bắt đầu từ 0f cho chắc
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 350)
            )
        } else {
            // Animate thoát
            progress.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 350)
            )
            // Sau khi thoát xong mới ẩn popup
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
            // Nội suy từ progress (0f → 1f)
            val scale = 0.6f + 0.4f * progress.value          // 0.6 → 1.0
            val alpha = progress.value                         // 0 → 1
            val translateYPx = with(density) {
                (-20).dp.toPx() * (1f - progress.value)        // -20dp → 0
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                        translationY = translateYPx
                        transformOrigin = TransformOrigin(1f, 0f) // góc phải trên
                    }
                    .width(IntrinsicSize.Max)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {

                    MenuItemContent(
                        iconId = R.drawable.ic_edit,
                        text = "Edit",
                        onClick = onEditClick,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    MenuItemContent(
                        iconId = R.drawable.ic_delete,
                        text = "Delete",
                        onClick = onDeleteClick,
                        tint = Color.Red
                    )
                }
            }
        }
    }
}



// Composable cho từng mục trong menu
@Composable
private fun MenuItemContent(
    @DrawableRes iconId: Int,
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
            painter = painterResource(id = iconId),
            contentDescription = text,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black,
            fontWeight = FontWeight.Medium
        )
    }
}