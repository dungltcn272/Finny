package com.ltcn272.finny.domain.model

import java.time.LocalDateTime

data class MessageCard(
    val type: String,
    val amount: Double,
    val description: String,
    val budgetId: String,
    val categoryId: String
)

data class Chat(
    val id: String,
    val isFromUser: Boolean,
    val text: String,
    val image: String?,
    val cards: List<MessageCard>,
    val timestamp: LocalDateTime
)
