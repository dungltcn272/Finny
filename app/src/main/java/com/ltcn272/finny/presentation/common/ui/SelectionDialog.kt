package com.ltcn272.finny.presentation.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ltcn272.finny.R
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun <T> SelectionDialog(
    title: String,
    items: List<T>,
    visible: Boolean,
    onDismiss: () -> Unit,
    onSelect: (T) -> Unit,
    itemToString: (T) -> String,
    initialSelection: T? = null,
    iconBuilder: @Composable ((T) -> Unit)? = null
) {
    if (!visible) return

    val itemHeight = 48.dp
    val dialogHeight = 220.dp
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current

    val visibleCount = remember(dialogHeight, itemHeight, density) {
        val dialogPx = with(density) { dialogHeight.toPx() }
        val itemPx = with(density) { itemHeight.toPx() }
        max(1, (dialogPx / itemPx).roundToInt())
    }
    val maxStart = remember(items.size, visibleCount) { max(0, items.size - visibleCount) }

    val requestedIndex = remember(initialSelection, items) {
        initialSelection?.let { items.indexOf(it) } ?: -1
    }

    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(requestedIndex, maxStart) {
        if (!initialized) {
            if (requestedIndex in items.indices) {
                val target = (requestedIndex - visibleCount / 2).coerceIn(0, maxStart)
                listState.scrollToItem(target)
            }
            initialized = true
        }
    }

    var centerIndex by remember { mutableIntStateOf(requestedIndex.coerceAtLeast(0)) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }.collectLatest { layoutInfo ->
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return@collectLatest

            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2f

            val newCenterIndex = visibleItems.minByOrNull {
                kotlin.math.abs((it.offset + it.size / 2) - viewportCenter)
            }?.index ?: centerIndex

            if (newCenterIndex != centerIndex) {
                centerIndex = newCenterIndex
                if (listState.isScrollInProgress) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            }
        }
    }

    val snapFling = rememberSnapFlingBehavior(
        lazyListState = listState,
        snapPosition = SnapPosition.Center
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.medium, color = Color.White) {
            Column(modifier = Modifier.padding(12.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }

                Box(modifier = Modifier.height(dialogHeight)) {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(vertical = ((dialogHeight - itemHeight) / 2)),
                        modifier = Modifier.fillMaxWidth(),
                        flingBehavior = snapFling
                    ) {
                        itemsIndexed(items) { index, item ->
                            val dist = kotlin.math.abs(index - centerIndex)
                            val scale = when (dist) {
                                0 -> 1f; 1 -> 0.95f; 2 -> 0.9f; else -> 0.8f
                            }
                            val alpha = when (dist) {
                                0 -> 1f; 1 -> 0.85f; 2 -> 0.6f; else -> 0.4f
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(itemHeight)
                                    .scale(scale)
                                    .alpha(alpha),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (iconBuilder != null) {
                                        iconBuilder(item)
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text(
                                        text = itemToString(item),
                                        fontSize = if (dist == 0) 18.sp else 14.sp,
                                        fontWeight = if (dist == 0) FontWeight.Bold else FontWeight.Normal,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeight)
                            .align(Alignment.Center)
                            .padding(horizontal = 24.dp)
                            .background(
                                Color(0xFF007AFF).copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp)
                            )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    Button(
                        onClick = { onDismiss() },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray.copy(alpha = 0.2f),
                            contentColor = Color.Gray
                        )
                    ) {
                        Text(stringResource(id = R.string.cancel))
                    }
                    Button(
                        onClick = {
                            if (items.isNotEmpty()) {
                                val idx = centerIndex.coerceIn(0, items.lastIndex)
                                onSelect(items[idx])
                            }
                            onDismiss()
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White
                        )
                    ) {
                        Text(stringResource(id = R.string.confirm))
                    }
                }
            }
        }
    }
}
