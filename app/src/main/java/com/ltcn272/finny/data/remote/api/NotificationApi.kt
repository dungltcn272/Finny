package com.ltcn272.finny.data.remote.api

import com.ltcn272.finny.data.remote.dto.ApiResponse
import com.ltcn272.finny.data.remote.dto.NotificationListDataDto
import com.ltcn272.finny.data.remote.dto.NotificationResponseDto
import com.ltcn272.finny.data.remote.dto.UnreadCountDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApi {

    @GET("notifications/list")
    suspend fun getNotifications(
        @Query("page") page: Int
    ): ApiResponse<NotificationListDataDto>

    @PUT("notifications/{id}")
    suspend fun markAsRead(
        @Path("id") id: String,
        @Body isReadMap: Map<String, Boolean>
    ): ApiResponse<NotificationResponseDto>

    @GET("notifications/count-unread")
    suspend fun getUnreadCount(): ApiResponse<UnreadCountDto>
}
