package com.ltcn272.finny.data.remote.api

import com.ltcn272.finny.data.remote.dto.UpdateUserRequestDto
import com.ltcn272.finny.data.remote.dto.UserDto
import com.ltcn272.finny.data.remote.dto.RefreshRequestDto
import com.ltcn272.finny.data.remote.dto.ApiResponseDto
import com.ltcn272.finny.data.remote.dto.AuthDataDto
import retrofit2.http.*

interface AuthApi {

    /**
     * API Login: Backend nhận Firebase ID Token qua Header "Authorization"
     * và trả về API Access/Refresh Token.
     * Firebase ID Token sẽ được gắn vào Header bởi AuthInterceptor.
     * Note: Hàm này không có @Body vì ID Token đã được xử lý ở tầng Interceptor.
     */
    @POST("auth/login")
    suspend fun login(): ApiResponseDto<AuthDataDto> // API trả về UserDto và AuthToken DTO

    @POST("auth/refresh")
    suspend fun refreshToken(@Header("Authorization") authHeader: String, @Body body: RefreshRequestDto): ApiResponseDto<AuthDataDto>

    @GET("auth/profile")
    suspend fun getProfile(): ApiResponseDto<UserDto>

    @PUT("auth/profile")
    suspend fun updateProfile(@Body body: UpdateUserRequestDto): ApiResponseDto<UserDto>
}