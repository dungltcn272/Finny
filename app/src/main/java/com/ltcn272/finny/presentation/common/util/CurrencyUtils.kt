package com.ltcn272.finny.presentation.common.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlin.math.abs

@Composable
fun formatCurrency(amount: Double, currencyCode: String): String {
    val formatter = rememberCurrencyFormatter(currencyCode = currencyCode)
    return formatter.format(amount)
}


@Composable
fun rememberCurrencyFormatter(currencyCode: String): NumberFormat {
    val locale = Locale("vi", "VN")
    return remember(locale, currencyCode) {
        NumberFormat.getCurrencyInstance(locale).apply {
            currency = Currency.getInstance(currencyCode.uppercase())
            maximumFractionDigits = 0
        }
    }
}

fun formatCurrencyNonComposable(
    amount: Double,
    currencyCode: String,
    locale: Locale = Locale("vi", "VN")
): String {
    return NumberFormat.getCurrencyInstance(locale).apply {
        currency = Currency.getInstance(currencyCode.uppercase())
        maximumFractionDigits = 0
    }.format(amount)
}

fun formatDoubleForInput(value: Double): String {
    val formatter = DecimalFormat("#.################")
    return formatter.format(value)
}

fun formatCurrencyShortVietnamese(amount: Double): String {
    val absAmount = abs(amount)
    if (absAmount < 1000) {
        // Keep as is if under 1000, no currency symbol
        return absAmount.toLong().toString()
    }

    val millions = (absAmount / 1_000_000).toLong()
    val thousands = ((absAmount % 1_000_000) / 1_000).toLong()
    val hundreds = (absAmount % 1000).toLong()

    return when {
        // Millions
        millions > 0 -> {
            if (thousands > 0) {
                // If there are thousands, display as "2tr525"
                "${millions}tr${String.format("%03d", thousands)}"
            } else {
                // If it's an even million, display as "2tr"
                "${millions}tr"
            }
        }
        // Thousands
        thousands > 0 -> {
            // Display as "150k"
            "${thousands}k"
        }
        // Under 1000
        else -> hundreds.toString()
    }
}
