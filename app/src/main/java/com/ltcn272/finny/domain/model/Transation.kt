package com.ltcn272.finny.domain.model

import java.time.ZonedDateTime

data class Transaction(
    val id: String,
    val name: String?,
    val budgetId: String?,
    val type: String?, // "outcome" / "income"
    val description: String?,
    val userId: String?,
    val category: String?,
    val amount: Double,
    val dateTime: ZonedDateTime?, // Dùng ZonedDateTime
    val image: String?,
    val location: Location?,
    val createdAt: ZonedDateTime?,
    val updatedAt: ZonedDateTime?
)

data class Location(
    val name: String,
    val lat: Double?,
    val lng: Double?
)

data class TransactionFilter(
    val budgetId: String? = null,
    val tags: List<String>? = null
    // Có thể mở rộng thêm filter theo Date range, Category, v.v. (từ TransactionViewModel.swift logic)
)