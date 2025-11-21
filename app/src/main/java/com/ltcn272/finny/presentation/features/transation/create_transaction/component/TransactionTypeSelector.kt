package com.ltcn272.finny.presentation.features.transation.create_transaction.component

import androidx.compose.animation.Animatable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.ltcn272.finny.domain.model.TransactionType
import kotlinx.coroutines.launch

@Preview
@Composable
fun TransactionTypeSelectorPreview() {
    var selectedType by remember { mutableStateOf(TransactionType.OUTCOME) }

    TransactionTypeSelector(
        selectedType = selectedType,
        onTypeSelected = { selectedType = it },
        modifier = Modifier.padding(16.dp)
    )
}

val ExpenseColor = Color(0xFFD32F2F)
val IncomeColor = Color(0xFF388E3C)
val UnselectedText = Color(0xFF666666)
val SelectedText = Color.White

@Composable
fun TransactionTypeSelector(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 999.dp,
    verticalPadding: Dp = 8.dp,
    horizontalPadding: Dp = 16.dp,
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    var expenseBounds by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var incomeBounds by remember { mutableStateOf<LayoutCoordinates?>(null) }

    val animX = remember { androidx.compose.animation.core.Animatable(0f) }
    val animW = remember { androidx.compose.animation.core.Animatable(0f) }

    val animColor = remember { Animatable(if (selectedType == TransactionType.OUTCOME) ExpenseColor else IncomeColor) }

    LaunchedEffect(selectedType, expenseBounds, incomeBounds) {
        val targetCoords = when (selectedType) {
            TransactionType.OUTCOME -> expenseBounds
            TransactionType.INCOME -> incomeBounds
        }

        val targetColor = if (selectedType == TransactionType.OUTCOME) ExpenseColor else IncomeColor

        targetCoords?.let { coords ->
            val targetX = coords.positionInParent().x
            val targetW = coords.size.width.toFloat()

            if (animW.value == 0f) {
                animX.snapTo(targetX)
                animW.snapTo(targetW)
                animColor.snapTo(targetColor)
            } else {
                scope.launch { animX.animateTo(targetX, spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.9f)) }
                scope.launch { animW.animateTo(targetW, spring(stiffness = Spring.StiffnessLow, dampingRatio = 0.9f)) }
                scope.launch { animColor.animateTo(targetColor, tween(durationMillis = 300)) }
            }
        }
    }

    Surface(
        color = Color(0xFFF0F0F0),
        shape = RoundedCornerShape(cornerRadius),
        modifier = modifier
            .wrapContentSize()
    ) {
        Box(modifier = Modifier.wrapContentSize().padding(2.dp)) {

            val xDp = with(density) { animX.value.toDp() }
            val wDp = with(density) { animW.value.toDp() }
            val hDp = with(density) { (expenseBounds ?: incomeBounds)?.size?.height?.toFloat()?.toDp() ?: 0.dp }

            if (wDp > 0.dp && hDp > 0.dp) {
                Surface(
                    color = animColor.value,
                    shape = RoundedCornerShape(cornerRadius),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .offset(x = xDp)
                        .width(wDp)
                        .height(hDp)
                        .zIndex(0.5f)
                ) {}
            }

            Row(
                modifier = Modifier.zIndex(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Chip(
                    label = "Expense",
                    isSelected = selectedType == TransactionType.OUTCOME,
                    selectedText = SelectedText,
                    unselectedText = UnselectedText,
                    verticalPadding = verticalPadding,
                    horizontalPadding = horizontalPadding,
                    onClick = { onTypeSelected(TransactionType.OUTCOME) },
                ) { coords ->
                    expenseBounds = coords
                }

                Chip(
                    label = "Income",
                    isSelected = selectedType == TransactionType.INCOME,
                    selectedText = SelectedText,
                    unselectedText = UnselectedText,
                    verticalPadding = verticalPadding,
                    horizontalPadding = horizontalPadding,
                    onClick = { onTypeSelected(TransactionType.INCOME) },
                ) { coords ->
                    incomeBounds = coords
                }
            }
        }
    }
}

@Composable
private fun Chip(
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
    val targetTxtColor = if (isSelected) selectedText else unselectedText
    val txtColor by animateColorAsState(targetValue = targetTxtColor, animationSpec = tween(300), label = "ChipTextColor")

    Box(
        modifier = Modifier
            .onGloballyPositioned(onPlaced)
            .clip(RoundedCornerShape(999.dp))
            .clickable(
                interactionSource = interaction,
                indication = null
            ) { onClick() }
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