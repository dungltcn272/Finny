// Kotlin
package com.ltcn272.finny.domain.util

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object DateUtils {

    // Primary ISO formatters
    private val API_DATE_TIME: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    private val ISO_ZONED: DateTimeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME
    private val ISO_LOCAL_DT: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val ISO_LOCAL_D: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    // Common legacy date-only formats we might encounter in Room/API
    private val LEGACY_DATE_ONLY_FORMATTERS: List<DateTimeFormatter> = listOf(
        ISO_LOCAL_D, // uuuu-MM-dd
        DateTimeFormatter.ofPattern("M/d/uuuu"),   // 1/2/2025
        DateTimeFormatter.ofPattern("MM/dd/uuuu"), // 01/02/2025
        DateTimeFormatter.ofPattern("d/M/uuuu"),   // 2/1/2025
        DateTimeFormatter.ofPattern("dd/MM/uuuu"), // 02/01/2025
        DateTimeFormatter.ofPattern("uuuu/M/d"),   // 2025/1/2
        DateTimeFormatter.ofPattern("uuuu/MM/dd")  // 2025/01/02
    )

    // Persist full date-time with offset when saving to Room/API
    fun formatApiRequestDate(dateTime: ZonedDateTime): String =
        dateTime.format(API_DATE_TIME)

    // Parse what we stored/received; preserve time information
    fun parseApiDate(value: String): ZonedDateTime {
        val text = value.trim()

        // Fully-qualified with zone
        runCatching { return ZonedDateTime.parse(text, ISO_ZONED) }
        runCatching { return OffsetDateTime.parse(text, API_DATE_TIME).toZonedDateTime() }

        // Local date-time (no zone)
        runCatching { return LocalDateTime.parse(text, ISO_LOCAL_DT).atZone(ZoneId.systemDefault()) }

        // Date-only: try a set of common patterns (includes MM/dd/uuuu)
        for (fmt in LEGACY_DATE_ONLY_FORMATTERS) {
            try {
                return LocalDate.parse(text, fmt).atStartOfDay(ZoneId.systemDefault())
            } catch (_: Exception) {
                // try next
            }
        }

        throw DateTimeParseException("Unsupported date/time format", text, 0)
    }

    // Keep createdAt/updatedAt consistent with time and offset
    fun formatIso8601(zdt: ZonedDateTime): String = zdt.format(API_DATE_TIME)

    fun parseIso8601(value: String): ZonedDateTime {
        val text = value.trim()
        runCatching { return ZonedDateTime.parse(text, ISO_ZONED) }
        runCatching { return OffsetDateTime.parse(text, API_DATE_TIME).toZonedDateTime() }
        runCatching { return LocalDateTime.parse(text, ISO_LOCAL_DT).atZone(ZoneId.systemDefault()) }

        for (fmt in LEGACY_DATE_ONLY_FORMATTERS) {
            try {
                return LocalDate.parse(text, fmt).atStartOfDay(ZoneId.systemDefault())
            } catch (_: Exception) {
            }
        }

        throw DateTimeParseException("Unsupported ISO8601 format", text, 0)
    }
}
