package com.ltcn272.finny.presentation.features.home.component

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.presentation.common.ui.shimmerEffect

/**
 * Composable chính chứa toàn bộ giao diện shimmer cho HomeScreen
 */
@Composable
fun HomeScreenShimmer(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Shimmer for HomeHeader
        item {
            HomeHeaderShimmer()
        }

        // 2. Shimmer for FeaturedBudgetCard
        item {
            FeaturedBudgetCardShimmer()
        }

        // 3. Shimmer for BudgetDistributionCard
        item {
            BudgetDistributionCardShimmer()
        }

        // 4. Shimmer for "Your Budgets" List
        item {
            ListHeaderShimmer()
        }
        items(2) { // Hiển thị 2 item shimmer cho danh sách budget
            BudgetItemShimmer()
        }

        // 5. Shimmer for "Latest Transactions" List
        item {
            ListHeaderShimmer()
        }
        items(3) { // Hiển thị 3 item shimmer cho danh sách transaction
            TransactionItemShimmer()
        }
    }
}

/**
 * Mô phỏng HomeHeader
 */
@Composable
private fun HomeHeaderShimmer() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // Lời chào và ngày tháng
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .height(28.dp)
                    .width(180.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .shimmerEffect()
            )
            Box(
                modifier = Modifier
                    .height(16.dp)
                    .width(100.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .shimmerEffect()
            )
        }
        // Các nút actions
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).shimmerEffect())
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).shimmerEffect())
        }
    }
}

/**
 * Mô phỏng FeaturedBudgetCard
 */
@Composable
private fun FeaturedBudgetCardShimmer() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .shimmerEffect()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(modifier = Modifier.height(20.dp).width(120.dp).clip(RoundedCornerShape(6.dp)).shimmerEffect())
        Box(modifier = Modifier.height(40.dp).width(200.dp).clip(RoundedCornerShape(8.dp)).shimmerEffect())
        Box(modifier = Modifier.height(18.dp).width(250.dp).clip(RoundedCornerShape(6.dp)).shimmerEffect())
        Spacer(Modifier.height(8.dp))
        Box(modifier = Modifier.height(8.dp).fillMaxWidth().clip(RoundedCornerShape(4.dp)).shimmerEffect())
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Box(modifier = Modifier.height(16.dp).width(80.dp).clip(RoundedCornerShape(6.dp)).shimmerEffect())
            Box(modifier = Modifier.height(16.dp).width(100.dp).clip(RoundedCornerShape(6.dp)).shimmerEffect())
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f).height(60.dp).clip(RoundedCornerShape(12.dp)).shimmerEffect())
            Box(modifier = Modifier.weight(1f).height(60.dp).clip(RoundedCornerShape(12.dp)).shimmerEffect())
        }
    }
}

/**
 * Mô phỏng BudgetDistributionCard
 */
@Composable
private fun BudgetDistributionCardShimmer() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(24.dp))
            .shimmerEffect()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(150.dp).clip(CircleShape).shimmerEffect())
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(3) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).shimmerEffect())
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.height(18.dp).width(80.dp).clip(RoundedCornerShape(6.dp)).shimmerEffect())
                        Box(modifier = Modifier.height(14.dp).width(60.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    }
                }
            }
        }
    }
}

/**
 * Mô phỏng ListHeader
 */
@Composable
private fun ListHeaderShimmer() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.height(24.dp).width(150.dp).clip(RoundedCornerShape(8.dp)).shimmerEffect())
        Box(modifier = Modifier.height(20.dp).width(70.dp).clip(RoundedCornerShape(6.dp)).shimmerEffect())
    }
}

/**
 * Mô phỏng BudgetItem
 */
@Composable
private fun BudgetItemShimmer() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(20.dp))
            .shimmerEffect()
    )
}

/**
 * Mô phỏng TransactionItem
 */
@Composable
private fun TransactionItemShimmer() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clip(RoundedCornerShape(16.dp))
            .shimmerEffect()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(44.dp).clip(CircleShape).shimmerEffect())
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.height(20.dp).fillMaxWidth(0.7f).clip(RoundedCornerShape(6.dp)).shimmerEffect())
            Box(modifier = Modifier.height(14.dp).fillMaxWidth(0.4f).clip(RoundedCornerShape(4.dp)).shimmerEffect())
        }
        Box(modifier = Modifier.height(22.dp).width(80.dp).clip(RoundedCornerShape(6.dp)).shimmerEffect())
    }
}
