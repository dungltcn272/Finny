package com.ltcn272.finny.domain.model

data class DashboardOverview(
    val summary: OverviewSummary,
    val chartData: List<OverviewChartItem>,
    val budgets: List<OverviewBudgetSummary>,
    val categories: List<OverviewCategorySummary>
)

data class OverviewSummary(
    val currentBalance: Double,
    val totalIncome: Double,
    val totalExpenses: Double,
    val transactionCount: Int,
    val avgPerDay: Double,
    val budgetUsagePercent: Int
)

data class OverviewChartItem(
    val date: String,
    val label: String,
    val income: Float,
    val outcome: Float
)

data class OverviewBudgetSummary(
    val id: String,
    val name: String,
    val limit: Double,
    val spent: Double,
    val usagePercent: Int
)

data class OverviewCategorySummary(
    val id: String,
    val name: String,
    val outcome: Double
)