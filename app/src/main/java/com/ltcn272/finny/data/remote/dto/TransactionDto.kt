package com.ltcn272.finny.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TransactionListResponseDto(
    @SerializedName("status") val status: Int,
    @SerializedName("data") val data: List<TransactionDto>,
    @SerializedName("pagination") val pagination: PaginationDto
)

data class TransactionDto(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("budget_id") val budgetId: String,
    @SerializedName("type") val type: String,
    @SerializedName("description") val description: String?,
    @SerializedName("user_id") val userId: String,
    @SerializedName("category") val category: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("date_time") val dateTime: String,
    @SerializedName("image") val image: String?,
    @SerializedName("location") val location: LocationDto?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)

data class LocationDto(
    @SerializedName("name") val name: String,
    @SerializedName("lat") val lat: Double?,
    @SerializedName("lng") val lng: Double?
)

data class TransactionFilterRequestDto(
    @SerializedName("page") val page: Int,
    @SerializedName("filter") val filter: FilterBodyDto? = null
)

data class FilterBodyDto(
    @SerializedName("budget_id") val budgetId: String?
)

data class CreateTransactionRequestDto(
    @SerializedName("name") val name: String?,
    @SerializedName("budget_id") val budgetId: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("amount") val amount: Double,
    @SerializedName("date_time") val dateTime: String?,
    @SerializedName("image") val image: String?,
    @SerializedName("location") val location: LocationRequestDto?
) {
    data class LocationRequestDto(
        @SerializedName("name") val name: String?,
        @SerializedName("lat") val lat: Double,
        @SerializedName("lng") val lng: Double
    )
}