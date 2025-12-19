package com.ltcn272.finny.domain.model

data class PricePlan(
    val tier: String,
    val code: String,
    val display: String,
    val price: Double,
    val currency: String,
    val duration: Int,
    val unit: String,
    val features: List<String>
)