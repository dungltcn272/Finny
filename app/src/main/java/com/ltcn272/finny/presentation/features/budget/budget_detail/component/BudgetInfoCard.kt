package com.ltcn272.finny.presentation.features.budget.budget_detail.component

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.presentation.common.ui.FinnyProgressIndicator
import com.ltcn272.finny.presentation.common.ui.shimmerEffect
import com.ltcn272.finny.presentation.common.util.formatCurrencyNonComposable

@Composable
internal fun BudgetInfoCard(
    budget: Budget,
    transactionCount: Int,
    netAmount: Double,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val transactionCountText = remember(transactionCount) {
        context.resources.getQuantityString(R.plurals.transaction_count, transactionCount, transactionCount)
    }
    val currency = budget.currency

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = budget.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            Text(
                text = formatCurrencyNonComposable(netAmount, currency),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = if (netAmount >= 0) Color(0xFF2E7D32) else Color(0xFFD32F2F)
            )

            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SyncAlt,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = transactionCountText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }


            Spacer(modifier = Modifier.height(16.dp))

            FinnyProgressIndicator(
                progress = budget.progress.toFloat() / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.spent_label, formatCurrencyNonComposable(budget.totalOutcome, currency)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.limit_label, formatCurrencyNonComposable(budget.limit, currency)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }

            budget.recurring?.let { recurring ->
                val intervalUnitString = when(recurring.unit) {
                    com.ltcn272.finny.domain.model.RecurringIntervalUnit.DAY -> stringResource(R.string.unit_day_lowercase)
                    com.ltcn272.finny.domain.model.RecurringIntervalUnit.WEEK -> stringResource(R.string.unit_week_lowercase)
                    com.ltcn272.finny.domain.model.RecurringIntervalUnit.MONTH -> stringResource(R.string.unit_month_lowercase)
                    com.ltcn272.finny.domain.model.RecurringIntervalUnit.YEAR -> stringResource(R.string.unit_year_lowercase)
                }
                val recurringText = stringResource(
                    R.string.recurring_topup_info,
                    formatCurrencyNonComposable(recurring.topupAmount, currency),
                    recurring.value,
                    intervalUnitString
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = recurringText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun BudgetInfoCardShimmer(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(modifier = Modifier.height(20.dp).width(120.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.height(40.dp).width(200.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            Spacer(Modifier.height(4.dp))
            Box(modifier = Modifier.height(16.dp).width(100.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier.height(14.dp).width(90.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                Box(modifier = Modifier.height(14.dp).width(110.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            }
        }
    }
}
