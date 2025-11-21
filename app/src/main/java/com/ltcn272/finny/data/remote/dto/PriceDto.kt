package com.ltcn272.finny.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PriceResponseDto(
    @SerializedName("status") val status: Int,
    @SerializedName("data") val data: List<PriceDto>,
    @SerializedName("message") val message: String
)

data class PriceDto(
    @SerializedName("_id") val id: String,
    @SerializedName("plan_name") val planName: String,
    @SerializedName("price") val price: Double,
    @SerializedName("currency") val currency: String,
    @SerializedName("period") val period: String,
    @SerializedName("features") val features: List<String>,
    @SerializedName("is_default") val isDefault: Boolean,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)