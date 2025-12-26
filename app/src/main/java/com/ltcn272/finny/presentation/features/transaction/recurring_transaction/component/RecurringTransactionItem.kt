package com.ltcn272.finny.presentation.features.transaction.recurring_transaction.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.RecurringIntervalUnit
import com.ltcn272.finny.domain.model.RecurringTransaction
import com.ltcn272.finny.domain.model.TransactionType
import com.ltcn272.finny.presentation.common.ui.shimmerEffect
import com.ltcn272.finny.presentation.common.util.formatCurrency
import com.ltcn272.finny.presentation.common.util.formatZonedDateTimeFull

@Composable
fun RecurringTransactionItem(
    transaction: RecurringTransaction,
    modifier: Modifier = Modifier,
    currencyCode: String,
    onClick: (() -> Unit) = {}
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val amountColor = if (isIncome) Color(0xFF2E7D32) else Color(0xFFC62828)
    val sign = if (isIncome) "+" else "-"

    val intervalText = when (transaction.intervalUnit) {
        RecurringIntervalUnit.DAY -> stringResource(id = R.string.unit_day_lowercase)
        RecurringIntervalUnit.WEEK -> stringResource(id = R.string.unit_week_lowercase)
        RecurringIntervalUnit.MONTH -> stringResource(id = R.string.unit_month_lowercase)
        RecurringIntervalUnit.YEAR -> stringResource(id = R.string.unit_year_lowercase)
    }
    val recurringInfo = if (transaction.intervalValue == 1) {
        stringResource(id = R.string.every_interval, intervalText)
    } else {
        stringResource(id = R.string.every_n_interval, transaction.intervalValue, intervalText)
    }

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f)),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val statusColor = if (transaction.active) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(statusColor, CircleShape)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = transaction.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        transaction.category?.name?.let { categoryName ->
                            Text(
                                text = categoryName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "$sign${formatCurrency(transaction.amount, currencyCode)}",
                    color = amountColor,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoChip(
                    icon = { Icon(Icons.Default.Sync, null, modifier = Modifier.size(16.dp)) },
                    text = { Text(recurringInfo, style = MaterialTheme.typography.bodySmall) }
                )

                InfoChip(
                    icon = { Icon(Icons.Default.Schedule, null, modifier = Modifier.size(16.dp)) },
                    text = {
                        Text(
                            text = transaction.nextRunAt?.let { formatZonedDateTimeFull(it) }
                                ?: stringResource(R.string.n_a),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun InfoChip(
    icon: @Composable () -> Unit,
    text: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    borderColor: Color = Color.Transparent
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            icon()
            text()
        }
    }
}


@Composable
fun RecurringTransactionItemShimmer(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0)),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top row shimmer
            Row(
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.fillMaxWidth(0.7f).height(20.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    Box(modifier = Modifier.fillMaxWidth(0.4f).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                }
                Spacer(modifier = Modifier.width(12.dp))
                Box(modifier = Modifier.width(80.dp).height(24.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

            // Bottom row shimmer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.height(26.dp).width(90.dp).clip(RoundedCornerShape(50.dp)).shimmerEffect())
                Box(modifier = Modifier.height(26.dp).width(120.dp).clip(RoundedCornerShape(50.dp)).shimmerEffect())
                Box(modifier = Modifier.height(26.dp).width(80.dp).clip(RoundedCornerShape(50.dp)).shimmerEffect())
            }
        }
    }
}
