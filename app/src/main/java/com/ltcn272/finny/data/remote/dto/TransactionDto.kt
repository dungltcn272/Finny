package com.ltcn272.finny.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TransactionListDataDto(
    @SerializedName("data")
    val data: List<TransactionResponseDto>,

    @SerializedName("pagination")
    val pagination: PaginationDto
)

data class TransactionResponseDto(
    @SerializedName("_id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("budget_id")
    val budgetId: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("category_id")
    val categoryId: String,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("date_time")
    val dateTime: String,
    @SerializedName("image")
    val image: String?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("category")
    val category: CategoryResponseDto?
)

data class CreateTransactionRequestDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("budget_id")
    val budgetId: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("category_id")
    val categoryId: String,

    @SerializedName("date_time")
    val dateTime: String,
    @SerializedName("is_recurring")
    val isRecurring: Boolean,
    @SerializedName("recurring_start_date")
    val recurringStartDate: String? = null,
    @SerializedName("recurring_interval_unit")
    val recurringIntervalUnit: String? = null,
    @SerializedName("recurring_interval_value")
    val recurringIntervalValue: Int? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("image")
    val image: String? = null
)

data class RecurringTransactionListDataDto(
    @SerializedName("data")
    val data: List<RecurringTransactionResponseDto>,

    @SerializedName("pagination")
    val pagination: PaginationDto
)

data class RecurringTransactionResponseDto(
    @SerializedName("_id")
    val id: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("budget_id")
    val budgetId: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("category_id")
    val categoryId: String,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("image")
    val image: String?,
    @SerializedName("start_date")
    val startDate: String,
    @SerializedName("interval_unit")
    val intervalUnit: String,
    @SerializedName("interval_value")
    val intervalValue: Int,
    @SerializedName("next_run_at")
    val nextRunAt: String?,
    @SerializedName("last_run_at")
    val lastRunAt: String?,
    @SerializedName("active")
    val active: Boolean,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("category")
    val category: CategoryResponseDto?
)

data class TransactionListRequestDto(
    @SerializedName("page")
    val page: Int? = null,

    @SerializedName("filter")
    val filter: TransactionFilterDto? = null
)

data class TransactionFilterDto(
    @SerializedName("start_date")
    val startDate: String? = null,

    @SerializedName("end_date")
    val endDate: String? = null,

    @SerializedName("budget_id")
    val budgetId: String? = null
)

data class UploadImageDataDto(
    @SerializedName("url") val url: String,
    @SerializedName("fileId") val fileId: String,
    @SerializedName("filePath") val filePath: String
)


