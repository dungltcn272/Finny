package com.ltcn272.finny.data.mapper

import com.ltcn272.finny.data.remote.dto.CreateTransactionRequestDto
import com.ltcn272.finny.data.remote.dto.RecurringTransactionResponseDto
import com.ltcn272.finny.data.remote.dto.TransactionFilterDto
import com.ltcn272.finny.data.remote.dto.TransactionResponseDto
import com.ltcn272.finny.domain.model.RecurringIntervalUnit
import com.ltcn272.finny.domain.model.RecurringTransaction
import com.ltcn272.finny.domain.model.Transaction
import com.ltcn272.finny.domain.model.TransactionFilter
import com.ltcn272.finny.domain.model.TransactionType
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun TransactionResponseDto.toTransactionDomain(): Transaction {
    return Transaction(
        serverId = id,
        name = name,
        budgetId = budgetId,
        type = TransactionType.valueOf(type.uppercase()),
        amount = amount,
        dateTime = ZonedDateTime.parse(dateTime, DateTimeFormatter.ISO_ZONED_DATE_TIME),
        description = description,
        image = image,
        category = this.category?.toCategoryDomain(),
        isRecurring = false,
        recurringInfo = null
    )
}

fun Transaction.toCreateDto(): CreateTransactionRequestDto {
    val categoryIdToSend = category?.serverId
        ?: throw IllegalArgumentException("Category ID cannot be empty to create a transaction")

    // --- CORRECT THE TIME FORMATTING ---
    val utcDateTime =
        dateTime.withZoneSameInstant(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_INSTANT)
    val utcRecurringStartDate = if (isRecurring) {
        recurringInfo?.startDate?.withZoneSameInstant(ZoneId.of("UTC"))
            ?.format(DateTimeFormatter.ISO_INSTANT)
    } else null
    // ---------------------------------

    return CreateTransactionRequestDto(
        name = name,
        budgetId = budgetId,
        type = type.name.lowercase(),
        amount = amount,
        categoryId = categoryIdToSend,
        dateTime = utcDateTime,
        description = description,
        image = image,
        isRecurring = isRecurring,
        recurringStartDate = utcRecurringStartDate,
        recurringIntervalUnit = if (isRecurring) recurringInfo?.intervalUnit?.name?.lowercase() else null,
        recurringIntervalValue = if (isRecurring) recurringInfo?.intervalValue else null
    )
}

fun Transaction.toUpdateMap(): Map<String, Any?> {
    val utcDateTime =
        dateTime.withZoneSameInstant(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_INSTANT)
    val utcRecurringStartDate = if (isRecurring) {
        recurringInfo?.startDate?.withZoneSameInstant(ZoneId.of("UTC"))
            ?.format(DateTimeFormatter.ISO_INSTANT)
    } else null

    return mutableMapOf(
        "name" to name,
        "budget_id" to budgetId,
        "type" to type.name.lowercase(),
        "amount" to amount,
        "category_id" to (category?.serverId
            ?: throw IllegalArgumentException("Category ID cannot be empty for update")),
        "date_time" to utcDateTime,
        "description" to description,
        "image" to image,
        "is_recurring" to isRecurring,
        "recurring_start_date" to utcRecurringStartDate,
        "recurring_interval_unit" to if (isRecurring) recurringInfo?.intervalUnit?.name?.lowercase() else null,
        "recurring_interval_value" to if (isRecurring) recurringInfo?.intervalValue else null
    )
}


fun RecurringTransactionResponseDto.toRecurringTransactionDomain(): RecurringTransaction {
    return RecurringTransaction(
        serverId = id,
        name = name,
        budgetId = budgetId,
        type = TransactionType.valueOf(type.uppercase()),
        amount = amount,
        description = description,
        image = image,
        startDate = ZonedDateTime.parse(startDate, DateTimeFormatter.ISO_ZONED_DATE_TIME),
        intervalUnit = RecurringIntervalUnit.valueOf(intervalUnit.uppercase()),
        intervalValue = intervalValue,
        nextRunAt = nextRunAt?.let {
            ZonedDateTime.parse(
                it,
                DateTimeFormatter.ISO_ZONED_DATE_TIME
            )
        },
        lastRunAt = lastRunAt?.let {
            ZonedDateTime.parse(
                it,
                DateTimeFormatter.ISO_ZONED_DATE_TIME
            )
        },
        active = active,
        category = this.category?.toCategoryDomain()
    )
}

fun TransactionFilter.toFilterDto(): TransactionFilterDto {
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    return TransactionFilterDto(
        startDate = this.startDate?.format(formatter),
        endDate = this.endDate?.format(formatter),
        budgetId = this.budgetId
    )
}