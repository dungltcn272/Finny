package com.ltcn272.finny.data.mapper

import com.ltcn272.finny.data.remote.dto.ChatDto
import com.ltcn272.finny.domain.model.Chat
import com.ltcn272.finny.domain.model.MessageCard
import com.ltcn272.finny.presentation.common.util.parseUtcString
import java.time.LocalDateTime
import kotlin.collections.mapNotNull

fun ChatDto.toChat(): Chat {
    return Chat(
        id = this.id,
        isFromUser = this.isMe ?: false,
        text = this.message?.text ?: "",
        cards = this.message?.card?.mapNotNull { cardDto ->
            if (cardDto.type != null && cardDto.amount != null && cardDto.description != null && cardDto.budgetId != null && cardDto.categoryId != null) {
                MessageCard(
                    type = cardDto.type,
                    amount = cardDto.amount,
                    description = cardDto.description,
                    budgetId = cardDto.budgetId,
                    categoryId = cardDto.categoryId
                )
            } else {
                null
            }
        } ?: kotlin.collections.emptyList(),
        timestamp = this.createdAt?.let { parseUtcString(it) } ?: LocalDateTime.now()
    )
}
