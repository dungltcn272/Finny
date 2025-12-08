package com.ltcn272.finny.di

import com.ltcn272.finny.data.repository.AuthRepositoryImpl
import com.ltcn272.finny.data.repository.BudgetRepositoryImpl
import com.ltcn272.finny.data.repository.ProfileRepositoryImpl
import com.ltcn272.finny.data.repository.TransactionRepositoryImpl
import com.ltcn272.finny.data.repository.PriceRepositoryImpl
import com.ltcn272.finny.domain.repository.AuthRepository
import com.ltcn272.finny.domain.repository.BudgetRepository
import com.ltcn272.finny.domain.repository.ProfileRepository
import com.ltcn272.finny.domain.repository.TransactionRepository
import com.ltcn272.finny.domain.repository.PriceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(budgetRepositoryImpl: BudgetRepositoryImpl): BudgetRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(transactionRepositoryImpl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(profileRepositoryImpl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindPriceRepository(priceRepositoryImpl: PriceRepositoryImpl): PriceRepository
}
