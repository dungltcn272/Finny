package com.ltcn272.finny.data.remote.api

import com.ltcn272.finny.data.remote.dto.ApiResponse
import com.ltcn272.finny.data.remote.dto.ProfileUserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ProfileApi {

    @GET("users/profile")
    suspend fun getProfile(): ApiResponse<ProfileUserDto>

    @POST("users/profile")
    suspend fun updateProfile(@Body body: Map<String, @JvmSuppressWildcards Any?>): ApiResponse<ProfileUserDto>
}
