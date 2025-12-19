package com.ltcn272.finny.data.remote.api

import com.ltcn272.finny.data.remote.dto.ApiResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface FcmApi {
    @POST("token-fcm/update-or-create")
    suspend fun updateOrCreateFcmToken(@Body body: Map<String, String>): ApiResponse<Unit>
}
