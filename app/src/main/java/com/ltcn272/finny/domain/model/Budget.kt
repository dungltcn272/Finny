package com.ltcn272.finny.domain.model

data class Budget(
    val localId: String?,
    val serverId: String?,
    val name: String,
    val amount: Long,
    val limit: Long,
    val currency: String,

    val startDate: String,

    // Recurring config
    val recurring: RecurringConfig?,

    val isSingle: Boolean
)

data class RecurringConfig(
    val unit: RecurringIntervalUnit,
    val value: Int,
    val topupAmount: Long,
    val nextRunAt: String?,
    val lastRunAt: String?
)

enum class RecurringIntervalUnit {
    DAY, WEEK, MONTH, YEAR
}
