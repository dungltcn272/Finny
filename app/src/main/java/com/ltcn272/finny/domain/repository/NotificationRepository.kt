package com.ltcn272.finny.domain.repository

import androidx.paging.PagingData
import com.ltcn272.finny.domain.model.Notification
import com.ltcn272.finny.domain.util.AppResult
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotifications(): Flow<PagingData<Notification>>
    suspend fun markAsRead(notificationId: String): AppResult<Unit>
    suspend fun getUnreadCount(): AppResult<Int>
}
