package com.ltcn272.finny.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.ltcn272.finny.data.paging.NotificationPagingSource
import com.ltcn272.finny.data.remote.api.NotificationApi
import com.ltcn272.finny.data.remote.dto.ensureSuccess
import com.ltcn272.finny.domain.model.Notification
import com.ltcn272.finny.domain.repository.NotificationRepository
import com.ltcn272.finny.domain.util.AppResult
import com.ltcn272.finny.domain.util.toErrorType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val notificationApi: NotificationApi
) : NotificationRepository {

    override fun getNotifications(): Flow<PagingData<Notification>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { NotificationPagingSource(notificationApi) }
        ).flow
    }

    override suspend fun markAsRead(notificationId: String): AppResult<Unit> {
        return try {
            val body = mapOf("is_read" to true)
            notificationApi.markAsRead(notificationId, body).ensureSuccess()
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.toErrorType())
        }
    }
}
