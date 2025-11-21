package com.ltcn272.finny.data.remote.dto

import com.google.gson.annotations.SerializedName


data class BudgetDataDto(
    @SerializedName("data") val data: List<BudgetDto>,
    @SerializedName("pagination") val pagination: PaginationDto
)

data class BudgetDto(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("limit") val limit: Double,     // Hạn mức của budget
    @SerializedName("start_date") val startDate: String,
    @SerializedName("remain") val remain: Double, // Số tiền còn lại (backend tính)
    @SerializedName("period") val period: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

// DTO for Create Budget Request Body
data class CreateBudgetRequestDto(
    @SerializedName("name") val name: String,
    @SerializedName("limit") val limit: Double, // Đổi tên từ amount -> limit
    @SerializedName("start_date") val startDate: String, // Format: "MM/dd/yyyy"
    @SerializedName("period") val period: String
)
