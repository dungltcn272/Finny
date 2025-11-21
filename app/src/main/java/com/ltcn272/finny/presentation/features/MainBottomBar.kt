package com.ltcn272.finny.presentation.features

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ltcn272.finny.R
import com.ltcn272.finny.core.navigation.MainRoute
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch


data class NavItem(
    val label: String,
    val iconRes: Int,   // resource id trong drawable
    val route: String
)

private data class ItemBounds(val x: Float, val width: Float)

@Composable
fun MainBottomBar(
    selectedRoute: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    edgePadding: Dp = 4.dp,
    iconSize: Dp = 20.dp,
    textSize: TextUnit = 10.sp,
) {
    val items = remember {
        listOf(
            NavItem("Home", R.drawable.ic_home, MainRoute.HOME),
            NavItem("Transaction", R.drawable.ic_transaction, MainRoute.TRANSACTION),
            NavItem("Chat", R.drawable.ic_facebook, MainRoute.CHAT),
            NavItem("Challenge", R.drawable.ic_google, MainRoute.CHALLENGE)
        )
    }
    val bounds = remember { mutableStateMapOf<String, ItemBounds>() }
    var isInitialAnimationDone by remember { mutableStateOf(false) }

    val pillAnimX = remember { Animatable(0f) }
    val pillAnimWidth = remember { Animatable(0f) }
    val pillAnimScale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedRoute, bounds.size) {
        val currentBounds = bounds[selectedRoute] ?: return@LaunchedEffect

        if (!isInitialAnimationDone) {
            // --- KHỞI TẠO: Không có animation ---
            // Snap ngay lập tức đến vị trí và kích thước đầu tiên
            pillAnimX.snapTo(currentBounds.x)
            pillAnimWidth.snapTo(currentBounds.width)
            isInitialAnimationDone = true
        } else {
            // 1. Scale lên tại vị trí cũ (Chậm và mềm)
            pillAnimScale.animateTo(
                targetValue = 1.1f,
                animationSpec = spring( stiffness = Spring.StiffnessLow) // Chậm, mềm mại
            )

            // 2. Di chuyển và đổi kích thước đồng thời (Chậm hơn một chút)
            val moveJob = launch {
                pillAnimX.animateTo(
                    targetValue = currentBounds.x,
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow) // Chậm nhất để di chuyển dài hơn
                )
            }
            val resizeJob = launch {
                pillAnimWidth.animateTo(
                    targetValue = currentBounds.width,
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow) // Giữ mềm mại
                )
            }
            joinAll(moveJob, resizeJob) // Chờ cả 2 animation di chuyển và đổi size hoàn thành

            // 3. Scale về kích thước ban đầu (Thời gian bằng lúc scale lên)
            pillAnimScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(stiffness = Spring.StiffnessLow) // Giống hệt bước 1
            )
        }
    }


    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        shape = RoundedCornerShape(50.dp)
    ) {
        Box(
            modifier = Modifier.padding(all = edgePadding),
            contentAlignment = Alignment.CenterStart // Để pill bắt đầu từ gốc toạ độ
        ) {
            // Pill (viên thuốc) di chuyển
            BottomBarPill(
                x = pillAnimX.value,
                width = pillAnimWidth.value,
                scale = pillAnimScale.value,
                height = 56.dp // Chiều cao cố định cho pill
            )

            // Các item
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    BottomBarItem(
                        modifier = Modifier.weight(1f),
                        item = item,
                        isSelected = item.route == selectedRoute,
                        iconSize = iconSize,
                        textSize = textSize,
                        onSelect = {
                            if (item.route != selectedRoute) {
                                onSelect(item.route)
                            }
                        },
                        onPlaced = { newBounds ->
                            bounds[item.route] = newBounds
                        }
                    )
                }
            }
        }
    }
}


@Composable
private fun BottomBarPill(x: Float, width: Float, scale: Float, height: Dp) {
    val density = LocalDensity.current
    if (width == 0f) return // Không vẽ nếu chưa có kích thước

    Surface(
        modifier = Modifier
            .offset(x = with(density) { x.toDp() })
            .size(width = with(density) { width.toDp() }, height = height)
            .graphicsLayer {
                // Áp dụng scale tại tâm của pill
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(50),
        color = Color(0xFFE0E0FF)
    ) {}
}


@Composable
private fun BottomBarItem(
    modifier: Modifier = Modifier,
    item: NavItem,
    isSelected: Boolean,
    iconSize: Dp,
    textSize: TextUnit,
    onSelect: () -> Unit,
    onPlaced: (ItemBounds) -> Unit,
) {
    val fgColor = if (isSelected) Color(0xFF1E90FF) else Color.Black

    Column(
        modifier = modifier
            .onGloballyPositioned {
                onPlaced(ItemBounds(it.positionInParent().x, it.size.width.toFloat()))
            }
            .clip(RoundedCornerShape(50.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // Không có hiệu ứng ripple
                onClick = onSelect
            )
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(item.iconRes),
            contentDescription = item.label,
            tint = fgColor,
            modifier = Modifier.size(iconSize)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = item.label,
            color = fgColor,
            style = MaterialTheme.typography.labelSmall,
            fontSize = textSize,
            maxLines = 1
        )
    }
}
