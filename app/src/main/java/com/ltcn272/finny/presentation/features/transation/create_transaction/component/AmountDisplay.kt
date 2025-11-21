package com.ltcn272.finny.presentation.features.transation.create_transaction.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ltcn272.finny.presentation.common.util.formatAmountWithSeparators

@Composable
fun AmountDisplay(
    currencySymbol: String = "$",
    amount: String = "5.99",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = currencySymbol,
            color = Color.Gray.copy(alpha = 0.8f),
            fontSize = 32.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.offset(y = 8.dp)
        )

        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = formatAmountWithSeparators(amount),
            color = Color.Black,
            fontSize = 54.sp,
            maxLines = 1,
            fontWeight = FontWeight.Bold,
            overflow = TextOverflow.StartEllipsis
        )
    }
}