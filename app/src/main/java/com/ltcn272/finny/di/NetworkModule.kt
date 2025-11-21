package com.ltcn272.finny.di

import com.ltcn272.finny.data.remote.AuthInterceptor
import com.ltcn272.finny.data.remote.api.AuthApi
import com.ltcn272.finny.data.remote.api.BudgetApi
import com.ltcn272.finny.data.remote.api.TransactionApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthOnlyRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthedRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://qlct.vercel.app/api/v1/"

    @Provides
    @Singleton
    @AuthOnlyRetrofit
    fun provideAuthOnlyRetrofit(): Retrofit {
        val client = OkHttpClient.Builder()
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 🔹 Retrofit có AuthInterceptor — dùng cho các API cần token
    @Provides
    @Singleton
    @AuthedRetrofit
    fun provideAuthedRetrofit(authInterceptor: AuthInterceptor): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApiService(@AuthOnlyRetrofit retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideBudgetApiService(@AuthedRetrofit retrofit: Retrofit): BudgetApi {
        return retrofit.create(BudgetApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTransactionApiService(@AuthedRetrofit retrofit: Retrofit): TransactionApi {
        return retrofit.create(TransactionApi::class.java)
    }
}