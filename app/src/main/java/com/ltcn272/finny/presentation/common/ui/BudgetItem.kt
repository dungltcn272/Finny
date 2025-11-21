package com.ltcn272.finny.presentation.common.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.domain.model.BudgetDetails
import com.ltcn272.finny.domain.model.BudgetPeriod
import com.ltcn272.finny.presentation.common.util.formatCurrency
import java.time.ZonedDateTime

@Composable
fun BudgetItem(budgetDetails: BudgetDetails, currency: String, onClick: () -> Unit) {
    val spent = budgetDetails.spentAmount
    val limit = budgetDetails.budget.limit
    val remain = limit - spent
    // Progress can be > 1.0 if overspent
    val progress = if (limit > 0) (spent / limit).toFloat() else 0f
    // For the UI, we cap the progress bar at 100% (or 1.0f)
    val displayProgress = progress.coerceIn(0f, 1f)
    val percentage = (progress * 100).toInt()

    val progressColor = when {
        progress >= 1f -> Color(0xFFD32F2F) // Dark Red
        progress > 0.75f -> Color(0xFFEF5350) // Light Red
        progress > 0.5f -> Color(0xFFFFC107)  // Amber Yellow
        else -> Color(0xFF26A69A) // Teal
    }
    val percentageColor = if (progress >= 1f) progressColor else Color.Gray

    val isOverspent = remain < 0
    val remainText = if (isOverspent) {
        "Overspent: ${formatCurrency(-remain, currency)}"
    } else {
        "Remain: ${formatCurrency(remain, currency)}"
    }
    val remainTextColor = if (isOverspent) progressColor else Color.Gray

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = budgetDetails.budget.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = percentageColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Custom Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(displayProgress)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(progressColor)

                )
            }


            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = remainText,
                    style = MaterialTheme.typography.bodySmall,
                    color = remainTextColor
                )
                Text(
                    text = "Limit: ${formatCurrency(limit, currency)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Low Usage")
@Composable
fun BudgetItemPreviewLow() {
    BudgetItem(
        budgetDetails = BudgetDetails(
            budget = Budget(
                id = "1", name = "Ăn uống", userId = "user1",
                startDate = ZonedDateTime.now(), limit = 2500000.0, period = BudgetPeriod.ONE_MONTH,
                createdAt = ZonedDateTime.now(), updatedAt = ZonedDateTime.now()
            ),
            spentAmount = 250000.0
        ),
        currency = "vnd",
        onClick = {}
    )
}

@Preview(showBackground = true, name = "Medium Usage")
@Composable
fun BudgetItemPreviewMedium() {
    BudgetItem(
        budgetDetails = BudgetDetails(
            budget = Budget(
                id = "1", name = "Giải trí", userId = "user1",
                startDate = ZonedDateTime.now(), limit = 2500000.0, period = BudgetPeriod.ONE_MONTH,
                createdAt = ZonedDateTime.now(), updatedAt = ZonedDateTime.now()
            ),
            spentAmount = 1500000.0
        ),
        currency = "vnd",
        onClick = {}
    )
}

@Preview(showBackground = true, name = "High Usage")
@Composable
fun BudgetItemPreviewHigh() {
    BudgetItem(
        budgetDetails = BudgetDetails(
            budget = Budget(
                id = "1", name = "Mua sắm", userId = "user1",
                startDate = ZonedDateTime.now(), limit = 2500000.0, period = BudgetPeriod.ONE_MONTH,
                createdAt = ZonedDateTime.now(), updatedAt = ZonedDateTime.now()
            ),
            spentAmount = 2200000.0
        ),
        currency = "vnd",
        onClick = {}
    )
}

@Preview(showBackground = true, name = "Over Limit")
@Composable
fun BudgetItemPreviewOver() {
    BudgetItem(
        budgetDetails = BudgetDetails(
            budget = Budget(
                id = "1", name = "Du lịch", userId = "user1",
                startDate = ZonedDateTime.now(), limit = 2500000.0, period = BudgetPeriod.ONE_MONTH,
                createdAt = ZonedDateTime.now(), updatedAt = ZonedDateTime.now()
            ),
            spentAmount = 2700000.0
        ),
        currency = "vnd",
        onClick = {}
    )
}
