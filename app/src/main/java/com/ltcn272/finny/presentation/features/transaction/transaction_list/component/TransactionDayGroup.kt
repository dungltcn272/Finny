package com.ltcn272.finny.presentation.features.transaction.transaction_list.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.domain.model.DaySection
import com.ltcn272.finny.domain.model.Transaction
import com.ltcn272.finny.presentation.common.ui.TransactionItem
import com.ltcn272.finny.presentation.common.util.formatCurrency
import com.ltcn272.finny.presentation.common.util.formatZonedDateTimeFull
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun TransactionDayGroup(
    modifier: Modifier = Modifier,
    daySection: DaySection,
    currencyCode: String,
    onAddTransactionForDate: (LocalDate) -> Unit,
    onTransactionClick: (Transaction) -> Unit
) {

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TransactionGroupHeader(
            date = formatZonedDateTimeFull(daySection.date.atStartOfDay(ZoneId.systemDefault())),
            totalAmount = daySection.totalAmount,
            currencyCode = currencyCode,
            onAddClick = { onAddTransactionForDate(daySection.date) }
        )

        daySection.transactions.forEach { transaction ->
            TransactionItem(
                transaction = transaction,
                currencyCode = currencyCode,
                onClick = { onTransactionClick(transaction) }
            )
        }
    }
}

@Composable
private fun TransactionGroupHeader(
    date: String,
    totalAmount: Double,
    currencyCode: String,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPositive = totalAmount >= 0
    val amountColor = if (isPositive) Color(0xFF2E7D32) else Color(0xFFC62828)
    val sign = if (isPositive) "+" else ""

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            text = date,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        if (totalAmount != 0.0) {
            Text(
                text = "$sign${formatCurrency(totalAmount, currencyCode)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = amountColor,
                modifier = Modifier
                    .background(amountColor.copy(alpha = 0.1f), CircleShape)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        // Add Button
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .border(
                    BorderStroke(
                        2.dp,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    ),
                    CircleShape
                )
                .clickable(onClick = onAddClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

