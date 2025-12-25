package com.ltcn272.finny.data.remote.api

import com.ltcn272.finny.data.remote.dto.ApiResponse
import com.ltcn272.finny.data.remote.dto.ChatDto
import com.ltcn272.finny.data.remote.dto.ChatListDataDto
import com.ltcn272.finny.data.remote.dto.SendMessageRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatApi {

    @GET("chats")
    suspend fun getChats(
        @Query("page") page: Int
    ): ApiResponse<ChatListDataDto>
    @POST("chats")
    suspend fun sendMessage(
        @Body request: SendMessageRequestDto
    ): ApiResponse<ChatDto>

    @POST("chats/{id}/reverse")
    suspend fun reverseMessage(
        @Path("id") id: String
    ): ApiResponse<ChatDto>
}
