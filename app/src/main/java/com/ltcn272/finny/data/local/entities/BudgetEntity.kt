package com.ltcn272.finny.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(

    @PrimaryKey
    val id: String,

    val serverId : String? = null,

    val name: String,

    val amount: Long,

    val limit: Long,

    val currency: String,

    val startDate: String,

    // ===== Recurring config =====
    val recurringActive: Boolean,

    val recurringIntervalUnit: String?, // day | week | month | year

    val recurringIntervalValue: Int,

    val recurringTopupAmount: Long,

    val recurringNextRunAt: String?,

    val recurringLastRunAt: String?,

    // ===== Flags =====
    val isSingle: Boolean,

    // ===== SYNC =====
    val syncState: SyncState = SyncState.CREATE,

    val updatedAt: String? = null
)