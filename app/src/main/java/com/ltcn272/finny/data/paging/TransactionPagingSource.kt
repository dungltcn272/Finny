package com.ltcn272.finny.data.paging

import android.util.Log
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

    private val TAG = "TransactionPagingSource" // THÊM TAG

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Transaction> {
        val currentPage = params.key ?: 1
        Log.d(TAG, "Bắt đầu tải trang: $currentPage với params.loadSize = ${params.loadSize}")

        return try {
            val requestDto = TransactionListRequestDto(
                page = currentPage,
                filter = filter
            )
            Log.d(TAG, "Đang gửi request DTO: $requestDto") // LOG REQUEST

            val response = transactionApi.getTransactions(requestDto)
            val transactions = response.data.data.map { it.toTransactionDomain() }
            Log.d(TAG, "Tải thành công ${transactions.size} transaction(s) cho trang $currentPage")

            val nextKey = if (transactions.isNotEmpty() && response.data.pagination.totalPage > currentPage) {
                currentPage + 1
            } else {
                null
            }
            Log.d(TAG, "Next page key: $nextKey")

            LoadResult.Page(
                data = transactions,
                prevKey = if (currentPage == 1) null else currentPage - 1,
                nextKey = nextKey
            )
        } catch (e: IOException) {
            Log.e(TAG, "Lỗi mạng (IOException): ${e.message}", e)
            return LoadResult.Error(e)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e(TAG, "Lỗi HTTP ${e.code()}: $errorBody", e)
            return LoadResult.Error(e)
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi không xác định trong PagingSource: ${e.message}", e)
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
