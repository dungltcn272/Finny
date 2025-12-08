package com.ltcn272.finny.data.remote.dto

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class PriceResponseDto(
    @SerializedName("status") val status: Int,
    @SerializedName("data") val data: PriceDataDto,
    @SerializedName("message") val message: String
)

data class PriceDataDto(
    @SerializedName("plan") val plan: Map<String, List<PriceOptionDto>>,
    @SerializedName("Feature") val feature: Map<String, List<FeatureDto>>
)

data class PriceOptionDto(
    @SerializedName("code") val code: String,
    @SerializedName("display") val display: String,
    @SerializedName("duration") val duration: Int,
    @SerializedName("unit") val unit: String,
    @SerializedName("price") val price: JsonElement
)

data class FeatureDto(
    @SerializedName("code") val code: String,
    @SerializedName("type") val type: String,
    @SerializedName("value") val value: JsonElement
)