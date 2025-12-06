package com.ltcn272.finny.presentation.features.transation.create_transaction.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.R
import com.ltcn272.finny.presentation.common.util.CurrencyUtils
import com.ltcn272.finny.presentation.features.transation.create_transaction.CreateTransactionFormState

@Composable
fun TransactionDetailsForm(
    formState: CreateTransactionFormState,
    selectedBudgetName: String?,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onBudgetClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
    ) {
        // --- Name Input ---
        BasicTextField(
            value = formState.name,
            onValueChange = onNameChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.headlineMedium.copy(
                color = LocalContentColor.current,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            ),
            cursorBrush = SolidColor(LocalContentColor.current),
            modifier = Modifier.fillMaxWidth(0.6f),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (formState.name.isEmpty()) {
                        Text(
                            text = stringResource(id = R.string.enter_name),
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                    innerTextField()
                }
            }
        )
        Spacer(modifier = Modifier.height(20.dp))

        // --- Amount Display ---
        AmountDisplay(
            currencySymbol = CurrencyUtils.getCurrencySymbolForCurrentLocale(),
            amount = formState.amount,
        )

        Spacer(modifier = Modifier.height(20.dp))

        // --- Budget Selection ---
        BudgetSurface(
            budgetName = selectedBudgetName ?: "",
            onClick = onBudgetClick
        )

        Spacer(modifier = Modifier.height(20.dp))

        // --- Description Input ---
        BasicTextField(
            value = formState.description,
            onValueChange = onDescriptionChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = LocalContentColor.current,
                textAlign = TextAlign.Center
            ),
            cursorBrush = SolidColor(LocalContentColor.current),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (formState.description.isEmpty()) {
                        Text(
                            text = stringResource(id = R.string.enter_description),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}