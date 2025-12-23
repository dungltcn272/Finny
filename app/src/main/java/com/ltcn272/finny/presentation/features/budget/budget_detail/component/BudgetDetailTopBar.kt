package com.ltcn272.finny.presentation.features.budget.budget_detail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.presentation.common.ui.CircleNavigationButton

@Composable
internal fun BudgetDetailTopBar(
    onBack: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleNavigationButton(icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft, onClick = onBack)
        CircleNavigationButton(icon = Icons.Default.MoreVert, onClick = onMoreClick)
    }
}
