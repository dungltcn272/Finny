package com.ltcn272.finny.domain.model

import java.time.ZonedDateTime

data class Budget(
    val id: String,
    val name: String,
    val userId: String,
    val amount: Double,
    val startDate: ZonedDateTime, // Dùng ZonedDateTime để handle múi giờ/ISO8601
    val remain: Double,
    val limit: Double,
    val period: String, // "single", "1_week", "1_month", "1_year"
    val daysRemaining: Double,
    val diffAvg: Double,
    val progress: Double,
    val totalIncome: Double,
    val totalOutcome: Double,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime
)