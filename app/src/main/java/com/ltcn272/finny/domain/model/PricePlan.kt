package com.ltcn272.finny.domain.model

import java.time.ZonedDateTime

data class PricePlan(
    val id: String,
    val planName: String,
    val price: Double,
    val currency: String,
    val period: String,
    val features: List<String>,
    val isDefault: Boolean,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime
)