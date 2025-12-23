package com.ltcn272.finny.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.ltcn272.finny.data.mapper.toTransactionDomain
import com.ltcn272.finny.data.remote.api.TransactionApi
import com.ltcn272.finny.data.remote.dto.TransactionFilterDto
import com.ltcn272.finny.data.remote.dto.TransactionListRequestDto
import com.ltcn272.finny.domain.model.Transaction
import retrofit2.HttpException
import java.io.IOException

class TransactionPagingSource(
    private val transactionApi: TransactionApi,
    private val filter: TransactionFilterDto?
) : PagingSource<Int, Transaction>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Transaction> {
        val currentPage = params.key ?: 1

        return try {
            val requestDto = TransactionListRequestDto(
                page = currentPage,
                filter = filter
            )

            val response = transactionApi.getTransactions(requestDto)
            val transactions = response.data.data.map { it.toTransactionDomain() }

            val nextKey = if (transactions.isNotEmpty() && response.data.pagination.totalPage > currentPage) {
                currentPage + 1
            } else {
                null
            }

            LoadResult.Page(
                data = transactions,
                prevKey = if (currentPage == 1) null else currentPage - 1,
                nextKey = nextKey
            )
        } catch (e: IOException) {
            return LoadResult.Error(e)
        } catch (e: HttpException) {
            return LoadResult.Error(e)
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Transaction>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
