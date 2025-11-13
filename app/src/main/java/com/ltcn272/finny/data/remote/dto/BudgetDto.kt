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
    @SerializedName("amount") val amount: Double,
    @SerializedName("start_date") val startDate: String, // API trả về Date String (ISO8601 hoặc MM/dd/yyyy)
    @SerializedName("remain") val remain: Double,
    @SerializedName("limit") val limit: Double,
    @SerializedName("period") val period: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("days_remaining") val daysRemaining: Double,
    @SerializedName("diff_avg") val diffAvg: Double,
    @SerializedName("progress") val progress: Double,
    @SerializedName("total_income") val totalIncome: Double,
    @SerializedName("total_outcome") val totalOutcome: Double
)

// DTO cho Create Budget Request Body (CreateBudgetView.swift)
data class CreateBudgetRequestDto(
    @SerializedName("name") val name: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("start_date") val startDate: String, // Format: "MM/dd/yyyy" (từ BudgetViewModel.swift)
    @SerializedName("period") val period: String // "single", "1_week", "1_month", "1_year"
)