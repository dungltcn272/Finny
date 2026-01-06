package com.ltcn272.finny.data.remote.dto

import com.google.gson.annotations.SerializedName

// --- Request Body for Overview ---
data class DashboardOverviewRequestDto(
    @SerializedName("period")
    val period: String // "week", "month"
)

// --- Response Body for Overview ---
data class DashboardOverviewDataDto(
    @SerializedName("summary")
    val summary: OverviewSummaryDto,
    @SerializedName("chart_data")
    val chartData: List<OverviewChartItemDto>,
    @SerializedName("budgets")
    val budgets: List<OverviewBudgetDto>,
    @SerializedName("categories")
    val categories: List<OverviewCategoryDto>
)

data class OverviewSummaryDto(
    @SerializedName("current_balance")
    val currentBalance: Double,
    @SerializedName("total_income")
    val totalIncome: Double,
    @SerializedName("total_expenses")
    val totalExpenses: Double,
    @SerializedName("balance")
    val balance: Double,
    @SerializedName("transaction_count")
    val transactionCount: Int,
    @SerializedName("avg_per_day")
    val avgPerDay: Double,
    @SerializedName("budget_usage_percent")
    val budgetUsagePercent: Int,
    @SerializedName("period")
    val period: String
)

data class OverviewChartItemDto(
    @SerializedName("date")
    val date: String,
    @SerializedName("label")
    val label: String,
    @SerializedName("income")
    val income: Float,
    @SerializedName("outcome")
    val outcome: Float
)

data class OverviewBudgetDto(
    @SerializedName("_id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("limit")
    val limit: Double,
    @SerializedName("usage_percent")
    val usagePercent: Int
)

data class OverviewCategoryDto(
    @SerializedName("_id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("income")
    val income: Double,
    @SerializedName("outcome")
    val outcome: Double
)

// --- Response Body for AI Insight ---
data class AiReportDataDto(
    @SerializedName("insight") val insight: String
)
