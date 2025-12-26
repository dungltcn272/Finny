package com.ltcn272.finny.domain.repository

import com.ltcn272.finny.domain.model.User
import com.ltcn272.finny.domain.util.AppResult
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getProfile(): Flow<AppResult<User>>
    fun updateProfile(data: Map<String, Any>): Flow<AppResult<User>>
}
