package com.ltcn272.finny.data.remote.api

import com.ltcn272.finny.data.remote.dto.ApiResponseDto
import com.ltcn272.finny.data.remote.dto.ProfileResponse
import com.ltcn272.finny.data.remote.dto.UpdateUserRequestDto
import com.ltcn272.finny.data.remote.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ProfileApi {

    @GET("users/profile")
    suspend fun getProfile(): ProfileResponse

    @POST("users/profile")
    suspend fun updateProfile(@Body body: UpdateUserRequestDto): ApiResponseDto<UserDto>
}
