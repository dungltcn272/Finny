package com.ltcn272.finny.domain.repository

import com.ltcn272.finny.domain.util.AppResult
import kotlinx.coroutines.flow.Flow

interface FcmRepository {
    fun updateOrCreateFcmToken(token: String): Flow<AppResult<Unit>>
}
