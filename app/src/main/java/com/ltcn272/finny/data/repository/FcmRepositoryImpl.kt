package com.ltcn272.finny.data.repository

import com.ltcn272.finny.data.remote.api.FcmApi
import com.ltcn272.finny.domain.repository.FcmRepository
import com.ltcn272.finny.domain.util.AppResult
import com.ltcn272.finny.domain.util.toErrorType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FcmRepositoryImpl @Inject constructor(
    private val fcmApi: FcmApi
) : FcmRepository {

    override fun updateOrCreateFcmToken(token: String): Flow<AppResult<Unit>> = flow {
        emit(AppResult.Loading)
        try {
            val requestBody = mapOf("token" to token)
            fcmApi.updateOrCreateFcmToken(requestBody)
            emit(AppResult.Success(Unit))
        } catch (e: Exception) {
            emit(AppResult.Error(e.toErrorType()))
        }
    }
}
