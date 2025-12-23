package com.ltcn272.finny.data.repository

import com.ltcn272.finny.data.mapper.toBudgetReport
import com.ltcn272.finny.data.mapper.toCategoryReport
import com.ltcn272.finny.data.remote.api.DashboardApi
import com.ltcn272.finny.data.remote.dto.DateRangeDto
import com.ltcn272.finny.data.remote.dto.ReportRequestDto
import com.ltcn272.finny.domain.model.BudgetReport
import com.ltcn272.finny.domain.model.CategoryReport
import com.ltcn272.finny.domain.repository.DashboardRepository
import com.ltcn272.finny.domain.util.AppResult
import com.ltcn272.finny.domain.util.toErrorType
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class DashboardRepositoryImpl @Inject constructor(
    private val dashboardApi: DashboardApi
) : DashboardRepository {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

    override suspend fun getCategoryReport(
        startDate: ZonedDateTime,
        endDate: ZonedDateTime
    ): AppResult<CategoryReport> {
        return try {
            val startUTC = startDate.withZoneSameInstant(ZoneId.of("UTC")).format(formatter)
            val endUTC = endDate.withZoneSameInstant(ZoneId.of("UTC")).format(formatter)

            val request = ReportRequestDto(
                dateRange = DateRangeDto(
                    start = startUTC,
                    end = endUTC
                )
            )

            val response = dashboardApi.getCategoryReport(request)
            AppResult.Success(response.data.toCategoryReport())
        } catch (e: Exception) {
            AppResult.Error(e.toErrorType())
        }
    }

    override suspend fun getBudgetReport(
        startDate: ZonedDateTime,
        endDate: ZonedDateTime
    ): AppResult<BudgetReport> {
        return try {
            val startUTC = startDate.withZoneSameInstant(ZoneId.of("UTC")).format(formatter)
            val endUTC = endDate.withZoneSameInstant(ZoneId.of("UTC")).format(formatter)

            val request = ReportRequestDto(
                dateRange = DateRangeDto(
                    start = startUTC,
                    end = endUTC
                )
            )
            val response = dashboardApi.getBudgetReport(request)
            AppResult.Success(response.data.toBudgetReport())
        } catch (e: Exception) {
            AppResult.Error(e.toErrorType())
        }
    }
}
