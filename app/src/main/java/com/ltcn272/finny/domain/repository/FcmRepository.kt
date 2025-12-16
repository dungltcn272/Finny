package com.ltcn272.finny.domain.repository

import retrofit2.Response

interface FcmRepository {
    suspend fun updateOrCreateFcmToken(token: String)
    suspend fun testNotification(title: String? = null, body: String? = null): Response<Unit>
}
