package com.ltcn272.finny.presentation.features.budget.create_budget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastCbrt
import androidx.compose.ui.util.trace
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.BudgetPeriod
import com.ltcn272.finny.presentation.theme.BudgetAmountCardBackground
import com.ltcn272.finny.presentation.theme.BudgetName
import com.ltcn272.finny.presentation.theme.BudgetIconTint
import com.ltcn272.finny.presentation.theme.BudgetPeriodBackground
import com.ltcn272.finny.presentation.theme.BudgetSectionBackground
import com.ltcn272.finny.presentation.theme.BudgetSecondaryText
import com.ltcn272.finny.presentation.theme.BudgetTitle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Preview
@Composable
fun BudgetFormPreview() {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("650") }
    var period by remember { mutableStateOf(BudgetPeriod.ONE_MONTH) }
    BudgetForm(
        isEditMode = true,
        name = name,
        onNameChange = { name = it },
        amount = amount,
        onAmountChange = { amount = it },
        dateTimeMillis = null,
        onOpenDateTimePicker = {},
        period = period,
        onPeriodChange = { period = it },
        modifier = Modifier.padding(16.dp),
        currencySymbol = "$"
    )
}

@Composable
fun BudgetForm(
    isEditMode: Boolean,
    name: String,
    onNameChange: (String) -> Unit,
    amount: String,
    onAmountChange: (String) -> Unit,
    dateTimeMillis: Long?,
    onOpenDateTimePicker: () -> Unit,
    period: BudgetPeriod,
    onPeriodChange: (BudgetPeriod) -> Unit,
    modifier: Modifier = Modifier,
    currencySymbol: String = "$"
) {

    val dateLabel = dateTimeMillis?.let {
        val fmt = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        fmt.format(Date(it))
    } ?: "Add Datetime"

    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val title = if (isEditMode) "Update Budget" else "Create Budget"
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = BudgetTitle)
        Spacer(Modifier.height(8.dp))

        // Budget Name (hoisted)
        BasicTextField(
            value = name,
            onValueChange = onNameChange,
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = BudgetTitle
            ),
            decorationBox = { inner ->
                Box(Modifier.wrapContentSize(), contentAlignment = Alignment.Center) {
                    if (name.isEmpty()) Text(
                        "Budget Name",
                        color = BudgetName,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    inner()
                }
            },
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight()
        )

        Spacer(Modifier.height(30.dp))

        // Amount card (hoisted amount)
        Surface(
            color = BudgetAmountCardBackground,
            shape = RoundedCornerShape(18.dp),
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.width(IntrinsicSize.Min)
                ) {
                    Text(
                        currencySymbol,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BudgetTitle
                    )
                    Spacer(Modifier.width(2.dp))
                    BasicTextField(
                        value = amount,
                        onValueChange = { input ->
                            onAmountChange(input.filter { it.isDigit() || it == '.' })
                        },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 34.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Start,
                            color = BudgetTitle
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.width(IntrinsicSize.Min)
                    )
                }
            }
        }

        // Date row (hoisted dateTimeMillis)
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)).background(Color.White)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painterResource(id = R.drawable.ic_calendar), contentDescription = null, tint = BudgetIconTint)
            Spacer(Modifier.width(10.dp))
            Text("Datetime", color = BudgetTitle, modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .height(30.dp)
                    .background(BudgetPeriodBackground, RoundedCornerShape(4.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onOpenDateTimePicker() }
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) { Text(dateLabel, fontSize = 12.sp, color = BudgetTitle) }
        }

        // Period
        Spacer(Modifier.height(10.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BudgetSectionBackground, RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(id = R.drawable.ic_trending_up), contentDescription = null, tint = BudgetIconTint)
                Spacer(Modifier.width(6.dp))
                Text("Period", fontWeight = FontWeight.Medium, color = BudgetTitle)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "What period you pay for this transaction?",
                fontSize = 12.sp,
                color = BudgetSecondaryText
            )
            Spacer(Modifier.height(10.dp))

            PeriodSegmentedControl(
                value = period,
                onValueChange = onPeriodChange,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
