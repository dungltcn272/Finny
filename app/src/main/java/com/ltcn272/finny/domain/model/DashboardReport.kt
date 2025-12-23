package com.ltcn272.finny.domain.model

data class ReportTotals(
    val income: Double,
    val outcome: Double,
    val net: Double,
    val prevIncome: Double,
    val prevOutcome: Double
)

data class CategoryReport(
    val totals: ReportTotals,
    val pieItems: List<PieItem>,
    val detailItems: List<CategoryReportDetail>
)

data class CategoryReportDetail(
    val id: String,
    val name: String,
    val outcome: Double
)

data class BudgetReport(
    val totals: ReportTotals,
    val pieItems: List<PieItem>,
    val detailItems: List<BudgetReportDetail>
)

data class BudgetReportDetail(
    val id: String,
    val name: String,
    val limit: Double,
    val outcome: Double,
    val ratio: Double
)

data class PieItem(
    val label: String,
    val value: Double,
    val percent: Double
)
