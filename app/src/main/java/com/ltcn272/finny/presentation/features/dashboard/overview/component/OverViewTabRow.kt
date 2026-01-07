package com.ltcn272.finny.presentation.features.dashboard.overview.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity


@Composable
fun OverViewTabRow(
    selectedTabIndex: Int,
    tabs: List<String>,
    onTabSelected: (Int) -> Unit
) {
    val density = LocalDensity.current
    var tabWidth by remember { mutableStateOf(0.dp) }

    val indicatorOffset by animateDpAsState(
        targetValue = tabWidth * selectedTabIndex,
        animationSpec = tween(durationMillis = 250),
        label = "IndicatorAnimation"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        tabWidth = with(density) { (coordinates.size.width / tabs.size).toDp() }
                    }
            ) {
                tabs.forEachIndexed { index, title ->
                    OverViewTab(
                        modifier = Modifier.weight(1f),
                        selected = selectedTabIndex == index,
                        text = title,
                        onClick = { onTabSelected(index) }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(tabWidth)
                    .height(3.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = indicatorOffset)
                    .background(Color.Black)
            )
        }

    }
}

@Composable
fun OverViewTab(
    modifier: Modifier = Modifier,
    selected: Boolean,
    text: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .height(48.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            color = if (selected) Color.Black else Color.Gray,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Preview
@Composable
fun PreviewOverViewTab() {
    var selectedIndex by remember { mutableStateOf(0) }
    val categories = listOf("Danh mục", "Ngân sách")

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        OverViewTabRow(
            selectedTabIndex = selectedIndex,
            tabs = categories,
            onTabSelected = { selectedIndex = it }
        )
    }
}