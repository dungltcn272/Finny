package com.ltcn272.finny.domain.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
object DateUtils {
    private val ISO_8601_FORMATTER = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("Z"))
    // Cần tạo hàm này để chuyển đổi các format Date
    fun parseApiDate(dateString: String): ZonedDateTime = ZonedDateTime.now() // Placeholder
    fun formatApiRequestDate(date: ZonedDateTime): String = "" // Placeholder (ví dụ: "MM/dd/yyyy")

    // Chuyển ZonedDateTime sang String (ISO8601) cho API
    fun formatIso8601(date: ZonedDateTime): String {
        return date.format(ISO_8601_FORMATTER)
    }

    // Chuyển String (ISO8601) sang ZonedDateTime
    fun parseIso8601(dateString: String): ZonedDateTime {
        return ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault()))
    }
}