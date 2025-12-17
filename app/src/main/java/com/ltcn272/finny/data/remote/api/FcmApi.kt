package com.ltcn272.finny.data.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class FcmTokenRequest(val token: String)
data class FcmTestRequest(val title: String?, val body: String?)

interface FcmApi {
    @POST("token-fcm/update-or-create")
    suspend fun updateOrCreateFcmToken(@Body request: FcmTokenRequest): Response<Unit>

    @POST("token-fcm/test-noti")
    suspend fun testNotification(@Body request: FcmTestRequest): Response<Unit>
}
