package com.ltcn272.finny.di

import com.ltcn272.finny.data.repository.AuthRepositoryImpl
import com.ltcn272.finny.data.repository.BudgetRepositoryImpl
import com.ltcn272.finny.data.repository.CategoryRepositoryImpl
import com.ltcn272.finny.data.repository.ChatRepositoryImpl
import com.ltcn272.finny.data.repository.DashboardRepositoryImpl
import com.ltcn272.finny.data.repository.FcmRepositoryImpl
import com.ltcn272.finny.data.repository.NotificationRepositoryImpl
import com.ltcn272.finny.data.repository.PriceRepositoryImpl
import com.ltcn272.finny.data.repository.ProfileRepositoryImpl
import com.ltcn272.finny.data.repository.TransactionRepositoryImpl
import com.ltcn272.finny.domain.repository.AuthRepository
import com.ltcn272.finny.domain.repository.BudgetRepository
import com.ltcn272.finny.domain.repository.CategoryRepository
import com.ltcn272.finny.domain.repository.ChatRepository
import com.ltcn272.finny.domain.repository.DashboardRepository
import com.ltcn272.finny.domain.repository.FcmRepository
import com.ltcn272.finny.domain.repository.NotificationRepository
import com.ltcn272.finny.domain.repository.PriceRepository
import com.ltcn272.finny.domain.repository.ProfileRepository
import com.ltcn272.finny.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    abstract fun bindBudgetRepository(impl: BudgetRepositoryImpl): BudgetRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(transactionRepositoryImpl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindPriceRepository(impl: PriceRepositoryImpl): PriceRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(profileRepositoryImpl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    abstract fun bindFcmRepository(impl: FcmRepositoryImpl): FcmRepository

    @Binds
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(notificationRepositoryImpl: NotificationRepositoryImpl): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindDashboardRepository(dashboardRepositoryImpl: DashboardRepositoryImpl): DashboardRepository
    @Binds
    @Singleton
    abstract fun bindChatRepository(chatRepositoryImpl: ChatRepositoryImpl): ChatRepository

}

