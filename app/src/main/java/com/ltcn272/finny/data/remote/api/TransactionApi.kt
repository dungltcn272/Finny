package com.ltcn272.finny.data.remote.api

import com.ltcn272.finny.data.remote.dto.*
import retrofit2.http.*

interface TransactionApi {

    @POST("transactions/list")
    suspend fun getTransactions(
        @Body body: TransactionListRequestDto
    ): ApiResponse<TransactionListDataDto>

    @GET("recurring-transactions")
    suspend fun getRecurringTransactions(
        @Query("page") page: Int
    ): ApiResponse<RecurringTransactionListDataDto>

    @POST("transactions/create")
    suspend fun createTransaction(
        @Body body: CreateTransactionRequestDto
    ): ApiResponse<TransactionResponseDto>


    @PUT("transactions/{id}")
    suspend fun updateTransaction(
        @Path("id") id: String,
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): ApiResponse<TransactionResponseDto>

    @DELETE("transactions/{id}")
    suspend fun deleteTransaction(
        @Path("id") id: String
    ): ApiResponse<Unit>

    @DELETE("recurring-transactions/{id}")
    suspend fun deleteRecurringTransaction(
        @Path("id") id: String
    ): ApiResponse<Unit>
}
