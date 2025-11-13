package com.ltcn272.finny.data.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.ltcn272.finny.data.local.entities.BudgetEntity
import com.ltcn272.finny.data.remote.dto.BudgetDto
import com.ltcn272.finny.data.remote.dto.CreateBudgetRequestDto
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.domain.util.DateUtils

// 1. DTO (API) -> Entity (Room)
fun BudgetDto.toEntity(isSynced: Boolean = true): BudgetEntity {
    return BudgetEntity(
        id = this.id,
        name = this.name,
        userId = this.userId,
        amount = this.amount,
        startDate = this.startDate,
        remain = this.remain,
        limit = this.limit,
        period = this.period,
        daysRemaining = this.daysRemaining,
        diffAvg = this.diffAvg,
        progress = this.progress,
        totalIncome = this.totalIncome,
        totalOutcome = this.totalOutcome,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        isSynced = isSynced,
        isDeleted = false
    )
}

// 2. Entity (Room) -> Domain (UI/Business Logic)
fun BudgetEntity.toDomain(): Budget {
    // Chuyển đổi String Date thành ZonedDateTime
    val startDateZoned = DateUtils.parseApiDate(this.startDate)
    val createdAtZoned = DateUtils.parseIso8601(this.createdAt)
    val updatedAtZoned = DateUtils.parseIso8601(this.updatedAt)

    return Budget(
        id = this.id,
        name = this.name,
        userId = this.userId,
        amount = this.amount,
        startDate = startDateZoned,
        remain = this.remain,
        limit = this.limit,
        period = this.period,
        daysRemaining = this.daysRemaining,
        diffAvg = this.diffAvg,
        progress = this.progress,
        totalIncome = this.totalIncome,
        totalOutcome = this.totalOutcome,
        createdAt = createdAtZoned,
        updatedAt = updatedAtZoned
    )
}

// 3. Domain (UI Request) -> DTO (API POST/PUT Request Body)
fun Budget.toCreateRequestDto(): CreateBudgetRequestDto {
    return CreateBudgetRequestDto(
        name = this.name,
        amount = this.amount,
        startDate = DateUtils.formatApiRequestDate(this.startDate),
        period = this.period
    )
}

fun Budget.toEntity(isSynced: Boolean = false, isDeleted: Boolean = false): BudgetEntity {
    return BudgetEntity(
        id = this.id,
        name = this.name,
        userId = this.userId,
        amount = this.amount,

        // Chuyển ZonedDateTime sang String theo format API/DB
        startDate = DateUtils.formatApiRequestDate(this.startDate),

        remain = this.remain,
        limit = this.limit,
        period = this.period,
        daysRemaining = this.daysRemaining,
        diffAvg = this.diffAvg,
        progress = this.progress,
        totalIncome = this.totalIncome,
        totalOutcome = this.totalOutcome,

        // Chuyển ZonedDateTime sang String ISO8601
        createdAt = DateUtils.formatIso8601(this.createdAt),
        updatedAt = DateUtils.formatIso8601(this.updatedAt),

        // Trạng thái sync (Cực kỳ quan trọng cho Offline-First)
        isSynced = isSynced,
        isDeleted = isDeleted
    )
}

