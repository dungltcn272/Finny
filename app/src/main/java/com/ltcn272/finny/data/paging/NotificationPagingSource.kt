package com.ltcn272.finny.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.ltcn272.finny.data.mapper.toNotificationDomain
import com.ltcn272.finny.data.remote.api.NotificationApi
import com.ltcn272.finny.domain.model.Notification
import retrofit2.HttpException
import java.io.IOException

class NotificationPagingSource(
    private val notificationApi: NotificationApi
) : PagingSource<Int, Notification>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Notification> {
        val currentPage = params.key ?: 1
        return try {
            val response = notificationApi.getNotifications(page = currentPage)
            val notifications = response.data.data.map { it.toNotificationDomain() }

            val nextKey = if (notifications.isNotEmpty() && response.data.pagination.totalPage > currentPage) {
                currentPage + 1
            } else {
                null
            }

            LoadResult.Page(
                data = notifications,
                prevKey = if (currentPage == 1) null else currentPage - 1,
                nextKey = nextKey
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Notification>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
