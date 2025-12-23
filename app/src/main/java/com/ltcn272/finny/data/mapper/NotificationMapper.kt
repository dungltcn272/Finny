package com.ltcn272.finny.data.mapper

import com.ltcn272.finny.data.remote.dto.NotificationMetaDto
import com.ltcn272.finny.data.remote.dto.NotificationResponseDto
import com.ltcn272.finny.domain.model.Notification
import com.ltcn272.finny.domain.model.NotificationMeta
import com.ltcn272.finny.domain.model.NotificationType
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun NotificationResponseDto.toNotificationDomain(): Notification {
    val notificationType = try {
        NotificationType.valueOf(type.uppercase())
    } catch (e: IllegalArgumentException) {
        NotificationType.UNKNOWN
    }

    return Notification(
        id = id,
        type = notificationType,
        title = title,
        description = description,
        meta = meta?.toNotificationMetaDomain(),
        isRead = isRead,
        createdAt = ZonedDateTime.parse(createdAt, DateTimeFormatter.ISO_ZONED_DATE_TIME)
    )
}

fun NotificationMetaDto.toNotificationMetaDomain(): NotificationMeta {
    return NotificationMeta(
        budgetId = budgetId
    )
}
