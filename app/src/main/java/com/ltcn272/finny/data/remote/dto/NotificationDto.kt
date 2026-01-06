package com.ltcn272.finny.data.remote.dto

import com.google.gson.annotations.SerializedName

data class NotificationListDataDto(
    @SerializedName("data")
    val data: List<NotificationResponseDto>,
    @SerializedName("pagination")
    val pagination: PaginationDto
)

data class NotificationResponseDto(
    @SerializedName("_id")
    val id: String,

    @SerializedName("user_id")
    val userId: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("meta")
    val meta: NotificationMetaDto?,

    @SerializedName("is_read")
    val isRead: Boolean,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String
)

data class NotificationMetaDto(
    @SerializedName("budget_id")
    val budgetId: String?,

    @SerializedName("limit")
    val limit: Double?
)

data class UnreadCountDto(
    @SerializedName("total_unread")
    val totalUnread: Int
)