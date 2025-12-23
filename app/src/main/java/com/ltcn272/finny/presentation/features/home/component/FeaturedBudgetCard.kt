package com.ltcn272.finny.presentation.features.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.presentation.common.ui.FinnyProgressIndicator
import com.ltcn272.finny.presentation.common.ui.shimmerEffect
import com.ltcn272.finny.presentation.common.util.formatCurrency

@Composable
fun FeaturedBudgetCard(
    budget: Budget,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            // --- REDUCE SPACING HERE ---
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = budget.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = formatCurrency(budget.amount, budget.currency),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = stringResource(
                    R.string.left_to_spend_of_limit,
                    formatCurrency(budget.limit, budget.currency)
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(6.dp)) // Reduce spacer

            FinnyProgressIndicator(
                progress = budget.progress.toFloat() / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            )

            Spacer(Modifier.height(2.dp)) // Reduce spacer

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val daysRemaining = budget.daysRemaining
                if (daysRemaining != null) {
                    Text(
                        text = stringResource(R.string.days_to_go, daysRemaining.toInt()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val savedAmount = budget.limit - budget.amount
                if (savedAmount > 0) {
                    Text(
                        text = stringResource(
                            R.string.saved_amount,
                            formatCurrency(savedAmount, budget.currency)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(6.dp)) // Reduce spacer

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoChip(
                    label = stringResource(R.string.incoming),
                    amount = formatCurrency(budget.totalIncome, budget.currency),
                    modifier = Modifier.weight(1f)
                )
                InfoChip(
                    label = stringResource(R.string.outcoming),
                    amount = formatCurrency(budget.totalOutcome, budget.currency),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun InfoChip(
    label: String,
    amount: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
        Text(
            text = amount,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun FeaturedBudgetCardShimmer(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Title
            Box(modifier = Modifier.height(20.dp).fillMaxWidth(0.4f).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            Spacer(Modifier.height(2.dp))
            // Amount
            Box(modifier = Modifier.height(36.dp).fillMaxWidth(0.7f).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            // Limit
            Box(modifier = Modifier.height(16.dp).fillMaxWidth(0.5f).clip(RoundedCornerShape(4.dp)).shimmerEffect())

            Spacer(Modifier.height(6.dp))

            // Progress bar
            Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())

            Spacer(Modifier.height(2.dp))

            // Days & Saved
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(modifier = Modifier.height(12.dp).width(80.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                Box(modifier = Modifier.height(12.dp).width(100.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            }

            Spacer(Modifier.height(6.dp))

            // Info Chips
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f).height(60.dp).clip(RoundedCornerShape(12.dp)).shimmerEffect())
                Box(modifier = Modifier.weight(1f).height(60.dp).clip(RoundedCornerShape(12.dp)).shimmerEffect())
            }
        }
    }
}