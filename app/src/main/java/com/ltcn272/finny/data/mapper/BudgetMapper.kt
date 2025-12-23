package com.ltcn272.finny.data.mapper

import com.ltcn272.finny.data.remote.dto.CreateBudgetRequestDto
import com.ltcn272.finny.data.remote.dto.BudgetResponseDto
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.domain.model.RecurringConfig
import com.ltcn272.finny.domain.model.RecurringIntervalUnit
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private val RESPONSE_DATE_FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME

fun BudgetResponseDto.toBudgetDomain(): Budget {
    val recurringConfig = if (recurringActive) {
        try {
            RecurringConfig(
                unit = RecurringIntervalUnit.valueOf(recurringIntervalUnit.uppercase()),
                value = recurringIntervalValue,
                topupAmount = recurringTopupAmount,
                nextRunAt = recurringNextRunAt?.let { ZonedDateTime.parse(it, RESPONSE_DATE_FORMATTER) },
                lastRunAt = recurringLastRunAt?.let { ZonedDateTime.parse(it, RESPONSE_DATE_FORMATTER) }
            )
        } catch (e: IllegalArgumentException) {
            null
        }
    } else {
        null
    }

    return Budget(
        serverId = id,
        name = name,
        amount = amount,
        limit = limit,
        currency = "VND",
        startDate = ZonedDateTime.parse(startDate, RESPONSE_DATE_FORMATTER),
        recurring = recurringConfig,
        isSingle = recurringConfig == null,
        totalIncome = totalIncome,
        totalOutcome = totalOutcome,
        progress = progress,
        daysRemaining = daysRemaining,
        expectedAvg = expectedAvg,
        actualAvg = actualAvg,
        diffAvg = diffAvg
    )
}


fun Budget.toCreateRequestDto(): CreateBudgetRequestDto {
    val utcStartDate = startDate.withZoneSameInstant(ZoneId.of("UTC"))
        .format(DateTimeFormatter.ISO_INSTANT)

    return CreateBudgetRequestDto(
        name = name,
        amount = amount,
        startDate = utcStartDate,
        limit = limit,
        recurringActive = recurring != null,
        recurringIntervalUnit = recurring?.unit?.name?.lowercase(),
        recurringIntervalValue = recurring?.value,
        recurringTopupAmount = recurring?.topupAmount
    )
}

fun Budget.toUpdateMap(): Map<String, Any?> {
    val dto = mutableMapOf<String, Any?>()

    val utcStartDate = startDate.withZoneSameInstant(ZoneId.of("UTC"))
        .format(DateTimeFormatter.ISO_INSTANT)

    dto["name"] = name
    dto["amount"] = amount
    dto["limit"] = limit
    dto["start_date"] = utcStartDate

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
