package com.ltcn272.finny.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.ZonedDateTime

@Parcelize
data class Budget(
    val serverId: String?,
    val name: String,
    val amount: Double,
    val limit: Double,
    val currency: String,
    val startDate: ZonedDateTime,
    val recurring: RecurringConfig?,
    val isSingle: Boolean,

    val totalIncome: Double = 0.0,
    val totalOutcome: Double = 0.0,
    val progress: Double = 0.0,
    val daysRemaining: Double? = null,
    val expectedAvg: Double = 0.0,
    val actualAvg: Double = 0.0,
    val diffAvg: Double = 0.0
) : Parcelable

@Parcelize
data class RecurringConfig(
    val unit: RecurringIntervalUnit,
    val value: Int,
    val topupAmount: Double,
    val nextRunAt: ZonedDateTime?,
    val lastRunAt: ZonedDateTime?
) : Parcelable

enum class RecurringIntervalUnit {
    DAY, WEEK, MONTH, YEAR
}
