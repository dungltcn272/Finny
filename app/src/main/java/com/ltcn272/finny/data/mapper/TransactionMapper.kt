
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
    return CreateTransactionRequestDto(
        name = name,
        budgetId = budgetId,
        type = type.name.lowercase(),
        amount = amount,
        categoryId = category?.serverId
            ?: throw IllegalArgumentException("Category ID không được rỗng để tạo giao dịch"),

        dateTime = dateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME),
        isRecurring = isRecurring,
        recurringStartDate = if (isRecurring) recurringInfo?.startDate?.format(DateTimeFormatter.ISO_ZONED_DATE_TIME) else null,
        recurringIntervalUnit = if (isRecurring) recurringInfo?.intervalUnit?.name?.lowercase() else null,
        recurringIntervalValue = if (isRecurring) recurringInfo?.intervalValue else null,
        description = description,
        image = image
    )
}

fun Transaction.toUpdateMap(): Map<String, Any?> {
    return mutableMapOf(
        "name" to name,
        "budget_id" to budgetId,
        "type" to type.name.lowercase(),
        "amount" to amount,
        "category_id" to (category?.serverId
            ?: throw IllegalArgumentException("Category ID không được rỗng để cập nhật")),
        "date_time" to dateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME),
        "description" to description,
        "image" to image,
        "is_recurring" to isRecurring,
        "recurring_start_date" to if (isRecurring) recurringInfo?.startDate?.format(DateTimeFormatter.ISO_ZONED_DATE_TIME) else null,
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
        nextRunAt = nextRunAt?.let { ZonedDateTime.parse(it, DateTimeFormatter.ISO_ZONED_DATE_TIME) },
        lastRunAt = lastRunAt?.let { ZonedDateTime.parse(it, DateTimeFormatter.ISO_ZONED_DATE_TIME) },
        active = active,
        category = this.category?.toCategoryDomain()
    )
}

fun TransactionFilter.toFilterDto(): TransactionFilterDto {
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    return TransactionFilterDto(
        startDate = this.startDate?.format(formatter),
        endDate = this.endDate?.format(formatter),
        category = this.categoryId
    )
}