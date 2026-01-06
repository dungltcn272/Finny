package com.ltcn272.finny.domain.model

data class ReportTotals(
    val income: Double,
    val outcome: Double,
    val net: Double,
    val prevIncome: Double,
    val prevOutcome: Double
)

// Dùng chung cho cả Budget và Category Report
interface Report {
    val totals: ReportTotals
    val pieItems: List<PieItem>
    val detailItems: List<Any>
}

data class CategoryReport(
    override val totals: ReportTotals,
    override val pieItems: List<PieItem>,
    override val detailItems: List<CategoryReportDetail>
) : Report

data class CategoryReportDetail(
    val id: String,
    val name: String,
    val outcome: Double
)

data class BudgetReport(
    override val totals: ReportTotals,
    override val pieItems: List<PieItem>,
    override val detailItems: List<BudgetReportDetail>
) : Report

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
