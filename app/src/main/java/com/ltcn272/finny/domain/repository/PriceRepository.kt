package com.ltcn272.finny.domain.repository

import com.ltcn272.finny.domain.model.PricePlan
import com.ltcn272.finny.domain.util.AppResult

interface PriceRepository {
    // case .getPrices(version: String) -> GET /prices
    suspend fun getPricePlans(version: String): AppResult<List<PricePlan>>
}