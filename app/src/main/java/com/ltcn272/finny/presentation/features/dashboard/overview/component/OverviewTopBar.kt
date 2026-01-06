package com.ltcn272.finny.presentation.features.dashboard.overview.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.R
import com.ltcn272.finny.presentation.common.ui.SegmentedControl
import com.ltcn272.finny.presentation.features.dashboard.OverviewPeriod

@Composable
fun OverviewTopBar(
    modifier: Modifier = Modifier,
    selectedPeriod: OverviewPeriod,
    onPeriodSelected: (OverviewPeriod) -> Unit,
) {
    val periods = OverviewPeriod.entries
    val periodTitles = mapOf(
        OverviewPeriod.WEEK to stringResource(R.string.week),
        OverviewPeriod.MONTH to stringResource(R.string.month)
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.financial_overview),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        SegmentedControl(
            options = periods,
            selected = selectedPeriod,
            onOptionClicked = onPeriodSelected,
            titleForItem = { periodTitles[it] ?: "" },
            containerColor = Color.White,
            indicatorColor = Color.Black,
            selectedTextColor = Color.White,
            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            indicatorPadding = 2.dp,
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
            modifier = Modifier.width(120.dp)
        )
    }
}
