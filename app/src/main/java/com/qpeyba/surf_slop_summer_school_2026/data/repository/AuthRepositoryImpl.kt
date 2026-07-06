package com.qpeyba.surf_slop_summer_school_2026.data.repository

import com.qpeyba.surf_slop_summer_school_2026.data.local.FcmTokenManager
import com.qpeyba.surf_slop_summer_school_2026.data.local.TokenStorage
import com.qpeyba.surf_slop_summer_school_2026.data.remote.api.AuthApi
import com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.request.OtpRequest
import com.qpeyba.surf_slop_summer_school_2026.data.remote.dto.request.OtpVerifyRequest
import com.qpeyba.surf_slop_summer_school_2026.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage,
    private val fcmTokenManager: FcmTokenManager
) : AuthRepository {

    override suspend fun requestOtp(phone: String): Result<Unit> {
        return try {
            val response = authApi.requestOtp(OtpRequest(phone))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("request_otp_failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyOtp(phone: String, code: String): Result<String> {
        return try {
            val response = authApi.verifyOtp(OtpVerifyRequest(phone, code))
            if (response.isSuccessful) {
                val body = response.body()!!
                tokenStorage.saveToken(body.accessToken, body.expiresIn)
                Result.success(body.accessToken)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "invalid_code"
                    429 -> "too_many_requests"
                    else -> "verify_failed: ${response.code()}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun hasValidToken(): Boolean {
        return tokenStorage.hasValidToken()
    }

    override suspend fun logout() {
        tokenStorage.clear()
        fcmTokenManager.clear()
    }

    override suspend fun saveToken(token: String, expiresIn: Long) {
        tokenStorage.saveToken(token, expiresIn)
    }
}
