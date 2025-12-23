package com.ltcn272.finny.presentation.common.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun <T> SegmentedControl(
    modifier: Modifier = Modifier,
    options: List<T>,
    selected: T,
    onOptionClicked: (T) -> Unit,
    titleForItem: (T) -> String,

    containerColor: Color = Color(0xFFEFEFF4),
    indicatorColor: Color = Color.White,
    indicatorPadding: Dp = 0.dp,

    contentPadding : PaddingValues = PaddingValues(vertical = 10.dp, horizontal = 5.dp),
) {
    val numSegments = options.size.toFloat()
    val selectedIndex = options.indexOf(selected)

    val offset by animateFloatAsState(
        targetValue = if (selectedIndex != -1) selectedIndex.toFloat() else 0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 200f),
        label = "SegmentedControlOffsetAnimation"
    )

    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .background(containerColor, CircleShape)
            .drawWithContent {
                val segmentWidth = this.size.width / numSegments
                val paddingValue = indicatorPadding.toPx()

                drawRoundRect(
                    color = indicatorColor,
                    topLeft = Offset(
                        (segmentWidth * offset) + paddingValue,
                        paddingValue
                    ),
                    size = Size(
                        segmentWidth - (paddingValue * 2),
                        this.size.height - (paddingValue * 2)
                    ),
                    cornerRadius = CornerRadius(this.size.height / 2)
                )
                drawContent()
            }
            .clip(CircleShape),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.onEachIndexed { index, segment ->
            Box(
                Modifier
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .clickable(
                        enabled = true,
                        onClickLabel = titleForItem(segment),
                        role = Role.Button,
                        interactionSource = MutableInteractionSource(),
                        indication = null
                    ) { onOptionClicked(segment) }
                    .padding(contentPadding)
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = titleForItem(segment),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.W600),
                )
            }
        }
    }
}