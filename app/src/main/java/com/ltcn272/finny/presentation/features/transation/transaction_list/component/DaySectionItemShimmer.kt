package com.ltcn272.finny.presentation.features.transation.transaction_list.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.presentation.common.ui.shimmerEffect

@Composable
fun DaySectionItemShimmer(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        // Header shimmer - Matches DaySectionItem header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp), // Match padding
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .width(120.dp).clip(RoundedCornerShape(10.dp))
                    .shimmerEffect()
            )
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .width(90.dp).clip(RoundedCornerShape(10.dp))
                    .shimmerEffect()
            )
        }

        // A Column to hold individual transaction shimmer cards
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp) // Spacing between each transaction card
        ) {
            // Shimmer for 2 transaction items, each in its own card
            repeat(2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp) // Same as TransactionItem icon
                            .clip(CircleShape)
                            .shimmerEffect()
                    )
                    Spacer(modifier = Modifier.width(16.dp)) // Same as TransactionItem
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .height(18.dp)
                                .fillMaxWidth(0.8f) // For transaction name
                                .shimmerEffect()
                        )
                        Box(
                            modifier = Modifier
                                .height(14.dp)
                                .fillMaxWidth(0.5f) // For category
                                .shimmerEffect()
                        )
                    }
                    Box(
                        modifier = Modifier
                            .height(22.dp)
                            .fillMaxWidth(0.25f) // For amount
                            .shimmerEffect()
                    )
                }
            }
        }
    }
}