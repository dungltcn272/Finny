package com.ltcn272.finny.presentation.common.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlin.math.absoluteValue

object CurrencyUtils {

    private data class Suffixes(val thousand: String, val million: String, val billion: String)

    private val VN_SUFFIXES = Suffixes(thousand = "N", million = "TR", billion = "Tỷ")
    private val INT_SUFFIXES = Suffixes(thousand = "K", million = "M", billion = "B")

    fun getCurrencySymbolForCurrentLocale(): String {
        return try {
            val locale = Locale.getDefault()
            Currency.getInstance(locale).symbol
        } catch (e: Exception) {
            "$"
        }
    }


    fun formatCurrencyShort(value: Double, locale: Locale = Locale.getDefault()): String {
        val isVietnamese = locale.language == "vi"
        val suffixes = if (isVietnamese) VN_SUFFIXES else INT_SUFFIXES

        val billion = 1_000_000_000.0
        val million = 1_000_000.0
        val thousand = 1_000.0

        val absValue = value.absoluteValue

        val numberFormat = NumberFormat.getNumberInstance(locale).apply {
            maximumFractionDigits = 1
            minimumFractionDigits = 0
        }

        return when {
            absValue >= billion -> {
                val displayValue = value / billion
                "${numberFormat.format(displayValue)}${suffixes.billion}"
            }
            absValue >= million -> {
                val displayValue = value / million
                "${numberFormat.format(displayValue)}${suffixes.million}"
            }
            absValue >= thousand -> {
                val displayValue = value / thousand
                "${numberFormat.format(displayValue)}${suffixes.thousand}"
            }
            else -> {
                NumberFormat.getIntegerInstance(locale).format(value)
            }
        }
    }

    /**
     * Định dạng một chuỗi số thành chuỗi có dấu phân cách hàng nghìn.
     * Ví dụ: "1000000" -> "1.000.000"
     *
     * @param amount Chuỗi số đầu vào.
     * @return Chuỗi đã được định dạng. Nếu đầu vào không phải là số hợp lệ, trả về chuỗi gốc.
     */
    fun formatAmountWithSeparators(amount: String): String {
        return try {
            val formatter = NumberFormat.getNumberInstance(Locale("de", "DE"))

            if (amount.contains(".")) {
                val parts = amount.split('.')
                val integerPart = parts[0].toLong()
                val decimalPart = parts.getOrNull(1) ?: ""

                val formattedInteger = formatter.format(integerPart)
                "$formattedInteger,$decimalPart"
            } else {
                val number = amount.toLong()
                formatter.format(number)
            }
        } catch (e: NumberFormatException) {
            amount
        }
    }
}
