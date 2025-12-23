package com.ltcn272.finny.data.remote.api

import com.ltcn272.finny.data.remote.dto.ApiResponse
import com.ltcn272.finny.data.remote.dto.NotificationListDataDto
import retrofit2.http.GET
import retrofit2.http.Query

interface NotificationApi {

    @GET("notifications/list")
    suspend fun getNotifications(
        @Query("page") page: Int
    ): ApiResponse<NotificationListDataDto>
}
