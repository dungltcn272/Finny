package com.ltcn272.finny.data.mapper

import com.ltcn272.finny.data.local.entities.TransactionEntity
import com.ltcn272.finny.data.remote.dto.TransactionDto
import com.ltcn272.finny.data.remote.dto.CreateTransactionRequestDto
import com.ltcn272.finny.domain.model.Transaction
import com.ltcn272.finny.domain.model.Location
import com.ltcn272.finny.domain.util.DateUtils

// 1. DTO (API Response) -> Entity (RoomDB)
fun TransactionDto.toEntity(isSynced: Boolean = true): TransactionEntity {
    return TransactionEntity(
        id = this.id,
        name = this.name,
        budgetId = this.budgetId,
        type = this.type,
        description = this.description,
        userId = this.userId,
        category = this.category,
        amount = this.amount,
        dateTime = this.dateTime,
        image = this.image,
        // Location là một đối tượng, không cần mapper riêng vì nó là @Embedded
        location = this.location?.let { Location(it.name, it.lat, it.lng) },
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        isSynced = isSynced,
        isDeleted = false
    )
}

// 2. Entity (RoomDB) -> Domain (UI/Business Logic)
fun TransactionEntity.toDomain(): Transaction {
    // Chuyển đổi String Date thành ZonedDateTime
    val dateTimeZoned = this.dateTime?.let { DateUtils.parseIso8601(it) }
    val createdAtZoned = this.createdAt?.let { DateUtils.parseIso8601(it) }
    val updatedAtZoned = this.updatedAt?.let { DateUtils.parseIso8601(it) }

    return Transaction(
        id = this.id,
        name = this.name,
        budgetId = this.budgetId,
        type = this.type,
        description = this.description,
        userId = this.userId,
        category = this.category,
        amount = this.amount,
        dateTime = dateTimeZoned,
        image = this.image,
        location = this.location,
        createdAt = createdAtZoned,
        updatedAt = updatedAtZoned
    )
}

// 3. Domain (UI Request) -> DTO (API POST/PUT Request Body)
fun Transaction.toCreateRequestDto(): CreateTransactionRequestDto {

    // 1. Khắc phục lỗi Type Mismatch: Chuyển ZonedDateTime sang String (ISO8601)
    val dateTimeApiString = this.dateTime?.let { DateUtils.formatIso8601(it) }

    // 2. Mapper cho Location
    val locationRequestDto = this.location?.let { domainLocation ->
        CreateTransactionRequestDto.LocationRequestDto(
            name = domainLocation.name,
            lat = domainLocation.lat,
            lng = domainLocation.lng
        )
    }

    return CreateTransactionRequestDto(
        name = this.name,
        budgetId = this.budgetId,
        type = this.type,
        description = this.description,
        category = this.category,
        amount = this.amount,
        dateTime = dateTimeApiString, // <-- Đã được xác định là String, không còn là ZonedDateTime
        image = this.image,
        location = locationRequestDto
    )
}

fun Transaction.toEntity(isSynced: Boolean, isDeleted: Boolean): TransactionEntity {
    return TransactionEntity(
        id = this.id,
        name = this.name,
        budgetId = this.budgetId,
        type = this.type,
        description = this.description,
        userId = this.userId,
        category = this.category,
        amount = this.amount,

        // Chuyển ZonedDateTime? sang String? ISO8601
        dateTime = this.dateTime?.let { DateUtils.formatIso8601(it) },

        image = this.image,
        location = this.location, // Location là Domain Model (@Embedded)

        // Chuyển ZonedDateTime? sang String? ISO8601
        createdAt = this.createdAt?.let { DateUtils.formatIso8601(it) },
        updatedAt = this.updatedAt?.let { DateUtils.formatIso8601(it) },

        // Trạng thái sync
        isSynced = isSynced,
        isDeleted = isDeleted
    )
}