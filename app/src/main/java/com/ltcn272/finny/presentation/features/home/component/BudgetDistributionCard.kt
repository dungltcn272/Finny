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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp
import com.ltcn272.finny.R
import com.ltcn272.finny.presentation.common.ui.ChartLegend
import com.ltcn272.finny.presentation.common.ui.DonutChart
import com.ltcn272.finny.presentation.common.ui.DonutChartWaiting
import com.ltcn272.finny.presentation.common.ui.DonutData
import com.ltcn272.finny.presentation.common.ui.shimmerEffect

@Composable
fun BudgetDistributionCard(
    reportData: List<DonutData>,
    totalAmount: Double,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp
    ) {
        Column {
            Text(
                stringResource(
                    id = R.string.budget_distribution
                ),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 12.dp, top = 15.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                DonutChart(
                    items = reportData,
                    totalLabel = stringResource(R.string.total_spent),
                    totalAmount = totalAmount,
                    modifier = Modifier.weight(1f)
                )


                ChartLegend(
                    items = reportData,
                    maxVisibleItems = 3,
                    modifier = Modifier.weight(0.5f)
                )

                Spacer(modifier.weight(0.1f))
            }
        }
    }
}

@Composable
fun BudgetDistributionCardShimmer(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp
    ) {
        Column {
            // Title shimmer
            Box(
                modifier = Modifier
                    .padding(start = 12.dp, top = 15.dp, bottom = 8.dp)
                    .height(20.dp)
                    .width(150.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Donut Chart Waiting
                DonutChartWaiting(
                    modifier = Modifier.weight(1f)
                )

                // Legend Shimmer
                Column(
                    modifier = Modifier.weight(0.5f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(3) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .shimmerEffect()
                            )
                            Spacer(Modifier.width(8.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .height(14.dp)
                                        .width(60.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .shimmerEffect()
                                )
                                Box(
                                    modifier = Modifier
                                        .height(12.dp)
                                        .width(40.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .shimmerEffect()
                                )
                            }
                        }
                    }
                }
                Spacer(modifier.weight(0.1f))
            }
        }
    }
}
