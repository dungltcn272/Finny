package com.ltcn272.finny.presentation.features.transaction.transaction_list.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.presentation.common.ui.SliderFilterRow

@Composable
fun BudgetFilterRow(
    budgets: List<Budget>,
    selectedBudget: Budget?,
    onBudgetSelected: (Budget?) -> Unit,
    onSettingClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        SliderFilterRow(
            modifier = Modifier.weight(1f),
            items = budgets,
            selectedItem = selectedBudget,
            onItemSelected = onBudgetSelected,
            itemToString = { it.name },
            selectedBg = MaterialTheme.colorScheme.primary,
            unselectedBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            selectedText = MaterialTheme.colorScheme.onPrimary,
            unselectedText = MaterialTheme.colorScheme.onSurfaceVariant
        )


        IconButton(onClick = onSettingClick) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
