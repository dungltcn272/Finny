package com.ltcn272.finny.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.ltcn272.finny.data.mapper.toChat
import com.ltcn272.finny.data.paging.ChatPagingSource
import com.ltcn272.finny.data.remote.api.ChatApi
import com.ltcn272.finny.data.remote.dto.MessageDto
import com.ltcn272.finny.data.remote.dto.SendMessageRequestDto
import com.ltcn272.finny.data.remote.dto.ensureSuccess
import com.ltcn272.finny.domain.model.Chat
import com.ltcn272.finny.domain.repository.ChatRepository
import com.ltcn272.finny.domain.util.AppResult
import com.ltcn272.finny.domain.util.toErrorType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val chatApi: ChatApi
) : ChatRepository {

    override fun getChatMessages(): Flow<PagingData<Chat>> {
        return Pager(
            config = PagingConfig(pageSize = 10, prefetchDistance = 5),
            pagingSourceFactory = { ChatPagingSource(chatApi) }
        ).flow
    }

    override suspend fun sendMessageAndGetResponse(
        text: String?,
        imageUrl: String?
    ): AppResult<Chat> {
        return try {
            //Step 1: Send user message and get message ID
            val userMessageDto = MessageDto(
                text = text?.ifBlank { null },
                image = imageUrl,
                card = emptyList()
            )
            val request = SendMessageRequestDto(message = userMessageDto)
            val userMessageResponse = chatApi.sendMessage(request).ensureSuccess()
            //Step 2: Get AI response using the message ID
            val aiResponse = chatApi.reverseMessage(userMessageResponse.id).ensureSuccess()

            AppResult.Success(aiResponse.toChat())
        } catch (e: Exception) {
            AppResult.Error(e.toErrorType())
        }
    }
}
