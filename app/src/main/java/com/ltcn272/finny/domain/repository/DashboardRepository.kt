package com.ltcn272.finny.domain.repository

import com.ltcn272.finny.domain.model.BudgetReport
import com.ltcn272.finny.domain.model.CategoryReport
import com.ltcn272.finny.domain.util.AppResult
import java.time.ZonedDateTime

interface DashboardRepository {
    suspend fun getCategoryReport(startDate: ZonedDateTime, endDate: ZonedDateTime): AppResult<CategoryReport>
    suspend fun getBudgetReport(startDate: ZonedDateTime, endDate: ZonedDateTime): AppResult<BudgetReport>
}
