package com.ltcn272.finny.presentation.common.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

fun formatCurrency(amount: Double, currency: String): String {
    // Use Vietnamese locale for "vnd" to get '.' as a grouping separator, as shown in the image.
    val locale = if (currency.equals("vnd", ignoreCase = true)) {
        Locale("vi", "VN")
    } else {
        // Use a generic US locale for others, which typically uses ',' as a separator.
        Locale.US
    }
    val symbols = DecimalFormatSymbols(locale)
    val formatter = DecimalFormat("#,###", symbols)
    return "${formatter.format(amount)} ${currency.lowercase()}"
}
