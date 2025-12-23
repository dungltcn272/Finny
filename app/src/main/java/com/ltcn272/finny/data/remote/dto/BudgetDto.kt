package com.ltcn272.finny.data.remote.dto

import com.google.gson.annotations.SerializedName

data class BudgetListDataDto(
    @SerializedName("data")
    val data: List<BudgetResponseDto>,
    @SerializedName("pagination")
    val pagination: PaginationDto
)

data class BudgetResponseDto(
    @SerializedName("_id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("user_id")
    val userId: String,

    @SerializedName("amount")
    val amount: Double,

    @SerializedName("start_date")
    val startDate: String,

    @SerializedName("limit")
    val limit: Double,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String,

    @SerializedName("days_remaining")
    val daysRemaining: Double?,

    @SerializedName("days_passed")
    val daysPassed: Double,

    @SerializedName("actual_avg")
    val actualAvg: Double,

    @SerializedName("expected_avg")
    val expectedAvg: Double,

    @SerializedName("diff_avg")
    val diffAvg: Double,

    @SerializedName("progress")
    val progress: Double,

    @SerializedName("total_income")
    val totalIncome: Double,

    @SerializedName("total_outcome")
    val totalOutcome: Double,

    @SerializedName("recurring_topup_amount")
    val recurringTopupAmount: Double,

    @SerializedName("recurring_interval_unit")
    val recurringIntervalUnit: String,

    @SerializedName("recurring_interval_value")
    val recurringIntervalValue: Int,

    @SerializedName("recurring_active")
    val recurringActive: Boolean,

    @SerializedName("recurring_next_run_at")
    val recurringNextRunAt: String?,

    @SerializedName("recurring_last_run_at")
    val recurringLastRunAt: String?
)

data class CreateBudgetRequestDto(
    @SerializedName("name")
    val name: String,

    @SerializedName("amount")
    val amount: Double,

    @SerializedName("start_date")
    val startDate: String,

    @SerializedName("limit")
    val limit: Double? = null,

    @SerializedName("recurring_active")
    val recurringActive: Boolean? = null,

    @SerializedName("recurring_interval_unit")
    val recurringIntervalUnit: String? = null,

    @SerializedName("recurring_interval_value")
    val recurringIntervalValue: Int? = null,

    @SerializedName("recurring_topup_amount")
    val recurringTopupAmount: Double? = null
)