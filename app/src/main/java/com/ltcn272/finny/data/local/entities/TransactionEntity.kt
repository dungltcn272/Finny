package com.ltcn272.finny.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tên bảng: Transactions
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val budgetId: String,
    val type: String,
    val description: String?,
    val userId: String?,
    val category: String?,
    val amount: Double,
    val dateTime: String,
    val image: String?,
    val localImagePath: String?, // Local file path for offline images
    val locationName: String?,
    val locationLat: Double?,
    val locationLng: Double?,
    val createdAt: String?,
    val updatedAt: String?,
    // Sync status fields
    val isSynced: Boolean = true,
    val isDeleted: Boolean = false
)