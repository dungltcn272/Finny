package com.ltcn272.finny.data.repository

import com.ltcn272.finny.data.remote.api.FcmApi
import com.ltcn272.finny.data.remote.api.FcmTestRequest
import com.ltcn272.finny.data.remote.api.FcmTokenRequest
import com.ltcn272.finny.domain.repository.FcmRepository
import retrofit2.Response
import javax.inject.Inject

class FcmRepositoryImpl @Inject constructor(
    private val fcmApi: FcmApi
) : FcmRepository {
    override suspend fun updateOrCreateFcmToken(token: String) {
        fcmApi.updateOrCreateFcmToken(FcmTokenRequest(token))
    }

    override suspend fun testNotification(title: String?, body: String?): Response<Unit> {
        val request = FcmTestRequest(title, body)
        return fcmApi.testNotification(request)
    }
}
