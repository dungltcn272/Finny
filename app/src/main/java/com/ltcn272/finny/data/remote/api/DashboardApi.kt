package com.ltcn272.finny.data.remote.api

import com.ltcn272.finny.data.remote.dto.*
import retrofit2.http.Body
import retrofit2.http.POST

interface DashboardApi {
    @POST("dashboards/reports/categories")
    suspend fun getCategoryReport(
        @Body body: DashboardReportRequestDto
    ): ApiResponse<CategoryReportDataDto>

    @POST("dashboards/reports/budgets")
    suspend fun getBudgetReport(
        @Body body: DashboardReportRequestDto
    ): ApiResponse<BudgetReportDataDto>

    @POST("dashboards/reports/ai")
    suspend fun getAiReport(
        @Body body: DashboardOverviewRequestDto
    ): ApiResponse<AiReportDataDto>

    @POST("dashboards/overview")
    suspend fun getOverview(
        @Body body: DashboardOverviewRequestDto
    ): ApiResponse<DashboardOverviewDataDto>
}
