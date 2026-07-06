package com.qpeyba.surf_slop_summer_school_2026.data.remote.interceptor

import com.qpeyba.surf_slop_summer_school_2026.data.local.TokenStorage
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenStorage: TokenStorage
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        if (original.url.encodedPath.contains("auth/otp")) {
            return chain.proceed(original)
        }

        val token = runBlocking { tokenStorage.getToken() }
        return if (token != null) {
            val request = original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            chain.proceed(request)
        } else {
            chain.proceed(original)
        }
    }
}
