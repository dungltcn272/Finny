package com.ltcn272.finny.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tên bảng: Budgets
@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey
    val id: String, // Ánh xạ từ _id
    val name: String,
    val userId: String,
    val amount: Double,
    val startDate: String, // Lưu trữ dạng String (ISO8601 hoặc MM/dd/yyyy)
    val remain: Double,
    val limit: Double,
    val period: String,
    val daysRemaining: Double,
    val diffAvg: Double,
    val progress: Double,
    val totalIncome: Double,
    val totalOutcome: Double,
    val createdAt: String,
    val updatedAt: String,
    // Trạng thái đồng bộ (Đặc biệt quan trọng cho Offline-First)
    val isSynced: Boolean = true, // true: Đã có trên server, false: Đang chờ POST/PUT
    val isDeleted: Boolean = false // true: Đã xóa cục bộ, đang chờ DELETE API
)