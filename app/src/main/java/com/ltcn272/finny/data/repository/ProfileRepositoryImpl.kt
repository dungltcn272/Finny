package com.ltcn272.finny.data.repository

import com.ltcn272.finny.data.mapper.toUpdateMap
import com.ltcn272.finny.data.mapper.toUserDomain
import com.ltcn272.finny.data.remote.api.ProfileApi
import com.ltcn272.finny.domain.model.User
import com.ltcn272.finny.domain.repository.ProfileRepository
import com.ltcn272.finny.domain.util.AppResult
import com.ltcn272.finny.domain.util.toErrorType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val profileApi: ProfileApi
) : ProfileRepository {

    override fun getProfile(): Flow<AppResult<User>> = flow {
        emit(AppResult.Loading)
        try {
            val response = profileApi.getProfile()
            emit(AppResult.Success(response.data.toUserDomain()))
        } catch (e: Exception) {
            emit(AppResult.Error(e.toErrorType()))
        }
    }

    override fun updateProfile(user: User): Flow<AppResult<User>> = flow {
        emit(AppResult.Loading)
        try {
            val requestBody = user.toUpdateMap()
            val response = profileApi.updateProfile(requestBody)
            emit(AppResult.Success(response.data.toUserDomain()))
        } catch (e: Exception) {
            emit(AppResult.Error(e.toErrorType()))
        }
    }

}
