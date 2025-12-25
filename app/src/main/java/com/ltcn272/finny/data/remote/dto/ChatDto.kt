package com.ltcn272.finny.data.remote.dto

import com.google.gson.annotations.SerializedName


data class ChatListDataDto(
    @SerializedName("data")
    val data: List<ChatDto>,
    @SerializedName("pagination")
    val pagination: PaginationDto
)
data class CardDto(
    @SerializedName("type") val type: String?,
    @SerializedName("amount") val amount: Double?,
    @SerializedName("description") val description: String?,
    @SerializedName("budget_id") val budgetId: String?,
    @SerializedName("category_id") val categoryId: String?
)

data class MessageDto(
    @SerializedName("text") val text: String?,
    @SerializedName("card") val card: List<CardDto>?
)

data class ChatDto(
    @SerializedName("_id") val id: String,
    @SerializedName("user_id") val userId: String?,
    @SerializedName("is_me") val isMe: Boolean?,
    @SerializedName("message") val message: MessageDto?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)

data class SendMessageRequestDto(
    @SerializedName("message") val message: MessageDto
)
