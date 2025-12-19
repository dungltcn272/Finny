package com.ltcn272.finny.data.mapper

import com.ltcn272.finny.data.remote.dto.CreateBudgetRequestDto
import com.ltcn272.finny.data.remote.dto.BudgetResponseDto
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.domain.model.RecurringConfig
import com.ltcn272.finny.domain.model.RecurringIntervalUnit
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME


fun BudgetResponseDto.toBudgetDomain(): Budget = Budget(
    serverId = id,
    name = name,
    amount = amount,
    limit = limit,
    currency = currency,
    startDate = ZonedDateTime.parse(startDate, ISO_ZONED_DATE_TIME),
    recurring = if (recurringActive) {
        RecurringConfig(
            unit = RecurringIntervalUnit.valueOf(recurringIntervalUnit!!.uppercase()),
            value = recurringIntervalValue,
            topupAmount = recurringTopupAmount,
            nextRunAt = recurringNextRunAt?.let { ZonedDateTime.parse(it,
                ISO_ZONED_DATE_TIME) },
            lastRunAt = recurringLastRunAt?.let { ZonedDateTime.parse(it,
                ISO_ZONED_DATE_TIME) }
        )
    } else null,
    isSingle = isSingle,

    totalIncome = totalIncome,
    totalOutcome = totalOutcome,
    progress = progress,
    daysRemaining = daysRemaining,
    expectedAvg = expectedAvg
)

fun Budget.toCreateRequestDto(): CreateBudgetRequestDto {
    return CreateBudgetRequestDto(
        name = name,
        amount = amount,
        startDate = startDate.format(ISO_ZONED_DATE_TIME),
        limit = limit,
        currency = currency,
        recurringActive = recurring != null,
        recurringIntervalUnit = recurring?.unit?.name,
        recurringIntervalValue = recurring?.value,
        recurringTopupAmount = recurring?.topupAmount
    )
}

fun Budget.toUpdateMap(): Map<String, Any?> {
    val dto = mutableMapOf<String, Any?>()

    dto["name"] = name
    dto["amount"] = amount
    dto["limit"] = limit
    dto["currency"] = currency
    dto["start_date"] = startDate.format(ISO_ZONED_DATE_TIME)

    dto["recurring_active"] = recurring != null
    if (recurring != null) {
        dto["recurring_interval_unit"] = recurring.unit.name.lowercase()
        dto["recurring_interval_value"] = recurring.value
        dto["recurring_topup_amount"] = recurring.topupAmount
    } else {
        dto["recurring_interval_unit"] = null
        dto["recurring_interval_value"] = null
        dto["recurring_topup_amount"] = null
    }

    return dto
}
