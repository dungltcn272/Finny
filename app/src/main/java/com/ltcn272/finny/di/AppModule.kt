package com.ltcn272.finny.di

import android.content.Context
import androidx.work.WorkManager
import com.ltcn272.finny.core.AppStateManager
import com.ltcn272.finny.core.SyncScheduler // Import lớp mới
import com.ltcn272.finny.core.TokenManager // Đã có
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Cung cấp Application Context
    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context {
        return context
    }

    // Cung cấp Token Manager (đã có)
    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }

    // Cung cấp Sync Scheduler (MỚI)
    @Provides
    @Singleton
    fun provideSyncScheduler(@ApplicationContext context: Context): SyncScheduler {
        return SyncScheduler(context)
    }

    // Cung cấp WorkManager instance
    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideAppStateManager(@ApplicationContext context: Context): AppStateManager {
        return AppStateManager(context)
    }
}