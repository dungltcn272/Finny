package com.ltcn272.finny.domain.model

import java.time.ZonedDateTime

data class Budget(
    val id: String,
    val name: String,
    val userId: String,
    val limit: Double,
    val period: BudgetPeriod,
    val startDate: ZonedDateTime,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime
)

data class BudgetDetails(
    val budget: Budget,
    val spentAmount: Double
)
