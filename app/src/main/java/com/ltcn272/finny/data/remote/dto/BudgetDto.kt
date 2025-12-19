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
    val amount: Long,

    @SerializedName("start_date")
    val startDate: String,

    @SerializedName("limit")
    val limit: Long,

    @SerializedName("currency")
    val currency: String,

    @SerializedName("recurring_topup_amount")
    val recurringTopupAmount: Long,

    @SerializedName("threshold_level_notified")
    val thresholdLevelNotified: Int,

    @SerializedName("recurring_interval_unit")
    val recurringIntervalUnit: String?,

    @SerializedName("recurring_interval_value")
    val recurringIntervalValue: Int,

    @SerializedName("recurring_active")
    val recurringActive: Boolean,

    @SerializedName("recurring_next_run_at")
    val recurringNextRunAt: String?,

    @SerializedName("recurring_last_run_at")
    val recurringLastRunAt: String?,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String,

    @SerializedName("days_remaining")
    val daysRemaining: Int?,

    @SerializedName("days_passed")
    val daysPassed: Int,

    @SerializedName("total_income")
    val totalIncome: Long,

    @SerializedName("total_outcome")
    val totalOutcome: Long,

    @SerializedName("actual_avg")
    val actualAvg: Long,

    @SerializedName("expected_avg")
    val expectedAvg: Long,

    @SerializedName("diff_avg")
    val diffAvg: Long,

    @SerializedName("progress")
    val progress: Float,

    @SerializedName("is_single")
    val isSingle: Boolean
)

data class CreateBudgetRequestDto(

    @SerializedName("name")
    val name: String,

    @SerializedName("amount")
    val amount: Long,

    @SerializedName("start_date")
    val startDate: String,

    // ===== Optional fields =====

    @SerializedName("limit")
    val limit: Long? = null,

    @SerializedName("currency")
    val currency: String? = null,

    @SerializedName("recurring_active")
    val recurringActive: Boolean? = null,

    @SerializedName("recurring_interval_unit")
    val recurringIntervalUnit: String? = null,
    // "day" | "week" | "month" | "year"

    @SerializedName("recurring_interval_value")
    val recurringIntervalValue: Int? = null,

    @SerializedName("recurring_topup_amount")
    val recurringTopupAmount: Long? = null
)
