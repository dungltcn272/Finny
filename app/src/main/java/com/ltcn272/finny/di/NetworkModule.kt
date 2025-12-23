package com.ltcn272.finny.di

import com.ltcn272.finny.data.remote.AuthInterceptor
import com.ltcn272.finny.data.remote.api.AuthApi
import com.ltcn272.finny.data.remote.api.BudgetApi
import com.ltcn272.finny.data.remote.api.CategoryApi
import com.ltcn272.finny.data.remote.api.DashboardApi
import com.ltcn272.finny.data.remote.api.FcmApi
import com.ltcn272.finny.data.remote.api.NotificationApi
import com.ltcn272.finny.data.remote.api.ProfileApi
import com.ltcn272.finny.data.remote.api.TransactionApi
import com.ltcn272.finny.data.remote.api.PriceApi
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

    // 🔹 Retrofit with AuthInterceptor — used for APIs that require a token
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

    @Provides
    @Singleton
    fun provideProfileApiService(@AuthedRetrofit retrofit: Retrofit): ProfileApi {
        return retrofit.create(ProfileApi::class.java)
    }

    @Provides
    @Singleton
    fun providePriceApiService(@AuthedRetrofit retrofit: Retrofit): PriceApi {
        return retrofit.create(PriceApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCategoryApiService(@AuthedRetrofit retrofit: Retrofit): CategoryApi {
        return retrofit.create(CategoryApi::class.java)
    }

    @Provides
    @Singleton
    fun provideFcmApiService(@AuthedRetrofit retrofit: Retrofit): FcmApi {
        return retrofit.create(FcmApi::class.java)
    }

    @Provides
    @Singleton
    fun provideNotificationApi(@AuthedRetrofit retrofit: Retrofit): NotificationApi {
        return retrofit.create(NotificationApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDashboardApi(@AuthedRetrofit retrofit: Retrofit): DashboardApi {
        return retrofit.create(DashboardApi::class.java)
    }

}
