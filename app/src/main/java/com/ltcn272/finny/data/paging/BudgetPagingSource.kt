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

    private val TAG = "BudgetPagingSource" // <-- THÊM TAG

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Budget> {
        val currentPage = params.key ?: 1
        Log.d(TAG, "Bắt đầu tải trang: $currentPage") // <-- LOG 1

        return try {
            val response = budgetApi.getBudgets(page = currentPage)
            Log.d(TAG, "API call thành công. Response raw: ${response}") // <-- LOG 2

            val budgets = response.data.data.map { dto ->
                dto.toBudgetDomain()
            }
            Log.d(TAG, "Tải thành công ${budgets.size} budget(s) cho trang $currentPage") // <-- LOG 3

            val nextKey = if (budgets.isNotEmpty() && response.data.pagination.totalPage > currentPage) {
                currentPage + 1
            } else {
                null
            }

            Log.d(TAG, "Next page key: $nextKey") // <-- LOG 4

            LoadResult.Page(
                data = budgets,
                prevKey = if (currentPage == 1) null else currentPage - 1,
                nextKey = nextKey
            )
        } catch (e: IOException) {
            Log.e(TAG, "Lỗi mạng (IOException): ${e.message}", e) // <-- LOG LỖI MẠNG
            LoadResult.Error(e)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string() // Đọc nội dung lỗi từ server
            Log.e(TAG, "Lỗi HTTP ${e.code()}: $errorBody", e) // <-- LOG LỖI HTTP
            LoadResult.Error(e)
        } catch (e: Exception) {
            // Bắt các lỗi khác, ví dụ lỗi mapping JSON
            Log.e(TAG, "Lỗi không xác định: ${e.message}", e) // <-- LOG LỖI CHUNG
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
