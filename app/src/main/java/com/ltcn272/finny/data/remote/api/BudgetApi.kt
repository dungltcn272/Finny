package com.ltcn272.finny.data.remote.api

import com.ltcn272.finny.data.remote.dto.ApiResponse
import com.ltcn272.finny.data.remote.dto.CreateBudgetRequestDto
import com.ltcn272.finny.data.remote.dto.BudgetListDataDto
import com.ltcn272.finny.data.remote.dto.BudgetResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface BudgetApi {

    @POST("budgets/create")
    suspend fun createBudget(
        @Body body: CreateBudgetRequestDto
    ): ApiResponse<BudgetResponseDto>

    @GET("budgets/list")
    suspend fun getBudgets(
        @Query("page") page: Int = 1
    ): ApiResponse<BudgetListDataDto>

    @PUT("budgets/{id}")
    suspend fun updateBudget(
        @Path("id") id: String,
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): ApiResponse<BudgetResponseDto>

    @DELETE("budgets/{id}")
    suspend fun deleteBudget(
        @Path("id") id: String
    ): ApiResponse<Unit>
}