package com.ltcn272.finny.presentation.common.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.time.ZonedDateTime
import androidx.compose.ui.platform.LocalConfiguration
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun formatDate(dateTime: ZonedDateTime): String {
    val locale = LocalConfiguration.current.locales[0]
    val formatter = remember(locale) {
        DateTimeFormatter
            .ofPattern("dd MMMM yyyy", locale)
    }
    return dateTime.format(formatter)
}


fun getCurrentDateFormatted(locale: Locale = Locale.getDefault()): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", locale)
    return ZonedDateTime.now().format(formatter)
}
