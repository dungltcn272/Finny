package com.ltcn272.finny.data.mapper

import com.ltcn272.finny.data.local.entities.BudgetEntity
import com.ltcn272.finny.data.local.entities.SyncState
import com.ltcn272.finny.data.remote.dto.CreateBudgetRequestDto
import com.ltcn272.finny.data.remote.dto.BudgetResponseDto
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.domain.model.RecurringConfig
import com.ltcn272.finny.domain.model.RecurringIntervalUnit
import java.util.UUID

fun BudgetResponseDto.toEntity(): BudgetEntity {
    return BudgetEntity(
        id = UUID.randomUUID().toString(),
        serverId = this.id,
        name = name,
        amount = amount,
        limit = limit,
        currency = currency,
        startDate = startDate,
        recurringActive = recurringActive,
        recurringIntervalUnit = recurringIntervalUnit,
        recurringIntervalValue = recurringIntervalValue,
        recurringTopupAmount = recurringTopupAmount,
        recurringNextRunAt = recurringNextRunAt,
        recurringLastRunAt = recurringLastRunAt,
        isSingle = isSingle,
        syncState = SyncState.SYNCED,
        updatedAt = updatedAt
    )
}

fun BudgetEntity.toDomain(): Budget {
    return Budget(
        localId = id,
        serverId = serverId,
        name = name,
        amount = amount,
        limit = limit,
        currency = currency,
        startDate = startDate,
        recurring = if (recurringActive) {
            RecurringConfig(
                unit = RecurringIntervalUnit.valueOf(
                    recurringIntervalUnit!!.uppercase()
                ),
                value = recurringIntervalValue,
                topupAmount = recurringTopupAmount,
                nextRunAt = recurringNextRunAt,
                lastRunAt = recurringLastRunAt
            )
        } else null,
        isSingle = isSingle
    )
}


fun Budget.toEntity(syncState: SyncState): BudgetEntity {
    return BudgetEntity(
        id = localId ?: UUID.randomUUID().toString(),
        serverId = serverId,
        name = name,
        amount = amount,
        limit = limit,
        currency = currency,
        startDate = startDate,
        recurringActive = recurring != null,
        recurringIntervalUnit = recurring?.unit?.name,
        recurringIntervalValue = recurring?.value ?: 0,
        recurringTopupAmount = recurring?.topupAmount ?: 0L,
        recurringNextRunAt = recurring?.nextRunAt,
        recurringLastRunAt = recurring?.lastRunAt,
        isSingle = isSingle,
        syncState = syncState
    )
}

fun BudgetEntity.toCreateRequestDto(): CreateBudgetRequestDto {
    return CreateBudgetRequestDto(
        name = name,
        amount = amount,
        startDate = startDate,
        limit = limit,
        currency = currency,
        recurringActive = recurringActive,
        recurringIntervalUnit = recurringIntervalUnit,
        recurringIntervalValue = if (recurringIntervalValue != 0) recurringIntervalValue else null,
        recurringTopupAmount = if (recurringTopupAmount != 0L) recurringTopupAmount else null
    )
}

fun BudgetEntity.toUpdateDto(): Map<String, Any> {
    val dto = mutableMapOf<String, Any>()

    dto["name"] = name
    dto["amount"] = amount
    dto["limit"] = limit
    dto["currency"] = currency
    dto["start_date"] = startDate

    // ===== Recurring =====
    dto["recurring_active"] = recurringActive
    if (recurringActive) {
        dto["recurring_interval_unit"] = recurringIntervalUnit!!
        dto["recurring_interval_value"] = recurringIntervalValue
        dto["recurring_topup_amount"] = recurringTopupAmount
    }

    return dto
}
