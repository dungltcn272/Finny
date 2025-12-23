package com.ltcn272.finny.data.remote.dto

import com.google.gson.annotations.SerializedName

// --- Request Body ---
data class ReportRequestDto(
    @SerializedName("date_range")
    val dateRange: DateRangeDto
)

data class DateRangeDto(
    @SerializedName("start")
    val start: String,
    @SerializedName("end")
    val end: String
)

// --- Common Response Parts ---
data class ReportTotalsDto(
    @SerializedName("income") val income: Double,
    @SerializedName("outcome") val outcome: Double,
    @SerializedName("net") val net: Double,
    @SerializedName("prev_income") val prevIncome: Double,
    @SerializedName("prev_outcome") val prevOutcome: Double
)

data class ReportPieItemDto(
    @SerializedName("label") val label: String,
    @SerializedName("value") val value: Double,
    @SerializedName("percent") val percent: Double
)

// --- Categories Report Response ---
data class CategoryReportDataDto(
    @SerializedName("totals") val totals: ReportTotalsDto,
    @SerializedName("pie") val pie: List<ReportPieItemDto>,
    @SerializedName("categories") val categories: List<CategoryReportDetailDto>
)

data class CategoryReportDetailDto(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("outcome") val outcome: Double
)

// --- Budgets Report Response ---
data class BudgetReportDataDto(
    @SerializedName("totals") val totals: ReportTotalsDto,
    @SerializedName("pie") val pie: List<ReportPieItemDto>,
    @SerializedName("budgets") val budgets: List<BudgetReportDetailDto>
)

data class BudgetReportDetailDto(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("limit") val limit: Double,
    @SerializedName("outcome") val outcome: Double,
    @SerializedName("ratio") val ratio: Double
)
