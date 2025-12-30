package com.ltcn272.finny.domain.repository

import androidx.paging.PagingData
import com.ltcn272.finny.domain.model.Chat
import com.ltcn272.finny.domain.model.MessageCard
import com.ltcn272.finny.domain.util.AppResult
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getChatMessages(): Flow<PagingData<Chat>>
    suspend fun sendMessageAndGetResponse(text: String): AppResult<List<MessageCard>>
}