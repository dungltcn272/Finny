package com.ltcn272.finny.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ltcn272.finny.data.remote.AuthInterceptor
import com.ltcn272.finny.data.remote.TokenAuthenticator
import com.ltcn272.finny.data.remote.api.AuthApi
import com.ltcn272.finny.data.remote.api.BudgetApi
import com.ltcn272.finny.data.remote.api.TransactionApi
import com.ltcn272.finny.data.remote.api.PriceApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://api.yourbackend.com/v1/" // Đặt BASE URL chính xác

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder().create()
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Hiển thị chi tiết request/response
        }
    }

    /**
     * Cung cấp OkHttpClient đã được cấu hình.
     * Đây là nơi tất cả cơ chế quản lý phiên được áp dụng.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator // <-- OkHttp Authenticator xử lý 401
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)        // Thêm Header Authorization (Access Token/ID Token)
            .authenticator(tokenAuthenticator)      // Xử lý tự động lỗi 401
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Cung cấp Retrofit Builder sử dụng OkHttpClient đã cấu hình và Gson Converter.
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // --- Cung cấp tất cả Retrofit API Services ---

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideBudgetApi(retrofit: Retrofit): BudgetApi {
        return retrofit.create(BudgetApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTransactionApi(retrofit: Retrofit): TransactionApi {
        return retrofit.create(TransactionApi::class.java)
    }

    @Provides
    @Singleton
    fun providePriceApi(retrofit: Retrofit): PriceApi {
        return retrofit.create(PriceApi::class.java)
    }
}