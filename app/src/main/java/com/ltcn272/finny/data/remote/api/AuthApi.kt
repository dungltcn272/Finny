package com.ltcn272.finny.data.remote.api

import com.ltcn272.finny.data.remote.dto.ApiResponse
import com.ltcn272.finny.data.remote.dto.AuthDataDto
import retrofit2.http.*

interface AuthApi {

    @POST("auth/login")
    suspend fun loginWithFirebaseToken(@Header("Authorization") firebaseToken: String): ApiResponse<AuthDataDto>

    @POST("auth/refresh-token")
    suspend fun refreshToken(
        @Header("Authorization") oldRefreshTokenHeader: String,
        @Body body: Map<String, String>
    ):  ApiResponse<AuthDataDto>

}