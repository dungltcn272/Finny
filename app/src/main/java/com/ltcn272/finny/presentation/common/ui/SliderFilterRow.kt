package com.ltcn272.finny.presentation.common.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun <T> SliderFilterRow(
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T?) -> Unit,
    itemToString: (T) -> String,
    modifier: Modifier = Modifier,
    // ---- params dễ chỉnh ----
    selectedBg: Color = Color.White,
    unselectedBg: Color = Color(0xFFE6E6E6),
    selectedText: Color = Color.Black,
    unselectedText: Color = Color(0xFF666666),
    cornerRadius: Dp = 999.dp,
    verticalPadding: Dp = 6.dp,
    horizontalPadding: Dp = 16.dp,
) {
    val scroll = rememberScrollState()
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    data class ChipBounds(val x: Float, val w: Float, val h: Float)
    val bounds = remember { mutableStateMapOf<Any?, ChipBounds>() }

    val animX = remember { Animatable(0f) }
    val animW = remember { Animatable(0f) }

    // animate đồng thời vị trí & độ rộng của pill
    LaunchedEffect(selectedItem, bounds[selectedItem]) {
        val b = bounds[selectedItem] ?: return@LaunchedEffect
        coroutineScope {
            launch { animX.animateTo(b.x, spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.9f)) }
            launch { animW.animateTo(b.w, spring(stiffness = Spring.StiffnessLow, dampingRatio = 0.9f)) }
        }
    }

    // container chung
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp)
    ) {
        // lớp cuộn chung cho pill + chip
        Box(
            modifier = Modifier
                .horizontalScroll(scroll)
                .padding(end = 10.dp)
        ) {
            // ----- LỚP NỀN: capsules xám cho các chip KHÔNG chọn -----
            bounds.forEach { (key, b) ->
                if (key != selectedItem && b.w > 0f && b.h > 0f) {
                    Box(
                        Modifier
                            .offset(x = with(density) { b.x.toDp() })
                            .width(with(density) { b.w.toDp() })
                            .height(with(density) { b.h.toDp() })
                            .clip(RoundedCornerShape(cornerRadius))
                            .background(unselectedBg)
                            .zIndex(0f)
                    )
                }
            }

            // ----- PILL trắng trượt cho chip ĐANG chọn (nằm trên xám, dưới chữ) -----
            val xDp = with(density) { animX.value.toDp() }
            val wDp = with(density) { animW.value.toDp() }
            val hDp = with(density) { (bounds[selectedItem]?.h ?: 0f).toDp() }
            if (wDp > 0.dp && hDp > 0.dp) {
                Box(
                    Modifier
                        .offset(x = xDp)
                        .width(wDp)
                        .height(hDp)
                        .clip(RoundedCornerShape(cornerRadius))
                        .background(selectedBg)
                        .zIndex(0.5f) // dưới text
                )
            }

            // ----- CHỮ & click (không ripple), luôn ở trên cùng -----
            Row(
                modifier = Modifier.zIndex(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChipMeasurable(
                    label = "All",
                    isSelected = selectedItem == null,
                    selectedText = selectedText,
                    unselectedText = unselectedText,
                    verticalPadding = verticalPadding,
                    horizontalPadding = horizontalPadding,
                    onClick = { onItemSelected(null) },
                ) { coords ->
                    val p = coords.positionInParent()
                    val s = coords.size
                    bounds[null] = ChipBounds(p.x, s.width.toFloat(), s.height.toFloat())
                    if (selectedItem == null && animW.value == 0f) {
                        scope.launch {
                            animX.snapTo(p.x); animW.snapTo(s.width.toFloat())
                        }
                    }
                }

                items.forEach { item ->
                    ChipMeasurable(
                        label = itemToString(item),
                        isSelected = item == selectedItem,
                        selectedText = selectedText,
                        unselectedText = unselectedText,
                        verticalPadding = verticalPadding,
                        horizontalPadding = horizontalPadding,
                        onClick = { onItemSelected(item) },
                    ) { coords ->
                        val p = coords.positionInParent()
                        val s = coords.size
                        bounds[item] = ChipBounds(p.x, s.width.toFloat(), s.height.toFloat())
                        if (selectedItem == item && animW.value == 0f) {
                            scope.launch {
                                animX.snapTo(p.x); animW.snapTo(s.width.toFloat())
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChipMeasurable(
    label: String,
    isSelected: Boolean,
    selectedText: Color,
    unselectedText: Color,
    verticalPadding: Dp,
    horizontalPadding: Dp,
    onClick: () -> Unit,
    onPlaced: (LayoutCoordinates) -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val txtColor = if (isSelected) selectedText else unselectedText

    Box(
        modifier = Modifier
            .onPlaced(onPlaced)
            .clip(CircleShape)
            // clickable KHÔNG ripple:
            .clickable(
                interactionSource = interaction,
                indication = null
            ) { onClick() }
            .padding(horizontal = 0.dp, vertical = 2.dp) // spacing dọc tổng thể
    ) {
        Text(
            text = label,
            color = txtColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = verticalPadding)
        )
    }
}