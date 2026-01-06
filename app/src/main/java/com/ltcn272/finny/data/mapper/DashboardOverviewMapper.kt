package com.ltcn272.finny.data.mapper

import com.ltcn272.finny.data.remote.dto.*
import com.ltcn272.finny.domain.model.*

fun DashboardOverviewDataDto.toDashboardOverview(): DashboardOverview {
    return DashboardOverview(
        summary = this.summary.toOverviewSummary(),
        chartData = this.chartData.map { it.toOverviewChartItem() },
        budgets = this.budgets.map { it.toOverviewBudgetSummary() },
        categories = this.categories.map { it.toOverviewCategorySummary() }
    )
}

fun OverviewSummaryDto.toOverviewSummary(): OverviewSummary {
    return OverviewSummary(
        currentBalance = this.currentBalance,
        totalIncome = this.totalIncome,
        totalExpenses = this.totalExpenses,
        transactionCount = this.transactionCount,
        avgPerDay = this.avgPerDay,
        budgetUsagePercent = this.budgetUsagePercent
    )
}

fun OverviewChartItemDto.toOverviewChartItem(): OverviewChartItem {
    return OverviewChartItem(
        date = this.date,
        label = this.label,
        income = this.income,
        outcome = this.outcome
    )
}

fun OverviewBudgetDto.toOverviewBudgetSummary(): OverviewBudgetSummary {
    val spent = (this.limit * this.usagePercent) / 100
    return OverviewBudgetSummary(
        id = this.id,
        name = this.name,
        limit = this.limit,
        spent = spent,
        usagePercent = this.usagePercent
    )
}

fun OverviewCategoryDto.toOverviewCategorySummary(): OverviewCategorySummary {
    return OverviewCategorySummary(
        id = this.id,
        name = this.name,
        outcome = this.outcome
    )
}

