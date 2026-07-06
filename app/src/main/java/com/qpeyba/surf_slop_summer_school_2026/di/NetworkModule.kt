package com.qpeyba.surf_slop_summer_school_2026.di

import com.qpeyba.surf_slop_summer_school_2026.data.local.TokenStorage
import com.qpeyba.surf_slop_summer_school_2026.data.remote.api.AuthApi
import com.qpeyba.surf_slop_summer_school_2026.data.remote.api.BookingsApi
import com.qpeyba.surf_slop_summer_school_2026.data.remote.api.InstructorsApi
import com.qpeyba.surf_slop_summer_school_2026.data.remote.api.ProfileApi
import com.qpeyba.surf_slop_summer_school_2026.data.remote.api.SlotsApi
import com.qpeyba.surf_slop_summer_school_2026.data.remote.interceptor.AuthInterceptor
import com.qpeyba.surf_slop_summer_school_2026.data.remote.interceptor.IdempotencyInterceptor
import com.qpeyba.surf_slop_summer_school_2026.data.remote.interceptor.UnauthorizedInterceptor
import com.qpeyba.surf_slop_summer_school_2026.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenStorage: TokenStorage): AuthInterceptor {
        return AuthInterceptor(tokenStorage)
    }

    @Provides
    @Singleton
    fun provideIdempotencyInterceptor(): IdempotencyInterceptor {
        return IdempotencyInterceptor()
    }

    @Provides
    @Singleton
    fun provideUnauthorizedInterceptor(tokenStorage: TokenStorage): UnauthorizedInterceptor {
        return UnauthorizedInterceptor(tokenStorage)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        idempotencyInterceptor: IdempotencyInterceptor,
        unauthorizedInterceptor: UnauthorizedInterceptor
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(idempotencyInterceptor)
            .addInterceptor(unauthorizedInterceptor)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideProfileApi(retrofit: Retrofit): ProfileApi = retrofit.create(ProfileApi::class.java)

    @Provides
    @Singleton
    fun provideSlotsApi(retrofit: Retrofit): SlotsApi = retrofit.create(SlotsApi::class.java)

    @Provides
    @Singleton
    fun provideBookingsApi(retrofit: Retrofit): BookingsApi = retrofit.create(BookingsApi::class.java)

    @Provides
    @Singleton
    fun provideInstructorsApi(retrofit: Retrofit): InstructorsApi = retrofit.create(InstructorsApi::class.java)
}
