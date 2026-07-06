package com.qpeyba.surf_slop_summer_school_2026.data.remote.api

import com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.request.OtpRequest
import com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.request.OtpVerifyRequest
import com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.response.TokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/v1/auth/otp/request")
    suspend fun requestOtp(@Body body: OtpRequest): Response<Unit>

    @POST("api/v1/auth/otp/verify")
    suspend fun verifyOtp(@Body body: OtpVerifyRequest): Response<TokenResponse>
}
