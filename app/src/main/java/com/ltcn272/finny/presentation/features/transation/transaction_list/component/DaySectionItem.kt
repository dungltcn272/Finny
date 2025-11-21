package com.ltcn272.finny.presentation.features.transation.transaction_list.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.domain.model.DaySection
import com.ltcn272.finny.presentation.common.ui.CreateNewButton
import com.ltcn272.finny.presentation.common.ui.TransactionItem
import java.time.LocalDate

@Composable
fun DaySectionItem(
    daySection: DaySection,
    currency: String,
    onCreateNewClick: () -> Unit,
    onTransactionClick: (String) -> Unit // Passes transaction ID
) {
    Column {
        DaySectionHeader(
            daySection = daySection,
            onAdd = onCreateNewClick
        )

        if (daySection.date == LocalDate.now()) {
            CreateNewButton(onClick = onCreateNewClick, modifier = Modifier.padding(vertical = 10.dp))
        }

        if (daySection.transactions.isNotEmpty()) {
            Column {
                daySection.transactions.forEach { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        currency = currency,
                        onClick = { onTransactionClick(transaction.id) }
                    )
                }
            }
        }
    }
}