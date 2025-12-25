package com.ltcn272.finny.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.ltcn272.finny.data.mapper.toChat
import com.ltcn272.finny.data.remote.api.ChatApi
import com.ltcn272.finny.domain.model.Chat
import retrofit2.HttpException
import java.io.IOException

class ChatPagingSource(private val chatApi: ChatApi) : PagingSource<Int, Chat>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Chat> {
        val page = params.key ?: 1
        return try {
            val response = chatApi.getChats(page = page)
            val chats = response.data.data.map { it.toChat() }

            val sortedChats = chats.sortedByDescending { it.timestamp }

            LoadResult.Page(
                data = sortedChats,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (chats.isEmpty()) null else page + 1
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Chat>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
