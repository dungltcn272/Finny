package com.ltcn272.finny.data.mapper

import com.ltcn272.finny.data.remote.dto.PriceDto
import com.ltcn272.finny.domain.model.PricePlan
import com.ltcn272.finny.domain.util.DateUtils // Cần dùng DateUtils đã tạo

/**
 * Mapper để chuyển đổi PriceDto (Data Layer) sang PricePlan (Domain Layer).
 */
fun PriceDto.toDomain(): PricePlan {
    // Chuyển đổi String ISO8601 sang ZonedDateTime
    val createdAtZoned = DateUtils.parseIso8601(this.createdAt)
    val updatedAtZoned = DateUtils.parseIso8601(this.updatedAt)

    return PricePlan(
        id = this.id,
        planName = this.planName,
        price = this.price,
        currency = this.currency,
        period = this.period,
        features = this.features,
        isDefault = this.isDefault,
        createdAt = createdAtZoned,
        updatedAt = updatedAtZoned
    )
}

// Đối với Price Plan (Gói dịch vụ), thường không cần Domain -> DTO (vì chúng ta không sửa gói dịch vụ)