package com.ltcn272.finny.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.ZonedDateTime

@Parcelize
data class Budget(
    val serverId: String?,
    val name: String,
    val amount: Long,
    val limit: Long,
    val currency: String,
    val startDate: ZonedDateTime,
    val recurring: RecurringConfig?,
    val isSingle: Boolean,

    val totalIncome: Long,
    val totalOutcome: Long,
    val progress: Float,
    val daysRemaining: Int?,
    val expectedAvg: Long
) : Parcelable

@Parcelize
data class RecurringConfig(
    val unit: RecurringIntervalUnit,
    val value: Int,
    val topupAmount: Long,
    val nextRunAt: ZonedDateTime?,
    val lastRunAt: ZonedDateTime?
) : Parcelable

enum class RecurringIntervalUnit {
    DAY, WEEK, MONTH, YEAR
}
