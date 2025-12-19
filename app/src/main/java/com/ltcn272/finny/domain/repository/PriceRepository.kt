package com.ltcn272.finny.domain.repository

import com.ltcn272.finny.domain.model.PricePlan
import com.ltcn272.finny.domain.util.AppResult
import kotlinx.coroutines.flow.Flow

interface PriceRepository {
    fun getPricePlans(version: String): Flow<AppResult<List<PricePlan>>>
}
