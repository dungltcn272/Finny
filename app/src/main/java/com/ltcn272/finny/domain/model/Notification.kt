package com.ltcn272.finny.domain.model

import java.time.ZonedDateTime

data class Notification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val description: String,
    val meta: NotificationMeta?,
    val isRead: Boolean,
    val createdAt: ZonedDateTime
)

data class NotificationMeta(
    val budgetId: String?
)

enum class NotificationType {
    TRANSACTION_CREATED,
    TRANSACTION_UPDATED,
    TRANSACTION_DELETED,
    BUDGET_CREATED,
    BUDGET_THRESHOLD_REACHED,
    BUDGET_PERIOD_END,
    INCOME_AUTO_RECURRING,
    UNKNOWN
}
