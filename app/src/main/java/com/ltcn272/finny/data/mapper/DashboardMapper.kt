package com.ltcn272.finny.data.mapper

import com.ltcn272.finny.data.remote.dto.*
import com.ltcn272.finny.domain.model.*

// --- Category Report Mapper ---
fun CategoryReportDataDto.toCategoryReport(): CategoryReport {
    return CategoryReport(
        totals = this.totals.toReportTotals(),
        pieItems = this.pie.map { it.toPieItem() },
        detailItems = this.categories.map { it.toCategoryReportDetail() }
    )
}

fun CategoryReportDetailDto.toCategoryReportDetail(): CategoryReportDetail {
    return CategoryReportDetail(
        id = this.id,
        name = this.name,
        outcome = this.outcome
    )
}

// --- Budget Report Mapper ---
fun BudgetReportDataDto.toBudgetReport(): BudgetReport {
    return BudgetReport(
        totals = this.totals.toReportTotals(),
        pieItems = this.pie.map { it.toPieItem() },
        detailItems = this.budgets.map { it.toBudgetReportDetail() }
    )
}

fun BudgetReportDetailDto.toBudgetReportDetail(): BudgetReportDetail {
    return BudgetReportDetail(
        id = this.id,
        name = this.name,
        limit = this.limit,
        outcome = this.outcome,
        ratio = this.ratio
    )
}

// --- Common Mappers ---
fun ReportTotalsDto.toReportTotals(): ReportTotals {
    return ReportTotals(
        income = this.income,
        outcome = this.outcome,
        net = this.net,
        prevIncome = this.prevIncome,
        prevOutcome = this.prevOutcome
    )
}

fun ReportPieItemDto.toPieItem(): PieItem {
    return PieItem(
        label = this.label,
        value = this.value,
        percent = this.percent
    )
}
