package com.ltcn272.finny.presentation.features.transation.transaction_list.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.presentation.common.ui.shimmerEffect

@Composable
fun BudgetFilterRowShimmer(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Simulate a few chips with variable widths to better match the real UI
            val chipWidths = listOf(90.dp, 70.dp, 110.dp)
            chipWidths.forEach { chipWidth ->
                Box(
                    modifier = Modifier
                        .height(36.dp) // More accurate height for a chip with vertical padding
                        .width(chipWidth)
                        .clip(CircleShape)
                        .shimmerEffect()
                )
            }
        }
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .shimmerEffect()
        )
    }
}