package com.ltcn272.finny.data.mapper

import com.ltcn272.finny.data.local.entities.BudgetEntity
import com.ltcn272.finny.data.remote.dto.BudgetDto
import com.ltcn272.finny.data.remote.dto.CreateBudgetRequestDto
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.domain.model.BudgetPeriod
import com.ltcn272.finny.domain.util.DateUtils

// DTO (from API) -> Entity (for Room)
fun BudgetDto.toEntity(isSynced: Boolean = true): BudgetEntity {
    return BudgetEntity(
        id = this.id,
        name = this.name,
        userId = this.userId,
        startDate = this.startDate,
        limit = this.limit,
        period = this.period,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        isSynced = isSynced,
        isDeleted = false
    )
}

// Entity (from Room) -> Domain (for UI/Logic)
fun BudgetEntity.toDomain(): Budget {
    val startDateZoned = DateUtils.parseApiDate(this.startDate)
    val createdAtZoned = DateUtils.parseIso8601(this.createdAt)
    val updatedAtZoned = DateUtils.parseIso8601(this.updatedAt)

    return Budget(
        id = this.id,
        name = this.name,
        userId = this.userId,
        startDate = startDateZoned,
        limit = this.limit,
        period = when(this.period) {
            "single" -> BudgetPeriod.SINGLE
            "1_week" -> BudgetPeriod.ONE_WEEK
            "1_month" -> BudgetPeriod.ONE_MONTH
            "1_year" -> BudgetPeriod.ONE_YEAR
            else -> BudgetPeriod.UNKNOWN
        },
        createdAt = createdAtZoned,
        updatedAt = updatedAtZoned
    )
}

// Domain (from UI) -> DTO (for API Request)
fun Budget.toCreateRequestDto(): CreateBudgetRequestDto {
    return CreateBudgetRequestDto(
        name = this.name,
        limit = this.limit,
        startDate = DateUtils.formatApiRequestDate(this.startDate),
        period = when(this.period) {
            BudgetPeriod.SINGLE -> "single"
            BudgetPeriod.ONE_WEEK -> "1_week"
            BudgetPeriod.ONE_MONTH -> "1_month"
            BudgetPeriod.ONE_YEAR -> "1_year"
            else -> "unknown"
        }
    )
}

// Domain (from UI) -> Entity (for Room)
fun Budget.toEntity(isSynced: Boolean = false, isDeleted: Boolean = false): BudgetEntity {
    return BudgetEntity(
        id = this.id,
        name = this.name,
        userId = this.userId,
        startDate = DateUtils.formatApiRequestDate(this.startDate),
        limit = this.limit,
        period = when(this.period) {
            BudgetPeriod.SINGLE -> "single"
            BudgetPeriod.ONE_WEEK -> "1_week"
            BudgetPeriod.ONE_MONTH -> "1_month"
            BudgetPeriod.ONE_YEAR -> "1_year"
            else -> "unknown"
        },
        createdAt = DateUtils.formatIso8601(this.createdAt),
        updatedAt = DateUtils.formatIso8601(this.updatedAt),
        isSynced = isSynced,
        isDeleted = isDeleted
    )
}
