package com.ltcn272.finny.domain.repository

import com.ltcn272.finny.domain.model.PricePlan
import com.ltcn272.finny.domain.util.AppResult

interface PriceRepository {
    suspend fun getPricePlans(version: String): AppResult<List<PricePlan>>
}