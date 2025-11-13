package com.ltcn272.finny.data.remote.api

import com.ltcn272.finny.data.remote.dto.PriceResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface PriceApi {
    // case .getPrices(version: String) -> GET /prices (PriceService.swift)
    @GET("prices")
    suspend fun getPrices(@Query("version") version: String): PriceResponseDto
}