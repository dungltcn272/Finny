package com.ltcn272.finny.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ltcn272.finny.data.local.dao.BudgetDao
import com.ltcn272.finny.data.local.dao.TransactionDao
import com.ltcn272.finny.data.local.dao.CategoryDao
import com.ltcn272.finny.data.local.entities.BudgetEntity
import com.ltcn272.finny.data.local.entities.TransactionEntity
import com.ltcn272.finny.data.local.entities.CategoryEntity

@Database(
    entities = [BudgetEntity::class, TransactionEntity::class, CategoryEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(SyncStateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun budgetDao(): BudgetDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
}