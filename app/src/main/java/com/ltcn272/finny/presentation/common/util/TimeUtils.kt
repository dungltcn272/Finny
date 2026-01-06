package com.ltcn272.finny.presentation.common.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.time.ZonedDateTime
import androidx.compose.ui.platform.LocalConfiguration
import java.time.LocalDateTime
import java.time.Year
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.TextStyle
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

@Composable
fun formatZonedDateTimeFull(dateTime: ZonedDateTime): String {
    val locale = LocalConfiguration.current.locales.get(0)
    val dayOfWeek = dateTime.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }

    val isCurrentYear = dateTime.year == Year.now().value

    val pattern = if (isCurrentYear) {
        "d MMMM"
    } else {
        "d MMMM yyyy"
    }

    val formatter = remember(locale, pattern) {
        DateTimeFormatter.ofPattern(pattern, locale)
    }
    return "$dayOfWeek, ${dateTime.format(formatter)}"
}

fun getCurrentDateFormatted(locale: Locale = Locale.getDefault()): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", locale)
    return ZonedDateTime.now().format(formatter)
}

fun parseUtcString(dateString: String): LocalDateTime? {
    return try {
        LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
    } catch (e: DateTimeParseException) {null
    }
}

fun formatDateFull(dateTime: ZonedDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy")
    return dateTime.withZoneSameInstant(ZoneId.systemDefault()).format(formatter)
}
