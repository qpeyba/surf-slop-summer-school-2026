package com.qpeyba.surf_slop_summer_school_2026.data.remote.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.Buffer

class HttpLoggingInterceptor : Interceptor {
    companion object {
        private const val TAG = "HTTP_LOG"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val method = request.method
        val url = request.url.toString()

        var requestBodyString = ""
        var modifiedRequest = request

        val body = request.body
        if (body != null) {
            try {
                val buffer = Buffer()
                body.writeTo(buffer)
                requestBodyString = buffer.readUtf8()
                modifiedRequest = request.newBuilder()
                    .method(method, requestBodyString.toRequestBody(body.contentType()))
                    .build()
            } catch (_: Exception) {
            }
        }

        Log.d(TAG, "--> $method $url | Body: $requestBodyString")

        try {
            val response = chain.proceed(modifiedRequest)
            val responseBodyString = try {
                response.peekBody(Long.MAX_VALUE).string()
            } catch (_: Exception) {
                ""
            }

            Log.d(TAG, "<-- ${response.code} $url | Body: $responseBodyString")
            return response
        } catch (e: Exception) {
            Log.d(TAG, "<-- ERROR $url | $e")
            throw e
        }
    }
}
