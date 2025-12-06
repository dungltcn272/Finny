package com.ltcn272.finny.presentation.features.transation.create_transaction.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.presentation.features.transation.create_transaction.CreateTransactionFormState

@Composable
fun FooterControls(
    formState: CreateTransactionFormState,
    onDateTimeClick: () -> Unit,
    onCategoryClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // --- DateTime Selection ---
        DateTimeSurface(
            dateTime = formState.dateTime,
            onDateTimeClick = onDateTimeClick,
            modifier = Modifier.weight(6f)
        )

        // --- Category Selection ---
        CategorySurface(
            category = formState.category,
            onClick = onCategoryClick,
            modifier = Modifier.weight(2f)
        )
    }
}