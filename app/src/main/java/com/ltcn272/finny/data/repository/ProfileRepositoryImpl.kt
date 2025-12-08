package com.ltcn272.finny.data.repository

import com.ltcn272.finny.data.mapper.toDomain
import com.ltcn272.finny.data.remote.api.ProfileApi
import com.ltcn272.finny.data.remote.dto.UpdateUserRequestDto
import com.ltcn272.finny.domain.model.User
import com.ltcn272.finny.domain.repository.ProfileRepository
import com.ltcn272.finny.domain.util.AppResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(private val profileApi: ProfileApi) : ProfileRepository {
    override suspend fun getProfile(): Flow<AppResult<User>> = flow {
        emit(AppResult.Loading)
        try {
            val response = profileApi.getProfile()
            // ProfileResponse contains data: UserDto
            val userDto = response.data
            emit(AppResult.Success(userDto.toDomain()))
        } catch (e: Exception) {
            emit(AppResult.Error(e.localizedMessage ?: "Unknown error"))
        }
    }

    override suspend fun updateProfile(displayName: String, currency: String?, lang: String?): AppResult<User> {
        return try {
            val body = UpdateUserRequestDto(
                displayName = displayName,
                avatar = null,
                currency = currency,
                lang = lang
            )
            val response = profileApi.updateProfile(body)
            if (response.status == 200 && response.data != null) {
                AppResult.Success(response.data.toDomain())
            } else {
                AppResult.Error(response.message)
            }
        } catch (e: Exception) {
            AppResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }
}
