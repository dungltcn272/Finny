package com.ltcn272.finny.presentation.features.budget.create_budget

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.BudgetPeriod
import com.ltcn272.finny.presentation.common.ui.SegmentedControl

@Composable
fun PeriodSegmentedControl(
    value: BudgetPeriod,
    onValueChange: (BudgetPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    val periods = listOf(BudgetPeriod.SINGLE, BudgetPeriod.ONE_WEEK, BudgetPeriod.ONE_MONTH, BudgetPeriod.ONE_YEAR)

    SegmentedControl(
        modifier = modifier,
        options = periods,
        selected = value,
        onOptionClicked = onValueChange,
        titleForItem = { period ->
            when (period) {
                BudgetPeriod.SINGLE -> stringResource(id = R.string.single)
                BudgetPeriod.ONE_WEEK -> stringResource(id = R.string.one_week)
                BudgetPeriod.ONE_MONTH -> stringResource(id = R.string.one_month)
                BudgetPeriod.ONE_YEAR -> stringResource(id = R.string.one_year)
                else -> ""
            }
        },
        indicatorPadding = 2.dp
    )
}