package com.ltcn272.finny.presentation.features.dashboard.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.presentation.common.ui.FinnyProgressIndicator
import com.ltcn272.finny.presentation.common.ui.shimmerEffect

@Composable
internal fun ReportDetailItem(
    modifier: Modifier = Modifier,
    name: String,
    amountText: String,
    progress: Float
) {
    val progressColor = remember(progress) {
        when {
            progress < 0 -> Color.Blue
            progress < 0.5f -> Color(0xFF42A5F5)
            progress < 0.85f -> Color(0xFFFFCA28)
            progress < 1.0f -> Color(0xFFFFA726)
            else -> Color(0xFFEF5350)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = amountText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (progress >= 0) {
            FinnyProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
@Composable
internal fun ReportDetailItemShimmer(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Name and Amount Shimmer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.height(18.dp).fillMaxWidth(0.4f).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            Box(modifier = Modifier.height(16.dp).fillMaxWidth(0.3f).clip(RoundedCornerShape(4.dp)).shimmerEffect())
        }
        // Progress Bar Shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .shimmerEffect()
        )
    }
}