package com.ltcn272.finny.presentation.features.dashboard.overview.component

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.R
import com.ltcn272.finny.presentation.common.ui.FinnyProgressIndicator
import com.ltcn272.finny.presentation.common.ui.shimmerEffect
import com.ltcn272.finny.presentation.common.util.formatCurrency

@Composable
fun BudgetSummaryItem(
    modifier: Modifier = Modifier,
    name: String,
    spent: Double,
    limit: Double,
    usagePercent: Int
) {
    val progress = if (limit > 0) (spent / limit).toFloat() else 0f
    val isOverspent = usagePercent > 100

    val (progressColor, chipColor, chipTextColor) = remember(usagePercent) {
        when {
            usagePercent > 100 -> Triple(
                Color(0xFFDC3545), 
                Color(0xFFDC3545), 
                Color.White
            )
            usagePercent >= 90 -> Triple(
                Color(0xFFFD7E14), 
                Color(0xFFFD7E14).copy(alpha = 0.15f),
                Color(0xFFFD7E14)
            )
            usagePercent >= 80 -> Triple(
                Color(0xFFFFC107), 
                Color(0xFFFFC107).copy(alpha = 0.2f),
                Color(0xFFB38600)
            )
            else -> Triple(
                Color(0xFF28A745), 
                Color(0xFF28A745).copy(alpha = 0.15f),
                Color(0xFF28A745)
            )
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.01f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${formatCurrency(spent, "VND")} / ${formatCurrency(limit, "VND")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = chipColor
                ) {
                    Text(
                        text = if (isOverspent) stringResource(R.string.overspent_tag) else "$usagePercent%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = chipTextColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            FinnyProgressIndicator(
                progress = progress.coerceIn(0f, 1f),
                modifier = Modifier
                    .height(8.dp)
                    .fillMaxWidth(),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
fun BudgetSummaryItemShimmer(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(18.dp)
                            .shimmerEffect()
                    )
                    Box(
                        modifier = Modifier
                            .width(160.dp)
                            .height(14.dp)
                            .shimmerEffect()
                    )
                }
                Box(
                    modifier = Modifier
                        .width(50.dp)
                        .height(24.dp).clip(CircleShape)
                        .shimmerEffect()
                )
            }
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .shimmerEffect()
            )
        }
    }
}
