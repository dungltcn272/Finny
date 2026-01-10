package com.ltcn272.finny.presentation.common.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.presentation.common.util.rememberCurrencyFormatter

@Composable
fun BudgetItem(
    budget: Budget,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit) = {}
) {

    val formatter = rememberCurrencyFormatter(budget.currency)

    val percentage = budget.progress * 100
    val progressText = remember(budget.progress) {
        if (percentage == 0.0 || percentage % 1.0 == 0.0) {
            String.format("%.0f%%", percentage)
        } else {
            String.format("%.1f%%", percentage)
        }
    }

    val progressColor = when {
        budget.progress < 0.8 -> Color(0xFF2EC4B6)
        budget.progress < 0.95 -> Color(0xFFFFBF00)
        else -> Color(0xFFE74C3C)
    }

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E5E5)),
        color = Color.White,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = budget.name,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = progressText,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(10.dp))

            FinnyProgressIndicator(
                progress = budget.progress.toFloat().coerceAtMost(1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = progressColor,
                trackColor = Color(0xFFEAEAEA)
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                BudgetInfo(
                    label = stringResource(R.string.budget_remaining),
                    value = formatter.format(budget.amount),
                    modifier = Modifier.weight(1f)
                )

                BudgetInfo(
                    label = stringResource(R.string.budget_limit),
                    value = formatter.format(budget.limit),
                    modifier = Modifier.weight(1f),
                    alignEnd = true
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                BudgetInfo(
                    label = stringResource(R.string.budget_total_income),
                    value = formatter.format(budget.totalIncome),
                    modifier = Modifier.weight(1f)
                )

                BudgetInfo(
                    label = stringResource(R.string.budget_total_outcome),
                    value = formatter.format(budget.totalOutcome),
                    modifier = Modifier.weight(1f),
                    alignEnd = true
                )
            }

            if (budget.recurring != null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.budget_recurring_topup),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun BudgetInfo(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false
) {
    Row(
        modifier = modifier,
        horizontalArrangement = if (alignEnd) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                color = Color.Gray
            ),
            maxLines = 1
        )

        Text(
            text = value,
            style = TextStyle(
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                letterSpacing = (-0.3).sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.basicMarquee()
        )
    }
}

@Composable
fun BudgetItemShimmer(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E5E5)),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )

                Spacer(Modifier.width(20.dp))

                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
            }

            Spacer(Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .shimmerEffect()
            )

            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                ShimmerInfoItem(Modifier.weight(1f), alignEnd = false)
                ShimmerInfoItem(Modifier.weight(1f), alignEnd = true)
            }

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                ShimmerInfoItem(Modifier.weight(1f), alignEnd = false)
                ShimmerInfoItem(Modifier.weight(1f), alignEnd = true)
            }
        }
    }
}

@Composable
private fun ShimmerInfoItem(
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false
) {
    Row(
        modifier = modifier,
        horizontalArrangement = if (alignEnd) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(12.sp.value.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )
        Spacer(Modifier.width(4.dp))
        Box(
            modifier = Modifier
                .width(70.dp)
                .height(12.sp.value.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )
    }
}

