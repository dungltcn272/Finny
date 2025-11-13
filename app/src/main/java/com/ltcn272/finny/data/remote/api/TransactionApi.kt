package com.ltcn272.finny.data.remote.api

import com.ltcn272.finny.data.remote.dto.ApiResponseDto
import com.ltcn272.finny.data.remote.dto.CreateTransactionRequestDto
import com.ltcn272.finny.data.remote.dto.TransactionDto
import com.ltcn272.finny.data.remote.dto.TransactionListResponseDto
import com.ltcn272.finny.data.remote.dto.TransactionFilterRequestDto
import com.ltcn272.finny.data.remote.dto.UploadImageResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface TransactionApi {

    // case .transactionList -> POST /transactions/list (TransactionService.swift)
    @POST("transactions/list")
    suspend fun getTransactions(@Body body: TransactionFilterRequestDto): ApiResponseDto<TransactionListResponseDto>

    // case .transactionCreate -> POST /transactions/create (TransactionService.swift)
    @POST("transactions/create")
    suspend fun createTransaction(@Body body: CreateTransactionRequestDto): ApiResponseDto<TransactionDto>

    // case .transactionUpdate(id: String) -> PUT /transactions/{id} (TransactionService.swift)
    @PUT("transactions/{id}")
    suspend fun updateTransaction(
        @Path("id") id: String,
        @Body body: CreateTransactionRequestDto // <-- Chấp nhận DTO
    ): ApiResponseDto<TransactionDto>
    // case .transactionDelete(id: String) -> DELETE /transactions/{id} (TransactionService.swift)
    @DELETE("transactions/{id}")
    suspend fun deleteTransaction(@Path("id") id: String): ApiResponseDto<Unit>

    // case .uploadImage -> POST /upload/image (TransactionService.swift)
    // Multipart cho việc upload ảnh
    @Multipart
    @POST("upload/image")
    suspend fun uploadImage(@Part image: MultipartBody.Part): ApiResponseDto<UploadImageResponseDto>
}