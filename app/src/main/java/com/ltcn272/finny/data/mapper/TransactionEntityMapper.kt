package com.ltcn272.finny.data.mapper

import com.ltcn272.finny.data.local.entities.TransactionEntity
import com.ltcn272.finny.data.remote.dto.CreateTransactionRequestDto
import com.ltcn272.finny.data.remote.dto.TransactionDto
import com.ltcn272.finny.domain.model.Location
import com.ltcn272.finny.domain.model.Transaction
import com.ltcn272.finny.domain.model.TransactionCategory
import com.ltcn272.finny.domain.model.TransactionType
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
        localImagePath = null, // Data from API never has a local path
        locationName = this.location?.name,
        locationLat = this.location?.lat,
        locationLng = this.location?.lng,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        isSynced = isSynced,
        isDeleted = false
    )
}

// 2. Entity (RoomDB) -> Domain (UI/Business Logic)
fun TransactionEntity.toDomain(): Transaction {
    val location = if (this.locationLat != null && this.locationLng != null) {
        Location(this.locationName, this.locationLat, this.locationLng)
    } else {
        null
    }

    val transactionType = try {
        this.type.let { TransactionType.valueOf(it.uppercase()) }
    } catch (e: IllegalArgumentException) {
        TransactionType.OUTCOME
    }

    val transactionCategory = try {
        this.category?.let { TransactionCategory.valueOf(it.uppercase()) } ?: TransactionCategory.OTHER
    } catch (e: IllegalArgumentException) {
        TransactionCategory.OTHER
    }

    return Transaction(
        id = this.id,
        name = this.name,
        budgetId = this.budgetId,
        type = transactionType,
        description = this.description,
        userId = this.userId,
        category = transactionCategory,
        amount = this.amount,
        dateTime = this.dateTime.let { DateUtils.parseApiDate(it) },
        image = this.image,
        localImagePath = this.localImagePath,
        location = location,
        createdAt = this.createdAt?.let { DateUtils.parseIso8601(it) },
        updatedAt = this.updatedAt?.let { DateUtils.parseIso8601(it) }
    )
}

// 3. Domain (UI Request) -> DTO (API POST/PUT Request Body)
fun Transaction.toCreateRequestDto(): CreateTransactionRequestDto {
    val locationDto = this.location?.let {
        CreateTransactionRequestDto.LocationRequestDto(
            name = it.name,
            lat = it.lat,
            lng = it.lng
        )
    }
    return CreateTransactionRequestDto(
        name = this.name,
        budgetId = this.budgetId,
        type = this.type.name,
        description = this.description,
        category = this.category.name,
        amount = this.amount,
        dateTime = this.dateTime.let { DateUtils.formatApiRequestDate(it) },
        image = this.image,
        location = locationDto
    )
}

fun Transaction.toEntity(isSynced: Boolean, isDeleted: Boolean): TransactionEntity {
    return TransactionEntity(
        id = this.id,
        name = this.name,
        budgetId = this.budgetId,
        type = this.type.name,
        description = this.description,
        userId = this.userId,
        category = this.category.name,
        amount = this.amount,
        dateTime = this.dateTime.let { DateUtils.formatApiRequestDate(it) },
        image = this.image,
        localImagePath = this.localImagePath,
        locationName = this.location?.name,
        locationLat = this.location?.lat,
        locationLng = this.location?.lng,
        createdAt = this.createdAt?.let { DateUtils.formatIso8601(it) },
        updatedAt = this.updatedAt?.let { DateUtils.formatIso8601(it) },
        isSynced = isSynced,
        isDeleted = isDeleted
    )
}