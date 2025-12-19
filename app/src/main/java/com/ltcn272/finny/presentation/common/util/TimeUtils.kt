package com.ltcn272.finny.presentation.common.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.time.ZonedDateTime
import androidx.compose.ui.platform.LocalConfiguration
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun formatDateTime(dateTime: ZonedDateTime): String {
    val locale = LocalConfiguration.current.locales[0]
    val formatter = remember(locale) {
        DateTimeFormatter
            .ofLocalizedDate(FormatStyle.MEDIUM)
            .withLocale(locale)
    }
    return dateTime.format(formatter)
}
