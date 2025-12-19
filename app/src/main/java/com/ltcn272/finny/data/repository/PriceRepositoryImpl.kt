package com.ltcn272.finny.data.repository

import com.ltcn272.finny.data.mapper.toPriceDomain
import com.ltcn272.finny.data.remote.api.PriceApi
import com.ltcn272.finny.domain.model.PricePlan
import com.ltcn272.finny.domain.repository.PriceRepository
import com.ltcn272.finny.domain.util.AppResult
import com.ltcn272.finny.domain.util.toErrorType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class PriceRepositoryImpl @Inject constructor(
    private val priceApi: PriceApi
) : PriceRepository {
    override fun getPricePlans(version: String): Flow<AppResult<List<PricePlan>>> =
        flow {
        emit(AppResult.Loading)
        try {
            val response = priceApi.getPrices(version)
            val pricePlans = response.toPriceDomain()
            emit(AppResult.Success(pricePlans))
        } catch (e: Exception) {
            emit(AppResult.Error(e.toErrorType()))
        }
    }
}
