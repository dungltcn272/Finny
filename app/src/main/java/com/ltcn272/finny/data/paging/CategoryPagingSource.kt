package com.ltcn272.finny.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.ltcn272.finny.data.mapper.toCategoryDomain
import com.ltcn272.finny.data.remote.api.CategoryApi
import com.ltcn272.finny.domain.model.Category
import retrofit2.HttpException
import java.io.IOException

class CategoryPagingSource(
    private val categoryApi: CategoryApi
) : PagingSource<Int, Category>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Category> {
        val currentPage = params.key ?: 1
        return try {
            val apiResponse = categoryApi.getList(page = currentPage)
            val categories = apiResponse.data.data.map { it.toCategoryDomain() }
            val nextKey = if (categories.isNotEmpty() && apiResponse.data.pagination.totalPage > currentPage) {
                currentPage + 1
            } else {
                null
            }

            LoadResult.Page(
                data = categories,
                prevKey = if (currentPage == 1) null else currentPage - 1,
                nextKey = nextKey
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Category>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
