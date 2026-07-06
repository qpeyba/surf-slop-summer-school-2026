package com.qpeyba.surf_slop_summer_school_2026.data.remote.interceptor

import com.qpeyba.surf_slop_summer_school_2026.data.local.TokenStorage
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class UnauthorizedInterceptor @Inject constructor(
    private val tokenStorage: TokenStorage
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == 401) {
            runBlocking { tokenStorage.clear() }
        }
        return response
    }
}
