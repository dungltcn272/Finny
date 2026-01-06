package com.ltcn272.finny.presentation.features.dashboard.report.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.R
import com.ltcn272.finny.presentation.common.ui.shimmerEffect
import com.ltcn272.finny.presentation.common.util.formatCurrency
import com.ltcn272.finny.presentation.common.util.formatCurrencyShortVietnamese
import kotlin.math.abs

@Composable
internal fun TotalStatsCard(
    modifier: Modifier = Modifier,
    label: String,
    amount: Double,
    change: Double,
    isIncome: Boolean
) {
    val primaryColor = if (isIncome) Color(0xFF2E7D32) else Color(0xFFC62828)

    val secondaryColor = if (isIncome) primaryColor.copy(alpha = 0.8f) else Color(0xFFE65100)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(primaryColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = primaryColor,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = formatCurrency(amount, "VND"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 2.dp)
            )

            val changeText = if (change >= 0) {
                stringResource(R.string.change_vs_period_up, formatCurrencyShortVietnamese(change))
            } else {
                stringResource(R.string.change_vs_period_down, formatCurrencyShortVietnamese(abs(change)))
            }
            Text(
                text = changeText,
                style = MaterialTheme.typography.bodySmall,
                color = secondaryColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


@Composable
internal fun TotalStatsCardShimmer(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(modifier = Modifier.size(20.dp).clip(CircleShape).shimmerEffect())
                Box(modifier = Modifier.height(14.dp).width(80.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            }
            Box(modifier = Modifier.height(28.dp).width(130.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            Box(modifier = Modifier.height(12.dp).width(100.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
        }
    }
}
