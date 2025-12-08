package com.ltcn272.finny.domain.repository

import com.ltcn272.finny.domain.model.User
import com.ltcn272.finny.domain.util.AppResult
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    suspend fun getProfile(): Flow<AppResult<User>>
    suspend fun updateProfile(displayName: String, currency: String?, lang: String?): AppResult<User>
}
