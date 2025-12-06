package com.ltcn272.finny.presentation.common.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

// Thêm lớp này vào file của bạn
@Immutable
private data class RightAlignedPopupPositionProvider(
    val contentOffset: DpOffset, // Offset từ góc của Anchor
    val density: Density,
    val padding: Int // Khoảng padding từ viền màn hình (tính bằng px)
) : androidx.compose.ui.window.PopupPositionProvider {

    override fun calculatePosition(
        anchorBounds: IntRect, // Vị trí của Icon/Anchor
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize // Kích thước của Popup
    ): IntOffset {
        // --- 1. TÍNH VỊ TRÍ Y (Dọc) ---
        // Đặt Popup ngay bên dưới Anchor, cộng thêm contentOffset.y
        val y = anchorBounds.bottom + with(density) { contentOffset.y.roundToPx() }

        // --- 2. TÍNH VỊ TRÍ X (Ngang) ---
        val contentOffsetX = with(density) { contentOffset.x.roundToPx() }

        // Mục tiêu: Căn lề phải của Popup trùng với lề phải của Anchor, sau đó lùi vào padding.

        // Vị trí X lý tưởng (căn lề phải của Popup theo lề phải của Anchor)
        // X = (Lề phải Anchor) - (Chiều rộng Popup)
        var x = anchorBounds.right - popupContentSize.width

        // Thêm offset ngang (contentOffset.x)
        x += contentOffsetX

        // --- 3. ĐIỀU CHỈNH CHỐNG TRÀN ---

        // Kiểm tra Popup có tràn ra ngoài viền trái không
        if (x < padding) {
            // Nếu tràn, đặt Popup cách viền trái một khoảng padding
            x = padding
        } else {
            // Kiểm tra Popup có tràn ra ngoài viền phải không
            val rightBound = x + popupContentSize.width
            if (rightBound > windowSize.width - padding) {
                // Nếu tràn, đặt Popup cách viền phải một khoảng padding
                x = windowSize.width - popupContentSize.width - padding
            }
        }

        // Đảm bảo không tràn viền dưới
        val yFinal = if (y + popupContentSize.height > windowSize.height) {
            // Nếu tràn, đặt Popup lên trên Anchor
            anchorBounds.top - popupContentSize.height - with(density) { contentOffset.y.roundToPx() }
        } else {
            y
        }

        return IntOffset(x, yFinal)
    }
}

// ... (các imports và Animatable logic giữ nguyên) ...

@Composable
fun AnimatedCustomPopup(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    // THÊM: Padding từ viền màn hình
    screenPadding: Dp = 16.dp,
    shape: RoundedCornerShape = RoundedCornerShape(18.dp),
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit
) {
    val density = LocalDensity.current

    // Biến trạng thái để lưu vị trí Pivot (điểm mở)
    var pivotX by remember { mutableStateOf(1f) }
    var pivotY by remember { mutableStateOf(0f) }

    val paddingPx = with(density) { screenPadding.roundToPx() }

    var shouldShowPopup by remember { mutableStateOf(expanded) }
    val progress = remember { Animatable(if (expanded) 1f else 0f) }

    // ... (LaunchedEffect logic giữ nguyên) ...

    if (shouldShowPopup) {
        // --- SỬ DỤNG POPUP POSITION PROVIDER ---
        val popupPositionProvider = remember(offset, paddingPx) {
            RightAlignedPopupPositionProvider(
                contentOffset = offset,
                density = density,
                padding = paddingPx
            )
        }

        Popup(
            // Dùng popupPositionProvider thay vì alignment cố định
            popupPositionProvider = popupPositionProvider,
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(focusable = true)
        ) {
            // Tính toán giá trị animation
            val scale = 0.8f + (0.2f * progress.value)
            val alpha = progress.value
            val translateYPx = with(density) { (-8).dp.toPx() * (1f - progress.value) }

            Card(
                shape = shape,
                colors = CardDefaults.cardColors(containerColor = containerColor),
                modifier = modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                        translationY = translateYPx
                        // Cập nhật transformOrigin dựa trên Pivot X, Y đã tính (mặc định 1f, 0f)
                        transformOrigin = TransformOrigin(pivotX, pivotY)
                    }
                    .width(IntrinsicSize.Max)
            ) {
                Column(content = content)
            }
        }
    }
}