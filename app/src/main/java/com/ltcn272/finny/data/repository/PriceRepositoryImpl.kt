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

            if (response.status == 200) {
                // Map DTO sang Domain Model
                val pricePlans = response.data.map { it.toDomain() }
                AppResult.Success(pricePlans)
            } else {
                AppResult.Error(response.message)
            }
        } catch (e: Exception) {
            AppResult.Error("Failed to fetch price plans: ${e.localizedMessage}", e)
        }
    }
}