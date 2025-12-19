package com.ltcn272.finny.presentation.common.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.presentation.common.util.rememberCurrencyFormatter

@Composable
fun BudgetItem(
    budget: Budget,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val formatter = rememberCurrencyFormatter(budget.currency)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable { onClick() }
                else Modifier
            ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E5E5)),
        color = Color.White,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            /** HEADER **/
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = budget.name,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = stringResource(
                        R.string.budget_percent,
                        budget.progress
                    ),
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(10.dp))

            /** PROGRESS **/
            FinnyProgressIndicator(
                progress = budget.progress / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFF2EC4B6),
                trackColor = Color(0xFFEAEAEA)
            )

            Spacer(Modifier.height(12.dp))

            /** ROW 1 **/
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                BudgetInfo(
                    label = stringResource(R.string.budget_remaining),
                    value = formatter.format(budget.amount),
                    modifier = Modifier.weight(1f)
                )

                BudgetInfo(
                    label = stringResource(R.string.budget_limit),
                    value = formatter.format(budget.limit),
                    modifier = Modifier.weight(1f),
                    alignEnd = true
                )
            }

            Spacer(Modifier.height(8.dp))

            /** ROW 2 **/
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                BudgetInfo(
                    label = stringResource(R.string.budget_total_income),
                    value = formatter.format(budget.totalIncome),
                    modifier = Modifier.weight(1f)
                )

                BudgetInfo(
                    label = stringResource(R.string.budget_total_outcome),
                    value = formatter.format(budget.totalOutcome),
                    modifier = Modifier.weight(1f),
                    alignEnd = true
                )
            }

            /** RECURRING **/
            if (budget.recurring != null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.budget_recurring_topup),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun BudgetInfo(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (alignEnd)
            Alignment.End else Alignment.Start
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Text(
            text = value,
            fontWeight = FontWeight.SemiBold
        )
    }
}

