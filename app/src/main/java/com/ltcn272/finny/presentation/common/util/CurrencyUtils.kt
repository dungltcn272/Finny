package com.ltcn272.finny.presentation.common.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.text.NumberFormat
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Currency

@Composable
fun formatCurrency(amount: Long, currencyCode: String): String {
    val formatter = rememberCurrencyFormatter(currencyCode = currencyCode)
    return formatter.format(amount)
}

@Composable
fun rememberCurrencyFormatter(currencyCode: String): NumberFormat {
    val locale = LocalConfiguration.current.locales[0]
    return remember(locale, currencyCode) {
        NumberFormat.getCurrencyInstance(locale).apply {
            currency = Currency.getInstance(currencyCode)
        }
    }
}
