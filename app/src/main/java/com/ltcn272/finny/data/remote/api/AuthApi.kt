package com.ltcn272.finny.data.remote.api

import com.ltcn272.finny.data.remote.dto.UpdateUserRequestDto
import com.ltcn272.finny.data.remote.dto.UserDto
import com.ltcn272.finny.data.remote.dto.RefreshRequestDto
import com.ltcn272.finny.data.remote.dto.ApiResponseDto
import com.ltcn272.finny.data.remote.dto.AuthDataDto
import retrofit2.http.*

interface AuthApi {

    // Tương đương với case Endpoint.login(firebaseToken: String)
    @POST("auth/login")
    suspend fun loginWithFirebaseToken(@Header("Authorization") firebaseToken: String): ApiResponseDto<AuthDataDto>

    // Tương đương với case Endpoint.refresh(refreshToken: String)
    @POST("auth/refresh-token")
    suspend fun refreshToken(
        // Token cũ được truyền qua Authorization Header
        @Header("Authorization") oldRefreshTokenHeader: String,
        // Và qua JSON Body
        @Body body: Map<String, String>
    ):  ApiResponseDto<AuthDataDto>

}