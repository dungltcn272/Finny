package com.ltcn272.finny.data.remote.api

import com.ltcn272.finny.data.remote.dto.AiReportDataDto
import com.ltcn272.finny.data.remote.dto.ApiResponse
import com.ltcn272.finny.data.remote.dto.BudgetReportDataDto
import com.ltcn272.finny.data.remote.dto.CategoryReportDataDto
import com.ltcn272.finny.data.remote.dto.ReportRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface DashboardApi {

    @POST("dashboards/reports/categories")
    suspend fun getCategoryReport(
        @Body body: ReportRequestDto
    ): ApiResponse<CategoryReportDataDto>

    @POST("dashboards/reports/budgets")
    suspend fun getBudgetReport(
        @Body body: ReportRequestDto
    ): ApiResponse<BudgetReportDataDto>

    @POST("dashboards/reports/ai")
    suspend fun getAiReport(
        @Body body: ReportRequestDto
    ): ApiResponse<AiReportDataDto>
}
