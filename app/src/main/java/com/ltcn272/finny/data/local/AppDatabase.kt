package com.ltcn272.finny.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ltcn272.finny.data.local.dao.BudgetDao
import com.ltcn272.finny.data.local.dao.TransactionDao
import com.ltcn272.finny.data.local.entities.BudgetEntity
import com.ltcn272.finny.data.local.entities.TransactionEntity

@Database(
    entities = [BudgetEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun budgetDao(): BudgetDao
    abstract fun transactionDao(): TransactionDao
}