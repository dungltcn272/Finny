package com.ltcn272.finny.data.remote.api

import com.ltcn272.finny.data.remote.dto.BudgetDto
import com.ltcn272.finny.data.remote.dto.CreateBudgetRequestDto
import com.ltcn272.finny.data.remote.dto.ApiResponseDto
import com.ltcn272.finny.data.remote.dto.BudgetDataDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface BudgetApi {
    // case .budgetList(page: Int) -> GET /budgets/list (BudgetService.swift)
    @GET("budgets/list")
    suspend fun getBudgets(@Query("page") page: Int): ApiResponseDto<BudgetDataDto>

    // case .budgetCreate -> POST /budgets/create (BudgetService.swift)
    @POST("budgets/create")
    suspend fun createBudget(@Body body: CreateBudgetRequestDto): ApiResponseDto<BudgetDto>

    // case .budgetUpdate(id: String) -> PUT /budgets/{id} (BudgetService.swift)
    @PUT("budgets/{id}")
    suspend fun updateBudget(
        @Path("id") id: String,
        @Body body: CreateBudgetRequestDto // Sử dụng lại DTO request body
    ): ApiResponseDto<BudgetDto>

    // case .budgetDelete(id: String) -> DELETE /budgets/{id} (BudgetService.swift)
    @DELETE("budgets/{id}")
    suspend fun deleteBudget(@Path("id") id: String): ApiResponseDto<Unit> // Unit tương đương EmptyResponse
}