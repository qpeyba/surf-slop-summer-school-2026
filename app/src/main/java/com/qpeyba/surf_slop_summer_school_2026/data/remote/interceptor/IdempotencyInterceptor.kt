package com.qpeyba.surf_slop_summer_school_2026.data.remote.interceptor

import com.qpeyba.surf_slop_summer_school_2026.util.IdempotencyKey
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class IdempotencyInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        if (original.method == "POST" && original.url.encodedPath.contains("bookings")) {
            val request = original.newBuilder()
                .header("Idempotency-Key", IdempotencyKey.generate())
                .build()
            return chain.proceed(request)
        }
        return chain.proceed(original)
    }
}
