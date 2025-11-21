package com.ltcn272.finny.presentation.features.budget.create_budget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ltcn272.finny.domain.model.BudgetPeriod
import com.ltcn272.finny.presentation.theme.BudgetChipBorder
import com.ltcn272.finny.presentation.theme.BudgetChipSelectedBackground
import com.ltcn272.finny.presentation.theme.BudgetPeriodBackground
import com.ltcn272.finny.presentation.theme.BudgetTitle

private fun BudgetPeriod.toDisplayName(): String = when (this) {
    BudgetPeriod.SINGLE -> "Single"
    BudgetPeriod.ONE_WEEK -> "1 Week"
    BudgetPeriod.ONE_MONTH -> "1 Month"
    BudgetPeriod.ONE_YEAR -> "1 Year"
    BudgetPeriod.CUSTOM -> "Custom"
    BudgetPeriod.UNKNOWN -> "Unknown"
}

@Composable
fun PeriodSegmentedControl(
    value: BudgetPeriod,
    onValueChange: (BudgetPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    val periods = listOf(BudgetPeriod.SINGLE, BudgetPeriod.ONE_WEEK, BudgetPeriod.ONE_MONTH, BudgetPeriod.ONE_YEAR)

    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(16.dp))
            .background(BudgetPeriodBackground)
            .padding(vertical = 2.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        periods.forEachIndexed { index, opt ->
            ChipItem(
                text = opt.toDisplayName(),
                selected = value == opt,
                onClick = { onValueChange(opt) },
                modifier = Modifier.weight(1f)
            )
            if (index < periods.size - 1) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .width(1.dp)
                        .fillMaxHeight(0.55f)
                        .background(BudgetChipBorder)
                )
            }
        }
    }
}

@Composable
fun ChipItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) BudgetChipSelectedBackground else Color.Transparent)
            .clickable(
                interactionSource = androidx.compose.runtime.remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = BudgetTitle,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}
