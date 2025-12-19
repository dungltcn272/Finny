package com.ltcn272.finny.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.ltcn272.finny.data.mapper.toRecurringTransactionDomain
import com.ltcn272.finny.data.remote.api.TransactionApi
import com.ltcn272.finny.domain.model.RecurringTransaction
import retrofit2.HttpException
import java.io.IOException

class RecurringTransactionPagingSource(
    private val transactionApi: TransactionApi,
) : PagingSource<Int, RecurringTransaction>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, RecurringTransaction> {
        val currentPage = params.key ?: 1
        return try {
            val response = transactionApi.getRecurringTransactions(page = currentPage)
            val recurringTransactionDtos = response.data.data

            val recurringTransactions =
                recurringTransactionDtos.map { it.toRecurringTransactionDomain() }

            val nextKey =
                if (recurringTransactions.isNotEmpty() && response.data.pagination.totalPage > currentPage) {
                    currentPage + 1
                } else {
                    null
                }

            LoadResult.Page(
                data = recurringTransactions,
                prevKey = if (currentPage == 1) null else currentPage - 1,
                nextKey = nextKey
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, RecurringTransaction>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
