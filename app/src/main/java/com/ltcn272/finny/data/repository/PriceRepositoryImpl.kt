package com.ltcn272.finny.data.repository

import com.ltcn272.finny.data.mapper.toDomain
import com.ltcn272.finny.data.remote.api.PriceApi
import com.ltcn272.finny.domain.model.PricePlan
import com.ltcn272.finny.domain.repository.PriceRepository
import com.ltcn272.finny.domain.util.AppResult
import javax.inject.Inject

class PriceRepositoryImpl @Inject constructor(
    private val priceApi: PriceApi
) : PriceRepository {
    override suspend fun getPricePlans(version: String): AppResult<List<PricePlan>> {
        return try {
            val response = priceApi.getPrices(version)

            // Adapter: PriceResponseDto.toDomain() returns List<PricePlan>
            val pricePlans = response.toDomain()
            AppResult.Success(pricePlans)
        } catch (e: Exception) {
            AppResult.Error("Failed to fetch price plans: ${e.localizedMessage}", e)
        }
    }
}