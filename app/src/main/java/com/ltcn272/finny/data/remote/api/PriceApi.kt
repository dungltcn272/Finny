package com.ltcn272.finny.data.remote.api

import com.ltcn272.finny.data.remote.dto.PriceApiResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface PriceApi {
    @GET("prices")
    suspend fun getPrices(@Query("version") version: String): PriceApiResponseDto
}