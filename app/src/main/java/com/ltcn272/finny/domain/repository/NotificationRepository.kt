package com.ltcn272.finny.domain.repository

import androidx.paging.PagingData
import com.ltcn272.finny.domain.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotifications(): Flow<PagingData<Notification>>
}
