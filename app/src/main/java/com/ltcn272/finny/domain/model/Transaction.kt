package com.ltcn272.finny.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.ZonedDateTime

@Parcelize
data class Transaction(
    val serverId: String,
    val name: String,
    val budgetId: String,
    val type: TransactionType,
    val amount: Double,
    val dateTime: ZonedDateTime,
    val description: String?,
    val image: String?,

    val category: Category?,

    val isRecurring: Boolean,

    val recurringInfo: RecurringTransactionInfo?
) : Parcelable

enum class TransactionType {
    INCOME,
    OUTCOME
}

@Parcelize
data class RecurringTransactionInfo(
    val startDate: ZonedDateTime,
    val intervalUnit: RecurringIntervalUnit,
    val intervalValue: Int
) : Parcelable

@Parcelize
data class RecurringTransaction(
    val serverId: String,
    val name: String,
    val budgetId: String,
    val type: TransactionType,
    val amount: Double,
    val description: String?,
    val image: String?,
    val active: Boolean,

    val startDate: ZonedDateTime,
    val intervalUnit: RecurringIntervalUnit,
    val intervalValue: Int,

    val nextRunAt: ZonedDateTime?,
    val lastRunAt: ZonedDateTime?,

    val category: Category?
) : Parcelable

data class TransactionFilter(
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val budgetId: String? = null
)

