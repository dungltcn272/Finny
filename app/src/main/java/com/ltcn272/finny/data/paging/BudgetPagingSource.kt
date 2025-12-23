package com.ltcn272.finny.data.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.ltcn272.finny.data.mapper.toBudgetDomain
import com.ltcn272.finny.data.remote.api.BudgetApi
import com.ltcn272.finny.domain.model.Budget
import retrofit2.HttpException
import java.io.IOException

class BudgetPagingSource(
    private val budgetApi: BudgetApi
) : PagingSource<Int, Budget>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Budget> {
        val currentPage = params.key ?: 1

        return try {
            val response = budgetApi.getBudgets(page = currentPage)

            val budgets = response.data.data.map { dto ->
                dto.toBudgetDomain()
            }

            val nextKey = if (budgets.isNotEmpty() && response.data.pagination.totalPage > currentPage) {
                currentPage + 1
            } else {
                null
            }

            LoadResult.Page(
                data = budgets,
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

    override fun getRefreshKey(state: PagingState<Int, Budget>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
