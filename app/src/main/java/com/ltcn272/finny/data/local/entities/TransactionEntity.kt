package com.ltcn272.finny.data.local.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ltcn272.finny.domain.model.Location

// Tên bảng: Transactions
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val name: String?,
    val budgetId: String?,
    val type: String?,
    val description: String?,
    val userId: String?,
    val category: String?,
    val amount: Double,
    val dateTime: String?, // Lưu trữ dạng String (ISO8601)
    val image: String?,
    @Embedded(prefix = "loc_") // Dùng @Embedded cho Location
    val location: Location?,
    val createdAt: String?,
    val updatedAt: String?,
    // Trạng thái đồng bộ
    val isSynced: Boolean = true,
    val isDeleted: Boolean = false
)