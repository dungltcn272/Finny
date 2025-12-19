package com.ltcn272.finny.data.remote.api

import com.ltcn272.finny.data.remote.dto.ApiResponse
import com.ltcn272.finny.data.remote.dto.CategoryListDataDto
import com.ltcn272.finny.data.remote.dto.CategoryResponseDto
import com.ltcn272.finny.data.remote.dto.CreateCategoryDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface CategoryApi {

    @GET("categories/list")
    suspend fun getList(
        @Query("page") page: Int = 1
    ): ApiResponse<CategoryListDataDto>

    @POST("categories")
    suspend fun create(
        @Body body: CreateCategoryDto
    ): ApiResponse<CategoryResponseDto>

    @PUT("categories/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body body: Map<String,Any?>
    ): ApiResponse<CategoryResponseDto>

    @DELETE("categories/{id}")
    suspend fun delete(
        @Path("id") id: String
    ): ApiResponse<Unit>
}

