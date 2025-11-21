package com.ltcn272.finny.domain.model

import java.time.LocalDate
import java.time.ZonedDateTime

data class Transaction(
    val id: String,
    val name: String,
    val budgetId: String,
    val type: TransactionType,
    val description: String?,
    val userId: String?,
    val category: TransactionCategory,
    val amount: Double,
    val dateTime: ZonedDateTime,
    val image: String?, // Server URL
    val localImagePath: String?, // Local file path for offline images
    val location: Location?,
    val createdAt: ZonedDateTime?,
    val updatedAt: ZonedDateTime?
)

enum class TransactionType {
    INCOME,
    OUTCOME
}

enum class TransactionCategory {
    FOOD,
    LUNCH,
    COFFEE,
    TRANSPORTATION,
    SHOPPING,
    HOUSING,
    UTILITIES,
    HEALTHCARE,
    ENTERTAINMENT,
    EDUCATION,
    SALARY,
    GIFT,
    OTHER
}

data class Location(
    val name: String?,
    val lat: Double,
    val lng: Double
)

data class TransactionFilter(
    val budgetId: String? = null,
    val selectedBudget: Budget? = null,
    val tags: List<String>? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
)
