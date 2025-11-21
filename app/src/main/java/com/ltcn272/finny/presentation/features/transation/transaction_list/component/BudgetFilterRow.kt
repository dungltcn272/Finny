package com.ltcn272.finny.presentation.features.transation.transaction_list.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.R
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        SliderFilterRow(
            modifier = Modifier.weight(1f),
            items = budgets,
            selectedItem = selectedBudget,
            onItemSelected = onBudgetSelected,
            itemToString = { it.name }
        )

        Surface(
            modifier = Modifier.size(24.dp),
            shape = CircleShape,
            color = Color.Transparent,
            onClick = onSettingClick
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_settings),
                contentDescription = "Budget Settings",
                tint = null
            )
        }
    }
}